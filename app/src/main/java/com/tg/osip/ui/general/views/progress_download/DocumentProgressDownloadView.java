package com.tg.osip.ui.general.views.progress_download;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.tdclient.update_managers.FileDownloaderI;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.ui.general.views.progress_download.view_resources.DocumentViewResources;
import com.tg.osip.ui.general.views.progress_download.view_resources.IViewResources;
import com.tg.osip.ui.general.views.progress_download.view_resources.VideoViewResources;
import com.tg.osip.utils.log.Logger;

import javax.inject.Inject;

import rx.Subscription;

/**
 * @author e.matsyuk
 */
public class DocumentProgressDownloadView extends ProgressDownloadView {

    @Inject
    FileDownloaderManager fileDownloaderManager;

    private String mimeType;

    public DocumentProgressDownloadView(Context context) {
        super(context);
        provideDependency();
    }

    public DocumentProgressDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        provideDependency();
    }

    public DocumentProgressDownloadView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        return new DocumentViewResources();
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
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        viewState = ViewState.PLAY;
                        openDocument();
                        break;
                }
            }
        });
    }

    private void openDocument() {
        if (playInfo == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(playInfo.getTgPath());
        if (mimeType != null && mimeType.startsWith("image")){
            intent.setDataAndType(uri, "image/*");
        } else {
            intent.setDataAndType(uri, mimeType);
        }
        try {
            getContext().startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            // FIXME add toast
            Logger.error(exception);
        }
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

}