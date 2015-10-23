package com.tg.osip.business;

import com.tg.osip.utils.log.Logger;

import org.drinkless.td.libcore.telegram.TdApi;

import rx.subjects.PublishSubject;

/**
 * Manager for updates from TGProxy UpdatesHandler
 *
 * @author e.matsyuk
 */
public class UpdateManager {

    private static volatile UpdateManager instance;
    private PublishSubject<TdApi.Update> updateChannel = PublishSubject.create();

    public static UpdateManager getInstance() {
        if (instance == null) {
            synchronized (UpdateManager.class) {
                if (instance == null) {
                    instance = new UpdateManager();
                }
            }
        }
        return instance;
    }

    public void sendUpdateEvent(TdApi.Update update) {
        updateChannel.onNext(update);
    }

    public PublishSubject<TdApi.Update> getUpdateChannel() {
        return updateChannel;
    }

}
