package com.tg.osip.ui.chats;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.tg.osip.R;
import com.tg.osip.business.PersistentInfo;
import com.tg.osip.business.chats.ChatsInteract;
import com.tg.osip.business.models.ChatItem;
import com.tg.osip.ui.activities.MainActivity;
import com.tg.osip.ui.general.DefaultSubscriber;
import com.tg.osip.ui.general.views.pagination.PaginationTool;
import com.tg.osip.ui.messages.MessagesFragment;
import com.tg.osip.ui.general.BaseFragment;
import com.tg.osip.ui.general.views.RecyclerItemClickListener;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Fragment show list of all chats
 *
 * @author e.matsyuk
 */
public class ChatsFragment extends BaseFragment {

    private static final int LIMIT = 50;

    private RecyclerView recyclerView;
    private ChatRecyclerAdapter chatRecyclerAdapter;
    private ChatsInteract chatsInteract = new ChatsInteract();
    private Subscription listPagingSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fmt_chats, container, false);
        setRetainInstance(true);
        init(rootView);
        initToolbar();
        return rootView;
    }

    private void init(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.RecyclerView);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        // init LayoutManager
        GridLayoutManager recyclerViewLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerViewLayoutManager.supportsPredictiveItemAnimations();
        // recyclerView setting
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), (view1, position) -> goToConcreteChat(position))
        );
        // first start
        if (chatRecyclerAdapter == null) {
            // start progressbar
            progressBar.setVisibility(View.VISIBLE);
            // init Controller
            chatRecyclerAdapter = new ChatRecyclerAdapter();
            chatRecyclerAdapter.setMyUserId(PersistentInfo.getInstance().getMeUserId());
        }
        recyclerView.setAdapter(chatRecyclerAdapter);
        startListPaging(progressBar);

    }

    private void goToConcreteChat(int position) {
        long chatId = chatRecyclerAdapter.getItem(position).getChat().id;
        MessagesFragment messagesFragment = MessagesFragment.newInstance(chatId);
        startFragment(messagesFragment);
    }

    private void startFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    private void initToolbar() {
        if (getSupportActivity() == null || getSupportActivity().getSupportActionBar() == null) {
            return;
        }
        Toolbar toolbar = ((MainActivity) getSupportActivity()).getToolbar();
        toolbar.setNavigationOnClickListener(((MainActivity) getSupportActivity()).getCommonNavigationOnClickListener());
        getSupportActivity().getSupportActionBar().setTitle(getResources().getString(R.string.chat_list_toolbar_title));
        getSupportActivity().getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity) getSupportActivity()).drawerToggleSyncState();
    }

    private void startListPaging(ProgressBar progressBar) {
        if (chatRecyclerAdapter.isAllItemsLoaded()) {
            return;
        }
        listPagingSubscription = PaginationTool
                .paging(recyclerView, offset -> chatsInteract.getNextDataPortionInList(offset, LIMIT))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<List<ChatItem>>() {
                    @Override
                    public void onNext(List<ChatItem> chatItems) {
                        if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
                            progressBar.setVisibility(View.GONE);
                        }
                        chatRecyclerAdapter.addNewItems(chatItems);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        if (listPagingSubscription != null && !listPagingSubscription.isUnsubscribed()) {
            listPagingSubscription.unsubscribe();
        }
        // for memory leak prevention (RecycleView is not unsubscibed from adapter DataObserver)
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        super.onDestroyView();
    }

}
