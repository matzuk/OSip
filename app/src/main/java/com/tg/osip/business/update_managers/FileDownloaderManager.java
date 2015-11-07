package com.tg.osip.business.update_managers;

import com.tg.osip.utils.BackgroundExecutor;
import com.tg.osip.utils.log.Logger;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.concurrent.ConcurrentHashMap;

import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Manager for file downloads
 *
 * @author e.matsyuk
 */
public class FileDownloaderManager {

    private final static String ADD_TO_PATH = "file://";
    public final static String FILE_PATH_EMPTY = "";

    private static volatile FileDownloaderManager instance;

    private ConcurrentHashMap<Integer, TdApi.File> fileHashMap = new ConcurrentHashMap<>();

    public static FileDownloaderManager getInstance() {
        if (instance == null) {
            synchronized (FileDownloaderManager.class) {
                if (instance == null) {
                    instance = new FileDownloaderManager();
                }
            }
        }
        return instance;
    }

    void subscribeToUpdateChannel(PublishSubject<TdApi.Update> updateChannel) {
        updateChannel
                .filter(update -> update.getClass() == TdApi.UpdateFile.class)
                .map(update -> (TdApi.UpdateFile) update)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .subscribe(new Subscriber<TdApi.UpdateFile>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(TdApi.UpdateFile update) {
                        fileHashMap.put(update.file.id, update.file);
                    }
                });
    }

    public String getFilePath(int fileId) {
        TdApi.File file = fileHashMap.get(fileId);
        if (file != null) {
            return ADD_TO_PATH + file.path;
        }
        return FILE_PATH_EMPTY;
    }

    public boolean isFileInCache(int fileId) {
        return !getFilePath(fileId).equals(FILE_PATH_EMPTY);
    }

}
