package com.tg.osip.business.update_managers;

import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import rx.subjects.PublishSubject;

/**
 * Manager for updates from TGProxy UpdatesHandler
 *
 * @author e.matsyuk
 */
public class UpdateManager {

    private static final String LOG = "Updates";

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

    private UpdateManager() {
        // init helper classes
        FileDownloaderManager.getInstance().subscribeToUpdateChannel(updateChannel);
    }

    public void sendUpdateEvent(TdApi.Update update) {
        Log.d(LOG, update.toString());
        updateChannel.onNext(update);
    }

    public PublishSubject<TdApi.Update> getUpdateChannel() {
        return updateChannel;
    }

}
