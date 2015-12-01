package com.tg.osip.business.messages;

import android.support.v4.util.Pair;

import com.tg.osip.business.models.MessageAdapterModel;
import com.tg.osip.business.models.MessageItem;
import com.tg.osip.business.models.PhotoItem;
import com.tg.osip.business.models.UserItem;
import com.tg.osip.business.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.TGProxy;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

/**
 * Interactor for presenter layout (MessagesFragment)
 *
 * @author e.matsyuk
 */
public class MessagesInteract {

    /**
     *
     * @param chatId chat id
     * @return Pair of TdApi.Chat(first) and MessageAdapterModel(second)
     */
    public Observable<Pair<TdApi.Chat, MessageAdapterModel>> getFirstChatData(long chatId) {
        return TGProxy.getInstance().sendTD(new TdApi.GetChat(chatId), TdApi.Chat.class)
                .concatMap(chat -> {
                    List<MessageItem> messages = new ArrayList<>(1);
                    messages.add(new MessageItem(chat.topMessage));
                    // download photo content from messages in another Stream
                    getPhotoTypeMediumDownloadingObservable(messages).subscribe();
                    // download users info for messages adapter in this Stream
                    return Observable.zip(Observable.just(messages), getUsersDownloadingObservable(messages), Observable.just(chat),
                            (messages1, integerUserItemMap, chat1) -> new Pair<>(chat1, new MessageAdapterModel(messages1, integerUserItemMap)));
                });
    }

    public Observable<MessageAdapterModel> getNextDataPortionInList(int offset, int limit, long chatId, int topMessageId) {
        return TGProxy.getInstance().sendTD(new TdApi.GetChatHistory(chatId, topMessageId, offset, limit), TdApi.Messages.class)
                .map(messages -> {
                    List<MessageItem> messageItemList = new ArrayList<>();
                    for (TdApi.Message message : messages.messages) {
                        messageItemList.add(new MessageItem(message));
                    }
                    return messageItemList;
                })
                .concatMap(messages -> {
                    // download photo content from messages in another Stream
                    getPhotoTypeMediumDownloadingObservable(messages).subscribe();
                    // download users info for messages adapter in this Stream
                    return Observable.zip(getUsersDownloadingObservable(messages), Observable.just(messages), (integerUserItemMap, messageItems) ->
                            new MessageAdapterModel(messageItems, integerUserItemMap));
                });
    }

    private Observable<Map<Integer, UserItem>> getUsersDownloadingObservable(List<MessageItem> mainListItems) {
        return Observable.from(mainListItems)
                .map(message -> message.getMessage().fromId)
                .distinct()
                .concatMap(integer -> TGProxy.getInstance().sendTD(new TdApi.GetUser(integer), TdApi.User.class))
                .map(UserItem::new)
                .toList()
                .doOnNext(userChatListItems -> FileDownloaderManager.getInstance().startFileListDownloading(userChatListItems))
                .map(users -> {
                    Map<Integer, UserItem> map = new HashMap<>();
                    for (UserItem user : users) {
                        map.put(user.getUser().id, user);
                    }
                    return map;
                });
    }

    private Observable<List<PhotoItem>> getPhotoTypeMediumDownloadingObservable(List<MessageItem> mainListItems) {
        return Observable.from(mainListItems)
                .filter(MessageItem::isPhotoMessage)
                .map(MessageItem::getPhotoItemMedium)
                .toList()
                .doOnNext(photoItems -> FileDownloaderManager.getInstance().startFileListDownloading(photoItems));
    }

}
