package com.tg.osip.business.chats;

import android.view.View;
import android.widget.ProgressBar;

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

    private WeakReference<ProgressBar> progressBarWeakReference;
    private Subscription firstStartRecyclerViewSubscription;

    private ILoading<ChatListItem> getILoading() {
        return offsetAndLimit -> TGProxy.getInstance().sendTD(new TdApi.GetChats(offsetAndLimit.getOffset(), offsetAndLimit.getLimit()), TdApi.Chats.class)
                .map(chats -> {
                    TdApi.Chat chatsMas[] = chats.chats;
                    return new ArrayList<>(Arrays.asList(chatsMas));
                })
                .concatMap(Observable::from)
                .map(ChatListItem::new)
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
     * This method is called first!
     * load fresh my user.id id and start RecyclerView for first one
     */
    public void firstStartRecyclerView(AutoLoadingRecyclerView<ChatListItem> autoLoadingRecyclerView, ChatRecyclerAdapter chatRecyclerAdapter, ProgressBar progressBar) {
        progressBarWeakReference = new WeakReference<>(progressBar);
        firstStartRecyclerViewSubscription = TGProxy.getInstance()
                .sendTD(new TdApi.GetMe(), TdApi.User.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TdApi.User>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(TdApi.User user) {
                        Logger.debug("user data loaded, recyclerview is next");
                        chatRecyclerAdapter.setMyUserId(user.id);
                        autoLoadingRecyclerView.setLoadingObservable(getILoading());
                        autoLoadingRecyclerView.startLoading();
                    }
                });
    }

    /**
     * set parameters to RecyclerView after screen reorientation
     * so we should not load my user.id for ILoading of RecyclerView
     */
    public void startRecyclerView(AutoLoadingRecyclerView<ChatListItem> autoLoadingRecyclerView) {
        autoLoadingRecyclerView.setLoadingObservable(getILoading());
        autoLoadingRecyclerView.startLoading();
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
