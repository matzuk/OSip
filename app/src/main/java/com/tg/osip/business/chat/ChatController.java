package com.tg.osip.business.chat;

import com.tg.osip.business.main.MainListItem;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.ui.chat.ChatRecyclerAdapter;
import com.tg.osip.ui.main_screen.MainRecyclerAdapter;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.utils.ui.auto_loading.AutoLoadingRecyclerView;
import com.tg.osip.utils.ui.auto_loading.ILoading;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Arrays;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Controller for Chat screen (ChatFragment)
 *
 * @author e.matsyuk
 */
public class ChatController {

    private int topMessageId;

    ILoading<TdApi.Message> getILoading(long chatId, int topMessageId) {
        return offsetAndLimit -> TGProxy.getInstance().sendTD(new TdApi.GetChatHistory(chatId, topMessageId, offsetAndLimit.getOffset(), offsetAndLimit.getLimit()), TdApi.Messages.class)
                .map(messages -> {
                    TdApi.Message messageMas[] = messages.messages;
                    return new ArrayList<>(Arrays.asList(messageMas));
                });
    }

    /**
     * load fresh top message id and start RecyclerView for first one
    */
    public void firstStartRecyclerView(AutoLoadingRecyclerView<TdApi.Message> autoLoadingRecyclerView, long chatId) {
        TGProxy.getInstance().sendTD(new TdApi.GetChat(chatId), TdApi.Chat.class)
                .observeOn(AndroidSchedulers.mainThread())
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
                        Logger.debug("user data loaded, recyclerview is next");
                        topMessageId = chat.topMessage.id;
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


}
