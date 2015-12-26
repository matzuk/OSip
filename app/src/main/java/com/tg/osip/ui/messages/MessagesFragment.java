package com.tg.osip.ui.messages;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.business.PersistentInfo;
import com.tg.osip.business.messages.MessagesInteract;
import com.tg.osip.tdclient.TGProxyI;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.ui.activities.MainActivity;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

/**
 * @author e.matsyuk
 */
public class MessagesFragment extends Fragment implements MessagesContract.View {

    public static final String CHAT_ID = "chat_id";

    @Inject
    MessagesPresenter messagesPresenter;

    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private View customToolbarView;

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
        setRetainInstance(true);
        ApplicationSIP.get().applicationComponent().plus(new MessagesModule()).inject(this);
        chatId = getArguments().getLong(CHAT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fmt_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initEmptyToolbar();
        init(view);
    }

    private void init(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.RecyclerView);
        // init LayoutManager
        GridLayoutManager recyclerViewLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerViewLayoutManager.supportsPredictiveItemAnimations();
        recyclerViewLayoutManager.setReverseLayout(true);
        // recyclerView setting
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        messagesPresenter.bindView(this);
        messagesPresenter.loadViewsData(getSupportActivity(), recyclerView, chatId);
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

    private AppCompatActivity getSupportActivity() {
        return (AppCompatActivity)getActivity();
    }

    @Override
    public void updateToolBar(View toolbarView) {
        this.customToolbarView = toolbarView;
        toolbar.addView(toolbarView);
    }

    @Override
    public void onDestroyView() {
        messagesPresenter.onDestroy();
        // for memory leak prevention (RecycleView is not unsubscibed from adapter DataObserver)
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        if (toolbar != null && customToolbarView != null) {
            toolbar.removeView(customToolbarView);
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
        public MessagesPresenter provideMessagesPresenter(@NonNull MessagesInteract messagesInteract, @NonNull PersistentInfo persistentInfo) {
            return new MessagesPresenter(messagesInteract, persistentInfo);
        }

        @Provides
        @NonNull
        public MessagesInteract provideMessagesInteract(@NonNull TGProxyI tgProxyI, @NonNull FileDownloaderManager fileDownloaderManager) {
            return new MessagesInteract(tgProxyI, fileDownloaderManager);
        }
    }

}
