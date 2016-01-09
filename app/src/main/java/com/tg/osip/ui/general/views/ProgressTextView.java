package com.tg.osip.ui.general.views;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.widget.TextView;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.tdclient.update_managers.FileDownloaderI;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.update_managers.FileDownloaderUtils;
import com.tg.osip.utils.CommonStaticFields;
import com.tg.osip.utils.common.BackgroundExecutor;
import com.tg.osip.utils.dagger2_static.BytesFormatter;
import com.tg.osip.utils.log.Logger;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author e.matsyuk
 */
public class ProgressTextView extends TextView {

    private String defaultText;
    private int fileSize;
    private String fileSizeString;
    private FileDownloaderI fileDownloader;

    @Inject
    FileDownloaderManager fileDownloaderManager;
    @Inject
    BytesFormatter bytesFormatter;

    Subscription downloadProgressChannelSubscription;
    Subscription downloadCancelChannelSubscription;

    public ProgressTextView(Context context) {
        super(context);
        provideDependency();
    }

    public ProgressTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        provideDependency();
    }

    public ProgressTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        provideDependency();
    }

    private void provideDependency() {
        ApplicationSIP.get().applicationComponent().inject(this);
    }

    @VisibleForTesting
    void setFileDownloaderManager(FileDownloaderManager fileDownloaderManager) {
        this.fileDownloaderManager = fileDownloaderManager;
    }

    public void setDownloadingInfo(FileDownloaderI fileDownloaderI, String defaultText, int fileSize) {
        unSubscribe();
        this.fileDownloader = fileDownloaderI;
        this.defaultText = defaultText;
        this.fileSize = fileSize;
        fileSizeString = bytesFormatter.formatFileSize(getContext(), fileSize);
        init();
    }

    private void init() {
        if (isFileDownloaded(fileDownloader)) {
            setText(defaultText);
            return;
        }
        if (isFileInProgress(fileDownloader)) {
            String downloadedString = getDownloadedString(getStartDownloadedPart());
            setText(downloadedString);
        } else {
            setText(defaultText);
        }
        subscribeToUpdateChannel();
        subscribeToCancelChannel();
    }

    private boolean isFileDownloaded(FileDownloaderI fileDownloaderI) {
        boolean isFileIdValid = FileDownloaderUtils.isFileIdValid(fileDownloaderI.getFileId());
        boolean isFilePathValid = FileDownloaderUtils.isFilePathValid(fileDownloaderI.getTGFilePath());
        boolean isFileInCache = fileDownloaderManager.isFileInCache(fileDownloaderI.getFileId());
        return isFileIdValid && (isFilePathValid || isFileInCache);
    }

    private boolean isFileInProgress(FileDownloaderI fileDownloaderI) {
        return fileDownloaderManager.isFileInProgress(fileDownloaderI.getFileId());
    }

    private String getDownloadedString(float downloadedPart) {
        int downloadedSize = (int)(downloadedPart * fileSize);
        String downloadedSizeString = bytesFormatter.formatFileSize(getContext(), downloadedSize);
        return getContext().getString(R.string.progress_textview_downloading, downloadedSizeString, fileSizeString);
    }

    private float getStartDownloadedPart() {
        return (float)fileDownloaderManager.getProgressValue(fileDownloader.getFileId()) / (float) CommonStaticFields.FULL_PROGRESS;
    }

    private void subscribeToUpdateChannel() {
        downloadProgressChannelSubscription = fileDownloaderManager.getDownloadProgressChannel()
                .filter(progressPair -> progressPair.first == fileDownloader.getFileId())
                .map(progressPair -> progressPair.second)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(Integer progress) {
                        float downloadedPart = (float) progress / (float) CommonStaticFields.FULL_PROGRESS;
                        String downloadedString = getDownloadedString(downloadedPart);
                        setText(downloadedString);
                        if (progress == CommonStaticFields.FULL_PROGRESS) {
                            unSubscribe();
                            setText(defaultText);
                        }
                    }
                });
    }

    private void subscribeToCancelChannel() {
        downloadCancelChannelSubscription = fileDownloaderManager.getDownloadCancelChannel()
                .filter(fileId -> fileId == fileDownloader.getFileId())
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(Integer progress) {
                        setText(defaultText);
                    }
                });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unSubscribe();
    }

    private void unSubscribe() {
        if (downloadProgressChannelSubscription != null && !downloadProgressChannelSubscription.isUnsubscribed()) {
            downloadProgressChannelSubscription.unsubscribe();
        }
        if (downloadCancelChannelSubscription != null && !downloadCancelChannelSubscription.isUnsubscribed()) {
            downloadCancelChannelSubscription.unsubscribe();
        }
    }

}
