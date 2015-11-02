package com.tg.osip.ui.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tg.osip.R;
import com.tg.osip.business.FileDownloaderManager;
import com.tg.osip.business.main.MainController;
import com.tg.osip.tdclient.models.MainListItem;
import com.tg.osip.ui.chat.ChatFragment;
import com.tg.osip.ui.general.BaseFragment;
import com.tg.osip.utils.ui.auto_loading.AutoLoadingRecyclerView;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.utils.ui.PreLoader;
import com.tg.osip.utils.ui.RecyclerItemClickListener;

/**
 * Fragment show list of all chats
 *
 * @author e.matsyuk
 */
public class MainFragment extends BaseFragment {

    private static final int LIMIT = 50;

    private AutoLoadingRecyclerView<MainListItem> recyclerView;
    private MainRecyclerAdapter mainRecyclerAdapter;
    private MainController mainController;
    // temp
    private PreLoader preLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fmt_main, container, false);
        init(rootView);
        initToolbar(rootView);
        return rootView;
    }

    private void init(View view) {
        preLoader = (PreLoader) view.findViewById(R.id.pro_loader);
        recyclerView = (AutoLoadingRecyclerView) view.findViewById(R.id.RecyclerView);
        // init Controller
        mainController = new MainController();
        // init LayoutManager
        GridLayoutManager recyclerViewLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerViewLayoutManager.supportsPredictiveItemAnimations();
        // init ChatRecyclerAdapter
        mainRecyclerAdapter = new MainRecyclerAdapter();
        mainRecyclerAdapter.setHasStableIds(true);
        // recyclerView setting
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setLimit(LIMIT);
        recyclerView.setAdapter(mainRecyclerAdapter);
        recyclerView.setLoadingObservable(mainController.getILoading(preLoader));
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), (view1, position) -> goToConcreteChat(position))
        );
        // start FileDownloaderManager
        FileDownloaderManager.getInstance().subscribeToUpdateChannel();
    }

    private void goToConcreteChat(int position) {
        long chatId = recyclerView.getAdapter().getItem(position).getApiChat().id;
        ChatFragment chatFragment = ChatFragment.newInstance(chatId);
        startFragment(chatFragment);
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
    public void onResume() {
        super.onResume();
        Logger.debug("start loading List");
        mainController.startRecyclerView(recyclerView, mainRecyclerAdapter);
    }

}
