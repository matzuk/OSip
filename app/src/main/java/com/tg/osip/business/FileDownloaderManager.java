package com.tg.osip.business;

import com.tg.osip.utils.BackgroundExecutor;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ConcurrentModificationException;
import java.util.concurrent.ConcurrentHashMap;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * @author e.matsyuk
 */
public class FileDownloaderManager {

    private final static String ADD_TO_PATH = "file://";
    public final static String FILE_PATH_EMPTY = "";

    private static volatile FileDownloaderManager instance;

    private Subscription updateChannelSubscription;

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

    public void subscribeToUpdateChannel() {
        updateChannelSubscription = UpdateManager.getInstance().getUpdateChannel()
                .filter(update -> update instanceof TdApi.UpdateFile)
                .map(update -> (TdApi.UpdateFile) update)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .subscribe(new Subscriber<TdApi.UpdateFile>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

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
