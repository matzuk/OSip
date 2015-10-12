package com.tg.osip.ui.chat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tg.osip.R;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.utils.ui.auto_loading.AutoLoadingRecyclerView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Arrays;

import rx.Subscriber;

/**
 * @author e.matsyuk
 */
public class ChatFragment extends Fragment {

    public static final String CHAT_ID = "chat_id";

    private Toolbar toolbar;
    private GridLayoutManager recyclerViewLayoutManager;
    private AutoLoadingRecyclerView<TdApi.Message> recyclerView;
    private ChatRecyclerAdapter chatRecyclerAdapter;

    private long chatId;
    private int topMessageId;

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
        init(rootView);
        initToolbar(rootView);
        return rootView;
    }

    private void init(View view) {
        recyclerView = (AutoLoadingRecyclerView) view.findViewById(R.id.RecyclerView);
        recyclerViewLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerViewLayoutManager.supportsPredictiveItemAnimations();
        recyclerViewLayoutManager.setReverseLayout(true);
        chatRecyclerAdapter = new ChatRecyclerAdapter();
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setLimit(50);
        recyclerView.setAdapter(chatRecyclerAdapter);
        recyclerView.setLoadingObservable(offsetAndLimit -> TGProxy.getInstance().sendTD(new TdApi.GetChatHistory(chatId, topMessageId, offsetAndLimit.getOffset(), offsetAndLimit.getLimit()), TdApi.Messages.class)
                .map(messages -> {
                    TdApi.Message messageMas[] = messages.messages;
                    return new ArrayList<>(Arrays.asList(messageMas));
                }));

        TGProxy.getInstance().sendTD(new TdApi.GetChat(chatId), TdApi.Chat.class)
                .subscribe(new Subscriber<TdApi.Chat>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(TdApi.Chat chat) {
                        topMessageId = chat.topMessage.id;
                        recyclerView.startLoading();
                    }
                });

    }

    private void initToolbar(View rootView) {
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
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
        recyclerView.onDestroy();
        super.onDestroyView();
    }

}
