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
import com.tg.osip.utils.log.Logger;
import com.tg.osip.utils.ui.RecyclerItemClickListener;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Arrays;

import rx.Subscriber;

/**
 * Fragment show list of all chats
 *
 * @author e.matsyuk
 */
public class MainFragment extends Fragment {

    private static final int LIMIT = 50;

    private AutoLoadingRecyclerView<TdApi.Chat> recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fmt_main, container, false);
        init(rootView);
        initToolbar(rootView);
        return rootView;
    }

    private void init(View view) {
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
        recyclerView.setLoadingObservable(
                offsetAndLimit -> TGProxy.getInstance().sendTD(new TdApi.GetChats(offsetAndLimit.getOffset(), offsetAndLimit.getLimit()), TdApi.Chats.class)
                        .map(chats -> {
                            TdApi.Chat chatsMas[] = chats.chats;
                            return new ArrayList<>(Arrays.asList(chatsMas));
                        })
        );
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), (view1, position) -> goToConcreteChat(position))
        );
    }

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
