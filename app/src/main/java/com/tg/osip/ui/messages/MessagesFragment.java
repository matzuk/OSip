package com.tg.osip.ui.messages;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tg.osip.R;
import com.tg.osip.business.messages.MessagesInteract;
import com.tg.osip.business.models.MessageAdapterModel;
import com.tg.osip.business.models.PhotoItem;
import com.tg.osip.ui.activities.MainActivity;
import com.tg.osip.ui.activities.PhotoMediaActivity;
import com.tg.osip.ui.general.DefaultSubscriber;
import com.tg.osip.ui.general.views.pagination.PaginationTool;
import com.tg.osip.utils.common.BackgroundExecutor;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.Serializable;
import java.util.List;

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

    private RecyclerView recyclerView;
    private MessagesRecyclerAdapter messagesRecyclerAdapter;
    private Toolbar toolbar;
    private MessagesInteract messagesInteract = new MessagesInteract();

    private Subscription loadFirstDataSubscription;
    private Subscription listPagingSubscription;

    private long chatId;
    private int topMessageId;

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
        chatId = getArguments().getLong(CHAT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fmt_messages, container, false);
        setRetainInstance(true);
        init(rootView);
        initToolbar();
        return rootView;
    }

    private void init(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.RecyclerView);
        toolbar = ((MainActivity)getSupportActivity()).getToolbar();
        // init LayoutManager
        GridLayoutManager recyclerViewLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerViewLayoutManager.supportsPredictiveItemAnimations();
        recyclerViewLayoutManager.setReverseLayout(true);
        // recyclerView setting
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        // for first start
        if (messagesRecyclerAdapter == null) {
            // init MessagesController
            messagesRecyclerAdapter = new MessagesRecyclerAdapter();
            recyclerView.setAdapter(messagesRecyclerAdapter);
            loadFirstData();
        } else {
            recyclerView.setAdapter(messagesRecyclerAdapter);
            startListPaging();
        }
    }

    private void initToolbar() {
        if (getSupportActivity() == null || getSupportActivity().getSupportActionBar() == null) {
            return;
        }
        getSupportActivity().getSupportActionBar().setTitle("");
        getSupportActivity().getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getSupportActivity().onBackPressed());
    }

    private void loadFirstData() {
        loadFirstDataSubscription = messagesInteract.getFirstChatData(chatId)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<Pair<TdApi.Chat, MessageAdapterModel>>() {
                    @Override
                    public void onNext(Pair<TdApi.Chat, MessageAdapterModel> chatMessageAdapterModelPair) {
                        topMessageId = chatMessageAdapterModelPair.first.topMessage.id;
                        MessageAdapterModel messageAdapterModel = chatMessageAdapterModelPair.second;
                        messagesRecyclerAdapter.addMessageAdapterModel(messageAdapterModel);
                        startListPaging();
                    }
                });
    }

    private void startListPaging() {
        if (messagesRecyclerAdapter.isAllItemsLoaded()) {
            return;
        }
        listPagingSubscription = PaginationTool
                .paging(recyclerView, offset -> messagesInteract.getNextDataPortionInList(offset, LIMIT, chatId, topMessageId), LIMIT, EMPTY_COUNT_LIST)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<MessageAdapterModel>() {
                    @Override
                    public void onNext(MessageAdapterModel messageAdapterModel) {
                        messagesRecyclerAdapter.addMessageAdapterModel(messageAdapterModel);
                    }
                });
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
        if (listPagingSubscription != null && !listPagingSubscription.isUnsubscribed()) {
            listPagingSubscription.unsubscribe();
        }
        if (loadFirstDataSubscription != null && !loadFirstDataSubscription.isUnsubscribed()) {
            loadFirstDataSubscription.unsubscribe();
        }
        // for memory leak prevention (RecycleView is not unsubscibed from adapter DataObserver)
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        super.onDestroyView();
    }

}
