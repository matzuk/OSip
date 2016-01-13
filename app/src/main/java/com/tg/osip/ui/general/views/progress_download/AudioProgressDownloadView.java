package com.tg.osip.ui.general.views.progress_download;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.business.media.MediaManager;
import com.tg.osip.tdclient.update_managers.FileDownloaderI;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.ui.general.views.progress_download.view_resources.AudioViewResources;
import com.tg.osip.ui.general.views.progress_download.view_resources.IViewResources;
import com.tg.osip.utils.common.BackgroundExecutor;
import com.tg.osip.utils.log.Logger;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author e.matsyuk
 */
public class AudioProgressDownloadView extends ProgressDownloadView {

    @Inject
    FileDownloaderManager fileDownloaderManager;
    @Inject
    MediaManager mediaManager;

    public AudioProgressDownloadView(Context context) {
        super(context);
        provideDependency();
    }

    public AudioProgressDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        provideDependency();
    }

    public AudioProgressDownloadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        provideDependency();
    }

    private void provideDependency() {
        ApplicationSIP.get().applicationComponent().inject(this);
    }

    @NonNull
    @Override
    protected FileDownloaderManager getFileDownloaderManager() {
        return fileDownloaderManager;
    }

    @NonNull
    @Override
    protected IViewResources getIViewResources() {
        return new AudioViewResources();
    }

    @Nullable
    @Override
    protected ViewState getPlayingState() {
        return fileDownloader.getFileId() == mediaManager.getCurrentIdFile()? mediaManager.isPaused()? ViewState.PAUSE_PLAY : ViewState.PLAY : ViewState.READY;
    }

    @Nullable
    @Override
    protected Subscription getPlayChannelSubscription() {
        return mediaManager.getPlayChannel()
                .filter(fileId -> fileId != fileDownloader.getFileId() && viewState == ViewState.PLAY)
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
                    public void onNext(Integer fileId) {
                        getAnimateImageLevelChanging(downloadImage, IMAGE_PLAY_PAUSE_LEVEL, IMAGE_PLAY_LEVEL).start();
                        viewState = ViewState.PAUSE_PLAY;
                    }
                });
    }

    @Override
    public void setFileDownloader(@NonNull FileDownloaderI fileDownloader) {
        super.setFileDownloader(fileDownloader);
        setOnClickListener(v -> {
            switch (viewState) {
                case START:
                    animateStartToDownloadingStateChanging();
                    viewState = ViewState.DOWNLOADING;
                    startDownloading();
                    break;
                case DOWNLOADING:
                    animateDownloadingToStartStateChanging();
                    viewState = ViewState.START;
                    stopDownloading();
                    break;
                case READY:
                    getAnimateImageLevelChanging(downloadImage, IMAGE_PLAY_LEVEL, IMAGE_PLAY_PAUSE_LEVEL).start();
                    viewState = ViewState.PLAY;
                    if (playInfo != null) {
                        subscribeToPlayChannel();
                        mediaManager.play(playInfo.getPath(), playInfo.getId());
                    }
                    break;
                case PLAY:
                    getAnimateImageLevelChanging(downloadImage, IMAGE_PLAY_PAUSE_LEVEL, IMAGE_PLAY_LEVEL).start();
                    viewState = ViewState.PAUSE_PLAY;
                    mediaManager.pause();
                    break;
                case PAUSE_PLAY:
                    getAnimateImageLevelChanging(downloadImage, IMAGE_PLAY_LEVEL, IMAGE_PLAY_PAUSE_LEVEL).start();
                    viewState = ViewState.PLAY;
                    if (playInfo != null) {
                        subscribeToPlayChannel();
                        mediaManager.play(playInfo.getPath(), playInfo.getId());
                    }
                    break;
            }
        });
    }

    @VisibleForTesting
    void setFileDownloaderManager(FileDownloaderManager fileDownloaderManager) {
        this.fileDownloaderManager = fileDownloaderManager;
    }

    @VisibleForTesting
    void setMediaManager(MediaManager mediaManager) {
        this.mediaManager = mediaManager;
    }

}
