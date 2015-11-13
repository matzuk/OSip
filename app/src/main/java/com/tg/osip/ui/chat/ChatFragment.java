package com.tg.osip.ui.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tg.osip.R;
import com.tg.osip.business.chat.ChatController;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.ui.views.auto_loading.AutoLoadingRecyclerView;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class ChatFragment extends Fragment {

    public static final String CHAT_ID = "chat_id";
    private static final int LIMIT = 50;

    private AutoLoadingRecyclerView<TdApi.Message> recyclerView;
    private ChatRecyclerAdapter chatRecyclerAdapter;
    private ChatController chatController;

    private long chatId;

    public static ChatFragment newInstance(long chatId) {
        ChatFragment f = new ChatFragment();
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
        View rootView = inflater.inflate(R.layout.fmt_chat, container, false);
        setRetainInstance(true);
        init(rootView);
        initToolbar(rootView);
        return rootView;
    }

    private void init(View view) {
        recyclerView = (AutoLoadingRecyclerView) view.findViewById(R.id.RecyclerView);
        // init LayoutManager
        GridLayoutManager recyclerViewLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerViewLayoutManager.supportsPredictiveItemAnimations();
        recyclerViewLayoutManager.setReverseLayout(true);
        // recyclerView setting
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setLimit(LIMIT);
        // for first start
        if (chatRecyclerAdapter == null || chatController == null) {
            // init ChatController
            chatController = new ChatController();
            // init ChatRecyclerAdapter
            chatRecyclerAdapter = new ChatRecyclerAdapter();
            chatRecyclerAdapter.setHasStableIds(true);
            recyclerView.setAdapter(chatRecyclerAdapter);
            Logger.debug("start loading List");
            chatController.firstStartRecyclerView(recyclerView, chatRecyclerAdapter, chatId);
        } else {
            recyclerView.setAdapter(chatRecyclerAdapter);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // start loading after reorientation
        if (savedInstanceState != null) {
            chatController.startRecyclerView(recyclerView, chatId);
        }
    }

    private void initToolbar(View rootView) {
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (getActivity() != null) {
            getSupportActivity().setSupportActionBar(toolbar);
            getSupportActivity().getSupportActionBar().setTitle("");
            getSupportActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActivity().getSupportActionBar().show();
        }
    }

    private AppCompatActivity getSupportActivity() {
        return (AppCompatActivity)getActivity();
    }

    @Override
    public void onDestroyView() {
        if (chatController != null) {
            chatController.onDestroy();
        }
        super.onDestroyView();
    }

}
