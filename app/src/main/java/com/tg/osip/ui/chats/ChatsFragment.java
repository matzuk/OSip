package com.tg.osip.ui.chats;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;

import com.tg.osip.R;
import com.tg.osip.business.main.ChatsController;
import com.tg.osip.business.main.ChatListItem;
import com.tg.osip.ui.messages.MessagesFragment;
import com.tg.osip.ui.general.BaseFragment;
import com.tg.osip.ui.views.auto_loading.AutoLoadingRecyclerView;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.ui.views.RecyclerItemClickListener;

/**
 * Fragment show list of all chats
 *
 * @author e.matsyuk
 */
public class ChatsFragment extends BaseFragment {

    private static final int LIMIT = 50;

    private AutoLoadingRecyclerView<ChatListItem> recyclerView;
    private ChatsController chatsController;
    private ChatRecyclerAdapter chatRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fmt_main, container, false);
        setRetainInstance(true);
        init(rootView);
        initToolbar(rootView);
        return rootView;
    }

    private void init(View view) {
        recyclerView = (AutoLoadingRecyclerView) view.findViewById(R.id.RecyclerView);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        // init LayoutManager
        GridLayoutManager recyclerViewLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerViewLayoutManager.supportsPredictiveItemAnimations();
        // recyclerView setting
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setLimit(LIMIT);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), (view1, position) -> goToConcreteChat(position))
        );
        // first start
        if (chatsController == null || chatRecyclerAdapter == null) {
            // start progressbar
            progressBar.setVisibility(View.VISIBLE);
            // init ChatRecyclerAdapter
            chatRecyclerAdapter = new ChatRecyclerAdapter();
            chatRecyclerAdapter.setHasStableIds(true);
            recyclerView.setAdapter(chatRecyclerAdapter);
            // init Controller
            chatsController = new ChatsController();
            // for more smoother RecyclerView appearing
            recyclerView.setVisibility(View.GONE);
            ViewTreeObserver textViewTreeObserver=recyclerView.getViewTreeObserver();
            textViewTreeObserver.addOnGlobalLayoutListener(() -> recyclerView.setVisibility(View.VISIBLE));
            Logger.debug("start loading List");
            chatsController.firstStartRecyclerView(recyclerView, chatRecyclerAdapter, progressBar);
        } else {
            recyclerView.setAdapter(chatRecyclerAdapter);
        }

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // start loading after reorientation
        if (savedInstanceState != null) {
            chatsController.startRecyclerView(recyclerView);
        }
    }

    private void goToConcreteChat(int position) {
        long chatId = recyclerView.getAdapter().getItem(position).getApiChat().id;
        MessagesFragment messagesFragment = MessagesFragment.newInstance(chatId);
        startFragment(messagesFragment);
    }

    private void startFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    private void initToolbar(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(getResources().getString(R.string.chat_list_toolbar_title));
        getSupportActivity().getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActivity().getSupportActionBar().show();
    }

    @Override
    public void onDestroyView() {
        if (chatsController != null) {
            chatsController.onDestroy();
        }
        super.onDestroyView();
    }

}
