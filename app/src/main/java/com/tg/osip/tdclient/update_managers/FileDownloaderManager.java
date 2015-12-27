package com.tg.osip.tdclient.update_managers;

import android.support.v4.util.Pair;

import com.tg.osip.tdclient.TGProxyI;
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
    private final static int EMPTY_FILE_ID = 0;
    private final static int MINIMAL_FILE_SIZE = 0;

    private ConcurrentHashMap<Integer, TdApi.File> fileHashMap = new ConcurrentHashMap<>();
    private PublishSubject<Integer> downloadChannel = PublishSubject.create();
    private PublishSubject<Pair<Integer, Integer>> downloadProgressChannel = PublishSubject.create();

    TGProxyI tgProxy;

    public FileDownloaderManager(TGProxyI tgProxy) {
        this.tgProxy = tgProxy;
        subscribeToUpdateChannel(tgProxy.getUpdateManager().getUpdateChannel());
    }

    public PublishSubject<Integer> getDownloadChannel() {
        return downloadChannel;
    }

    public PublishSubject<Pair<Integer, Integer>> getDownloadProgressChannel() {
        return downloadProgressChannel;
    }

    private void subscribeToUpdateChannel(PublishSubject<TdApi.Update> updateChannel) {
        updateChannel
                .filter(update -> update.getClass() == TdApi.UpdateFile.class)
                .map(update -> (TdApi.UpdateFile) update)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .subscribe(new Subscriber<TdApi.UpdateFile>() {
                    @Override
                    public void onCompleted() {
                    }

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
        updateChannel
                .filter(update -> update.getClass() == TdApi.UpdateFileProgress.class)
                .map(update -> (TdApi.UpdateFileProgress) update)
                .filter(update -> update.fileId > EMPTY_FILE_ID && update.size > MINIMAL_FILE_SIZE)
                .map(update -> {
                    float progress = (float) update.ready / (float) update.size * 100;
                    return new Pair<>(update.fileId, (int) progress);
                })
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .subscribe(new Subscriber<Pair<Integer, Integer>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(Pair<Integer, Integer> update) {
                        downloadProgressChannel.onNext(update);
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
        if (FileDownloaderUtils.isFileIdValid(fileDownloaderI.getFileId()) && !FileDownloaderUtils.isFilePathValid(fileDownloaderI.getFilePath()) &&
                !isFileInCache(fileDownloaderI.getFileId())) {
            tgProxy
                    .sendTD(new TdApi.DownloadFile(fileDownloaderI.getFileId()), TdApi.Ok.class)
                    .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                    .observeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                    .subscribe();
        }
    }

    public <T extends FileDownloaderI> void startFileListDownloading(List<T> fileDownloaderIs) {
        Observable.from(fileDownloaderIs)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .filter(fileDownloaderI -> FileDownloaderUtils.isFileIdValid(fileDownloaderI.getFileId()) && !FileDownloaderUtils.isFilePathValid(fileDownloaderI.getFilePath()) &&
                        !isFileInCache(fileDownloaderI.getFileId()))
                .concatMap(imageLoaderI -> tgProxy.sendTD(new TdApi.DownloadFile(imageLoaderI.getFileId()), TdApi.Ok.class))
                .subscribe();
    }

    public void clearManager() {
        fileHashMap.clear();
    }

}
