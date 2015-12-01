package com.tg.osip.business.chats;

import com.tg.osip.business.models.ChatItem;
import com.tg.osip.business.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.TGProxy;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;

/**
 * Interactor for presenter layout (ChatsFragment)
 *
 * @author e.matsyuk
 */
public class ChatsInteract {

    public Observable<List<ChatItem>> getNextDataPortionInList(int offset, int limit) {
        return TGProxy.getInstance().sendTD(new TdApi.GetChats(offset, limit), TdApi.Chats.class)
                .map(chats -> {
                    TdApi.Chat chatsMas[] = chats.chats;
                    return new ArrayList<>(Arrays.asList(chatsMas));
                })
                .concatMap(Observable::from)
                .map(ChatItem::new)
                .toList()
                .doOnNext(mainListItems -> {
                    // send photo items to FileDownloaderManager
                    FileDownloaderManager.getInstance().startFileListDownloading(mainListItems);
                });
    }

}
