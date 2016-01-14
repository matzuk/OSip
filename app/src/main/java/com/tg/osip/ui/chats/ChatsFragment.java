package com.tg.osip.ui.chats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.business.PersistentInfo;
import com.tg.osip.business.chats.ChatsInteract;
import com.tg.osip.tdclient.TGProxyI;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.ui.activities.MainActivity;
import com.tg.osip.ui.general.BaseFragment;
import com.tg.osip.ui.general.views.RecyclerItemClickListener;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

/**
 * Fragment show list of all chats
 *
 * @author e.matsyuk
 */
public class ChatsFragment extends BaseFragment implements ChatsContract.View {

    @Inject
    ChatsPresenter chatsPresenter;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ApplicationSIP.get().applicationComponent().plus(new ChatsModule()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fmt_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        init(view);
        initToolbar();
    }

    private void init(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.RecyclerView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        // init LayoutManager
        GridLayoutManager recyclerViewLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerViewLayoutManager.supportsPredictiveItemAnimations();
        // recyclerView setting
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), (view1, position) -> chatsPresenter.clickChatItem(getSupportActivity(), position))
        );
        chatsPresenter.bindView(this);
        chatsPresenter.loadChatsList(recyclerView);
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

    @Override
    public void onDestroyView() {
        chatsPresenter.onDestroy();
        // for memory leak prevention (RecycleView is not unsubscibed from adapter DataObserver)
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        super.onDestroyView();
    }

    @Override
    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Subcomponent(modules = ChatsModule.class)
    public interface ChatsComponent {
        void inject(ChatsFragment chatsFragment);
    }

    @Module
    public static class ChatsModule {

        @Provides
        @NonNull
        public ChatsPresenter provideChatsPresenter(@NonNull ChatsInteract chatsInteract, @NonNull PersistentInfo persistentInfo) {
            return new ChatsPresenter(chatsInteract, persistentInfo);
        }

        @Provides
        @NonNull
        public ChatsInteract provideChatsInteract(@NonNull TGProxyI tgProxyI, @NonNull FileDownloaderManager fileDownloaderManager) {
            return new ChatsInteract(tgProxyI, fileDownloaderManager);
        }
    }

}
