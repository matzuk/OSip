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
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.ui.chat.ChatFragment;
import com.tg.osip.ui.chat.ChatRecyclerAdapter;
import com.tg.osip.ui.views.auto_loading.AutoLoadingRecyclerView;
import com.tg.osip.ui.views.auto_loading.ILoading;
import com.tg.osip.ui.views.auto_loading.OffsetAndLimit;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.utils.ui.PreLoader;
import com.tg.osip.utils.ui.RecyclerItemClickListener;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Fragment show list of all chats
 *
 * @author e.matsyuk
 */
public class MainFragment extends Fragment {

    private static final int LIMIT = 50;

    private AutoLoadingRecyclerView<TdApi.Chat> recyclerView;
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
        // init LayoutManager
        GridLayoutManager recyclerViewLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerViewLayoutManager.supportsPredictiveItemAnimations();
        // init ChatRecyclerAdapter
        MainRecyclerAdapter mainRecyclerAdapter = new MainRecyclerAdapter();
        mainRecyclerAdapter.setHasStableIds(true);
        // recyclerView setting
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setLimit(LIMIT);
        recyclerView.setAdapter(mainRecyclerAdapter);
        recyclerView.setLoadingObservable(loading);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), (view1, position) -> goToConcreteChat(position))
        );
    }

    private ILoading<TdApi.Chat> loading = new ILoading<TdApi.Chat>() {
        @Override
        public Observable<List<TdApi.Chat>> getLoadingObservable(OffsetAndLimit offsetAndLimit) {
            return TGProxy.getInstance().sendTD(new TdApi.GetChats(offsetAndLimit.getOffset(), offsetAndLimit.getLimit()), TdApi.Chats.class)
                    .map(chats -> {
                        TdApi.Chat chatsMas[] = chats.chats;
                        return new ArrayList<>(Arrays.asList(chatsMas));
                    });
        }
        @Override
        public void startLoadData() {

        }
        @Override
        public void endLoadData() {
            preLoader.setVisibility(View.GONE);
        }
    };

    private void goToConcreteChat(int position) {
        long chatId = recyclerView.getAdapter().getItem(position).id;
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
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        recyclerView.startLoading();
    }

}
