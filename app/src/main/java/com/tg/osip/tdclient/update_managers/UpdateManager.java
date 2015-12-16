package com.tg.osip.tdclient.update_managers;

import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import rx.subjects.PublishSubject;

/**
 * Manager for updates from TGProxyImpl UpdatesHandler
 *
 * @author e.matsyuk
 */
public class UpdateManager {

    private static final String LOG = "Updates";

    private PublishSubject<TdApi.Update> updateChannel = PublishSubject.create();

    public void sendUpdateEvent(TdApi.Update update) {
        updateChannel.onNext(update);
    }

    public PublishSubject<TdApi.Update> getUpdateChannel() {
        return updateChannel;
    }

}
