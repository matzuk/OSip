package com.tg.osip.business.chats;

import com.tg.osip.business.models.ChatItem;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.TGProxyI;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Interactor for presenter layout (ChatsFragment)
 *
 * @author e.matsyuk
 */
public class ChatsInteract {

    TGProxyI tgProxy;
    FileDownloaderManager fileDownloaderManager;

    public ChatsInteract(TGProxyI tgProxy, FileDownloaderManager fileDownloaderManager) {
        this.tgProxy = tgProxy;
        this.fileDownloaderManager = fileDownloaderManager;
    }

    public Observable<List<ChatItem>> getNextDataPortionInList(int offset, int limit) {
        return tgProxy.sendTD(new TdApi.GetChats(offset, limit), TdApi.Chats.class)
                .map(chats -> {
                    TdApi.Chat chatsMas[] = chats.chats;
                    return new ArrayList<>(Arrays.asList(chatsMas));
                })
                .concatMap(Observable::from)
                .map(ChatItem::new)
                .toList()
                .doOnNext(fileDownloaderManager::startFileListDownloading);
    }

}
