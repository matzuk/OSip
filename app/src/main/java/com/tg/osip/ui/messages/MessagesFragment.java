package com.tg.osip.ui.messages;

import android.content.Intent;
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
import com.tg.osip.business.models.MessageItem;
import com.tg.osip.business.models.PhotoItem;
import com.tg.osip.ui.activities.MainActivity;
import com.tg.osip.ui.activities.PhotoMediaActivity;
import com.tg.osip.ui.general.views.auto_loading.AutoLoadingRecyclerView;
import com.tg.osip.utils.log.Logger;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.Serializable;
import java.util.List;

/**
 * @author e.matsyuk
 */
public class MessagesFragment extends Fragment {

    public static final String CHAT_ID = "chat_id";
    private static final int LIMIT = 50;

    private AutoLoadingRecyclerView<MessageItem> recyclerView;
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
        messagesController.setRecyclerView(recyclerView, onMessageClickListener);
        messagesController.setToolbar(getContext(), toolbar);
        messagesController.loadData();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // start loading after reorientation
        if (savedInstanceState != null) {
            messagesController.setRecyclerView(recyclerView, onMessageClickListener);
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

    private OnMessageClickListener onMessageClickListener = new OnMessageClickListener() {
        @Override
        public void onPhotoMessageClick(List<PhotoItem> photoMItemList) {
            Intent intent = new Intent(getActivity(), PhotoMediaActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(PhotoMediaActivity.PHOTO_M, (Serializable) photoMItemList);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    };

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
