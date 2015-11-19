package com.tg.osip.business.chats;

import android.view.View;
import android.widget.ProgressBar;

import com.tg.osip.business.models.ChatItem;
import com.tg.osip.business.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.ui.chats.ChatRecyclerAdapter;
import com.tg.osip.ui.views.auto_loading.AutoLoadingRecyclerView;
import com.tg.osip.ui.views.auto_loading.ILoading;
import com.tg.osip.utils.log.Logger;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Controller for Chats screen (ChatsFragment)
 *
 * @author e.matsyuk
 */
public class ChatsController {

    // views from fragment
    private WeakReference<ProgressBar> progressBarWeakReference;
    private WeakReference<AutoLoadingRecyclerView<ChatItem>> recyclerViewWeakReference;
    // adapters
    private ChatRecyclerAdapter chatRecyclerAdapter;
    // subscriptions
    private Subscription firstStartRecyclerViewSubscription;

    public ChatsController() {
        initAdapters();
    }

    private void initAdapters() {
        chatRecyclerAdapter = new ChatRecyclerAdapter();
        chatRecyclerAdapter.setHasStableIds(true);
    }

    public void setProgressBar(ProgressBar progressBar) {
        progressBarWeakReference = new WeakReference<>(progressBar);
    }

    public void setRecyclerView(AutoLoadingRecyclerView<ChatItem> autoLoadingRecyclerView) {
        recyclerViewWeakReference = new WeakReference<>(autoLoadingRecyclerView);
        // set adapter to new or recreated recyclerView
        autoLoadingRecyclerView.setAdapter(chatRecyclerAdapter);
    }

    /**
     * This method is called first!
     * load fresh my user.id id and start RecyclerView for first one
     */
    public void loadData() {
        if (firstStartRecyclerViewSubscription != null) {
            return;
        }
        firstStartRecyclerViewSubscription = TGProxy.getInstance()
                .sendTD(new TdApi.GetMe(), TdApi.User.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TdApi.User>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(TdApi.User user) {
                        Logger.debug("user data loaded, recyclerview is next");
                        successLoadData(user);
                    }
                });
    }

    private void successLoadData(TdApi.User user) {
        chatRecyclerAdapter.setMyUserId(user.id);
        if (recyclerViewWeakReference != null && recyclerViewWeakReference.get() != null) {
            AutoLoadingRecyclerView<ChatItem> autoLoadingRecyclerView = recyclerViewWeakReference.get();
            autoLoadingRecyclerView.setLoadingObservable(getILoading());
            autoLoadingRecyclerView.startLoading();
        }
    }

    private ILoading<ChatItem> getILoading() {
        return offsetAndLimit -> TGProxy.getInstance().sendTD(new TdApi.GetChats(offsetAndLimit.getOffset(), offsetAndLimit.getLimit()), TdApi.Chats.class)
                .map(chats -> {
                    TdApi.Chat chatsMas[] = chats.chats;
                    return new ArrayList<>(Arrays.asList(chatsMas));
                })
                .concatMap(Observable::from)
                .map(ChatItem::new)
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(mainListItems -> {
                    if (progressBarWeakReference != null && progressBarWeakReference.get() != null) {
                        progressBarWeakReference.get().setVisibility(View.GONE);
                    }
                    FileDownloaderManager.getInstance().startFileListDownloading(mainListItems);
                });
    }

    /**
     * restore data to recreated views
     */
    public void restoreDataToViews() {
        if (recyclerViewWeakReference != null && recyclerViewWeakReference.get() != null) {
            AutoLoadingRecyclerView<ChatItem> autoLoadingRecyclerView = recyclerViewWeakReference.get();
            autoLoadingRecyclerView.setLoadingObservable(getILoading());
            autoLoadingRecyclerView.startLoading();
        }
    }

    /**
     * Required method for memory leaks preventing
     */
    public void onDestroy() {
        if (firstStartRecyclerViewSubscription != null && !firstStartRecyclerViewSubscription.isUnsubscribed()) {
            firstStartRecyclerViewSubscription.unsubscribe();
        }
    }

}
