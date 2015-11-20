package com.tg.osip.ui.messages;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.tg.osip.R;
import com.tg.osip.business.messages.MessagesController;
import com.tg.osip.ui.activities.MainActivity;
import com.tg.osip.ui.general.views.auto_loading.AutoLoadingRecyclerView;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class MessagesFragment extends Fragment {

    public static final String CHAT_ID = "chat_id";
    private static final int LIMIT = 50;

    private AutoLoadingRecyclerView<TdApi.Message> recyclerView;
    private Toolbar toolbar;
    private MessagesController messagesController;

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
        recyclerView = (AutoLoadingRecyclerView) view.findViewById(R.id.RecyclerView);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        toolbar = ((MainActivity)getSupportActivity()).getToolbar();
        // init LayoutManager
        GridLayoutManager recyclerViewLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerViewLayoutManager.supportsPredictiveItemAnimations();
        recyclerViewLayoutManager.setReverseLayout(true);
        // recyclerView setting
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setLimit(LIMIT);
        // for first start
        if (messagesController == null) {
            // start progressbar
            progressBar.setVisibility(View.VISIBLE);
            // init MessagesController
            messagesController = new MessagesController(chatId);
        }
        messagesController.setProgressBar(progressBar);
        messagesController.setRecyclerView(recyclerView);
        messagesController.setToolbar(getContext(), toolbar);
        messagesController.loadData();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // start loading after reorientation
        if (savedInstanceState != null) {
            messagesController.setRecyclerView(recyclerView);
            messagesController.restoreDataToViews();
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

    private AppCompatActivity getSupportActivity() {
        return (AppCompatActivity)getActivity();
    }

    @Override
    public void onDestroyView() {
        if (messagesController != null) {
            messagesController.onDestroy();
        }
        super.onDestroyView();
    }

}
