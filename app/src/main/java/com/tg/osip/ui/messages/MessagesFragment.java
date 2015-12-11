package com.tg.osip.ui.messages;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.business.chats.ChatsInteract;
import com.tg.osip.business.messages.MessagesInteract;
import com.tg.osip.business.models.MessageAdapterModel;
import com.tg.osip.business.models.PhotoItem;
import com.tg.osip.tdclient.TGProxyI;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.ui.activities.MainActivity;
import com.tg.osip.ui.activities.PhotoMediaActivity;
import com.tg.osip.ui.general.views.pagination.PaginationTool;
import com.tg.osip.utils.common.BackgroundExecutor;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author e.matsyuk
 */
public class MessagesFragment extends Fragment {

    public static final String CHAT_ID = "chat_id";
    private static final int LIMIT = 50;
    // one item from previous screen (chats)
    private static final int EMPTY_COUNT_LIST = 1;

    @Inject
    MessagesInteract messagesInteract;

    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private MessagesRecyclerAdapter messagesRecyclerAdapter = new MessagesRecyclerAdapter();
    private MessageToolbarAdapter messageToolbarAdapter;

    private Subscription loadDataSubscription;

    private long chatId;

    public static MessagesFragment newInstance(long chatId) {
        MessagesFragment f = new MessagesFragment();
        Bundle args = new Bundle();
        args.putLong(CHAT_ID, chatId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            return;
        }
        ApplicationSIP.get().applicationComponent().plus(new MessagesModule()).inject(this);
        chatId = getArguments().getLong(CHAT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fmt_messages, container, false);
        setRetainInstance(true);
        initEmptyToolbar();
        init(rootView);
        return rootView;
    }

    private void init(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.RecyclerView);
        // init LayoutManager
        GridLayoutManager recyclerViewLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerViewLayoutManager.supportsPredictiveItemAnimations();
        recyclerViewLayoutManager.setReverseLayout(true);
        // recyclerView setting
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setAdapter(messagesRecyclerAdapter);
        messagesRecyclerAdapter.setOnMessageClickListener(onMessageClickListener);
        loadData();
    }

    private void initEmptyToolbar() {
        if (getSupportActivity() == null || getSupportActivity().getSupportActionBar() == null) {
            return;
        }
        toolbar = ((MainActivity)getSupportActivity()).getToolbar();
        getSupportActivity().getSupportActionBar().setTitle("");
        getSupportActivity().getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getSupportActivity().onBackPressed());
    }

    private void initToolbar() {
        if (messageToolbarAdapter != null && messageToolbarAdapter.getToolbarView() != null) {
            toolbar.addView(messageToolbarAdapter.getToolbarView());
        }
    }

    private void loadData() {
        loadDataSubscription = messagesInteract.getFirstChatData(chatId)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(this::getChatLoadingSuccess)
                .concatMap(chatMessageAdapterModelPair -> PaginationTool
                        .paging(recyclerView, offset ->
                                messagesInteract.getNextDataPortionInList(offset, LIMIT, chatId, chatMessageAdapterModelPair.first.topMessage.id), LIMIT, EMPTY_COUNT_LIST))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::getNextDataPortionSuccess);
    }

    private void getChatLoadingSuccess(Pair<TdApi.Chat, MessageAdapterModel> chatMessageAdapterModelPair) {
        // toolbar
        messageToolbarAdapter = new MessageToolbarAdapter(getContext(), chatMessageAdapterModelPair.first);
        initToolbar();
        // adapter
        // add message from Chat topMessage only one
        if (messagesRecyclerAdapter.getItemCount() < EMPTY_COUNT_LIST) {
            MessageAdapterModel messageAdapterModel = chatMessageAdapterModelPair.second;
            messagesRecyclerAdapter.addMessageAdapterModel(messageAdapterModel);
        }
    }

    private void getNextDataPortionSuccess(MessageAdapterModel messageAdapterModel) {
        // adapter
        messagesRecyclerAdapter.addMessageAdapterModel(messageAdapterModel);
    }

    private OnMessageClickListener onMessageClickListener = new OnMessageClickListener() {
        @Override
        public void onPhotoMessageClick(int clickedPosition, List<PhotoItem> photoLargeItemList) {
            Intent intent = new Intent(getActivity(), PhotoMediaActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(PhotoMediaActivity.CLICKED_POSITION, clickedPosition);
            bundle.putSerializable(PhotoMediaActivity.PHOTO_LARGE, (Serializable) photoLargeItemList);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    };

    private AppCompatActivity getSupportActivity() {
        return (AppCompatActivity)getActivity();
    }

    @Override
    public void onDestroyView() {
        if (loadDataSubscription != null && !loadDataSubscription.isUnsubscribed()) {
            loadDataSubscription.unsubscribe();
        }
        // for memory leak prevention (RecycleView is not unsubscibed from adapter DataObserver)
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        if (toolbar != null && messageToolbarAdapter != null) {
            toolbar.removeView(messageToolbarAdapter.getToolbarView());
        }
        super.onDestroyView();
    }

    @Subcomponent(modules = MessagesModule.class)
    public interface MessagesComponent {
        void inject(MessagesFragment messagesFragment);
    }

    @Module
    public static class MessagesModule {

        @Provides
        @NonNull
        public MessagesInteract provideMessagesInteract(@NonNull TGProxyI tgProxyI, @NonNull FileDownloaderManager fileDownloaderManager) {
            return new MessagesInteract(
                    tgProxyI,
                    fileDownloaderManager
            );
        }
    }

}
