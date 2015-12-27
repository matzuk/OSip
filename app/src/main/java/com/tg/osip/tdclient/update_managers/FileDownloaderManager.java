package com.tg.osip.tdclient.update_managers;

import com.tg.osip.tdclient.TGProxyI;
import com.tg.osip.ui.general.views.images.ImageLoaderUtils;
import com.tg.osip.utils.common.BackgroundExecutor;
import com.tg.osip.utils.log.Logger;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


import rx.Observable;
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

    private ConcurrentHashMap<Integer, TdApi.File> fileHashMap = new ConcurrentHashMap<>();
    private PublishSubject<Integer> downloadChannel = PublishSubject.create();

    TGProxyI tgProxy;

    public FileDownloaderManager(TGProxyI tgProxy) {
        this.tgProxy = tgProxy;
        subscribeToUpdateChannel(tgProxy.getUpdateManager().getUpdateChannel());
    }

    public PublishSubject<Integer> getDownloadChannel() {
        return downloadChannel;
    }

    private void subscribeToUpdateChannel(PublishSubject<TdApi.Update> updateChannel) {
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
                        downloadChannel.onNext(update.file.id);
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

    public <T extends FileDownloaderI> void startFileDownloading(T fileDownloaderI) {
        if (ImageLoaderUtils.isPhotoFileIdValid(fileDownloaderI.getPhotoFileId()) && !ImageLoaderUtils.isPhotoFilePathValid(fileDownloaderI.getPhotoFilePath()) &&
                !isFileInCache(fileDownloaderI.getPhotoFileId())) {
            tgProxy
                    .sendTD(new TdApi.DownloadFile(fileDownloaderI.getPhotoFileId()), TdApi.Ok.class)
                    .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                    .observeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                    .subscribe();
        }
    }

    public <T extends FileDownloaderI> void startFileListDownloading(List<T> fileDownloaderIs) {
        Observable.from(fileDownloaderIs)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .filter(fileDownloaderI -> ImageLoaderUtils.isPhotoFileIdValid(fileDownloaderI.getPhotoFileId()) && !ImageLoaderUtils.isPhotoFilePathValid(fileDownloaderI.getPhotoFilePath()) &&
                        !isFileInCache(fileDownloaderI.getPhotoFileId()))
                .concatMap(imageLoaderI -> tgProxy.sendTD(new TdApi.DownloadFile(imageLoaderI.getPhotoFileId()), TdApi.Ok.class))
                .subscribe();
    }

    public void clearManager() {
        fileHashMap.clear();
    }

}
