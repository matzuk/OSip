package com.tg.osip.ui.general.views.progress_download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.tdclient.update_managers.FileDownloaderI;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.ui.general.views.progress_download.view_resources.IViewResources;
import com.tg.osip.ui.general.views.progress_download.view_resources.VideoViewResources;

import javax.inject.Inject;

import rx.Subscription;

/**
 * @author e.matsyuk
 */
public class VideoProgressDownloadView extends ProgressDownloadView {

    @Inject
    FileDownloaderManager fileDownloaderManager;

    public VideoProgressDownloadView(Context context) {
        super(context);
        provideDependency();
    }

    public VideoProgressDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        provideDependency();
    }

    public VideoProgressDownloadView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        return new VideoViewResources();
    }

    @Nullable
    @Override
    protected ViewState getPlayingState() {
        return null;
    }

    @Nullable
    @Override
    protected Subscription getPlayChannelSubscription() {
        return null;
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
                    playVideo();
                    break;
            }
        });
    }

    private void playVideo() {
        if (playInfo == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(playInfo.getPath()));
        intent.setDataAndType(Uri.parse(playInfo.getPath()), "video/*");
        getContext().startActivity(intent);
    }

}
