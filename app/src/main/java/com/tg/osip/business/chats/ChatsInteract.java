package com.tg.osip.business.chats;

import android.content.Context;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.business.models.ChatItem;
import com.tg.osip.business.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.TGProxyI;
import com.tg.osip.tdclient.TGProxyImpl;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Interactor for presenter layout (ChatsFragment)
 *
 * @author e.matsyuk
 */
public class ChatsInteract {

    @Inject
    TGProxyI tgProxy;

    public ChatsInteract() {
        ApplicationSIP.get().applicationComponent().inject(this);
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
                .doOnNext(mainListItems -> {
                    // send photo items to FileDownloaderManager
                    FileDownloaderManager.getInstance().startFileListDownloading(mainListItems);
                });
    }

}
