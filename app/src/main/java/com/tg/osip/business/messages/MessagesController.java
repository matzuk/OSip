package com.tg.osip.business.messages;

import android.view.View;
import android.widget.ProgressBar;

import com.tg.osip.business.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.ui.messages.MessagesRecyclerAdapter;
import com.tg.osip.utils.common.BackgroundExecutor;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.ui.views.auto_loading.AutoLoadingRecyclerView;
import com.tg.osip.ui.views.auto_loading.ILoading;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Controller for Chat screen (MessagesFragment)
 *
 * @author e.matsyuk
 */
public class MessagesController {

    private WeakReference<ProgressBar> progressBarWeakReference;
    private int topMessageId;
    private Subscription firstStartRecyclerViewSubscription;
    private MessagesRecyclerAdapter messagesRecyclerAdapter;

    private ILoading<TdApi.Message> getILoading(long chatId, int topMessageId) {
        return offsetAndLimit -> TGProxy.getInstance().sendTD(new TdApi.GetChatHistory(chatId, topMessageId, offsetAndLimit.getOffset(), offsetAndLimit.getLimit()), TdApi.Messages.class)
                .map(messages -> {
                    TdApi.Message messageMas[] = messages.messages;
                    return (List<TdApi.Message>) new ArrayList<>(Arrays.asList(messageMas));
                })
                .concatMap(this::getUsersDownloadingProxyObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext((messageList) -> {
                    if (progressBarWeakReference != null && progressBarWeakReference.get() != null) {
                        progressBarWeakReference.get().setVisibility(View.GONE);
                    }
                });
    }

    private Observable<List<TdApi.Message>> getUsersDownloadingProxyObservable(List<TdApi.Message> mainListItems) {
        return Observable.create(subscriber -> getUsersDownloadingObservable(mainListItems)
                .subscribe(new Subscriber<Map<Integer, UserMessageListItem>>() {
                    @Override
                    public void onCompleted() {
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        subscriber.onError(e);
                    }

                    @Override
                    public void onNext(Map<Integer, UserMessageListItem> map) {
                        if (messagesRecyclerAdapter != null) {
                            messagesRecyclerAdapter.setChatUsers(map);
                        }
                        subscriber.onNext(mainListItems);
                    }
                }));
    }

    private Observable<Map<Integer, UserMessageListItem>> getUsersDownloadingObservable(List<TdApi.Message> mainListItems) {
        return Observable.from(mainListItems)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .map(message -> message.fromId)
                .distinct()
                .concatMap(integer -> TGProxy.getInstance().sendTD(new TdApi.GetUser(integer), TdApi.User.class))
                .map(UserMessageListItem::new)
                .toList()
                .doOnNext(userChatListItems -> FileDownloaderManager.getInstance().startFileDownloading(userChatListItems))
                .map(users -> {
                    Map<Integer, UserMessageListItem> map = new HashMap<>();
                    for (UserMessageListItem user : users) {
                        map.put(user.getUser().id, user);
                    }
                    return map;
                });
    }

    /**
     * This method is called first!
     * load fresh top message id and start RecyclerView for first one
    */
    public void firstStartRecyclerView(AutoLoadingRecyclerView<TdApi.Message> autoLoadingRecyclerView, MessagesRecyclerAdapter messagesRecyclerAdapter, long chatId, ProgressBar progressBar) {
        this.messagesRecyclerAdapter = messagesRecyclerAdapter;
        progressBarWeakReference = new WeakReference<>(progressBar);
        firstStartRecyclerViewSubscription = TGProxy.getInstance()
                .sendTD(new TdApi.GetMe(), TdApi.User.class)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(user -> messagesRecyclerAdapter.setMyUserId(user.id))
                .concatMap(user -> TGProxy.getInstance().sendTD(new TdApi.GetChat(chatId), TdApi.Chat.class))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TdApi.Chat>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(TdApi.Chat chat) {
                        Logger.debug("user data loaded, recyclerview is next");
                        topMessageId = chat.topMessage.id;
                        messagesRecyclerAdapter.setLastChatReadOutboxId(chat.lastReadOutboxMessageId);
                        messagesRecyclerAdapter.addNewItem(chat.topMessage);
                        messagesRecyclerAdapter.notifyItemInserted(0);
                        autoLoadingRecyclerView.setLoadingObservable(getILoading(chatId, topMessageId));
                        autoLoadingRecyclerView.startLoading();
                    }
                });
    }

    /**
     * set parameters to RecyclerView after screen reorientation
     * so we should not load topMessageId for ILoading of RecyclerView
     */
    public void startRecyclerView(AutoLoadingRecyclerView<TdApi.Message> autoLoadingRecyclerView, long chatId) {
        autoLoadingRecyclerView.setLoadingObservable(getILoading(chatId, topMessageId));
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
