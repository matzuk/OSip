package com.tg.osip.business.chat;

import android.view.View;
import android.widget.ProgressBar;

import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.ui.chat.ChatRecyclerAdapter;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.ui.views.auto_loading.AutoLoadingRecyclerView;
import com.tg.osip.ui.views.auto_loading.ILoading;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Controller for Chat screen (ChatFragment)
 *
 * @author e.matsyuk
 */
public class ChatController {

    private WeakReference<ProgressBar> progressBarWeakReference;
    private int topMessageId;
    private Subscription firstStartRecyclerViewSubscription;

    ILoading<TdApi.Message> getILoading(long chatId, int topMessageId) {
        return offsetAndLimit -> TGProxy.getInstance().sendTD(new TdApi.GetChatHistory(chatId, topMessageId, offsetAndLimit.getOffset(), offsetAndLimit.getLimit()), TdApi.Messages.class)
                .map(messages -> {
                    TdApi.Message messageMas[] = messages.messages;
                    List<TdApi.Message> messageList = new ArrayList<>(Arrays.asList(messageMas));
                    return messageList;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext((messageList) -> {
                    if (progressBarWeakReference != null && progressBarWeakReference.get() != null) {
                        progressBarWeakReference.get().setVisibility(View.GONE);
                    }
                });
    }

    /**
     * load fresh top message id and start RecyclerView for first one
    */
    public void firstStartRecyclerView(AutoLoadingRecyclerView<TdApi.Message> autoLoadingRecyclerView, ChatRecyclerAdapter chatRecyclerAdapter, long chatId, ProgressBar progressBar) {
        progressBarWeakReference = new WeakReference<>(progressBar);
        firstStartRecyclerViewSubscription = TGProxy.getInstance()
                .sendTD(new TdApi.GetMe(), TdApi.User.class)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(user -> chatRecyclerAdapter.setMyUserId(user.id))
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
                        chatRecyclerAdapter.setLastChatReadOutboxId(chat.lastReadOutboxMessageId);
                        chatRecyclerAdapter.addNewItem(chat.topMessage);
                        chatRecyclerAdapter.notifyItemInserted(0);
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
