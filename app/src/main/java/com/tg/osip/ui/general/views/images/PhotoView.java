package com.tg.osip.ui.general.views.images;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tg.osip.ApplicationSIP;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.update_managers.FileDownloaderUtils;
import com.tg.osip.utils.common.BackgroundExecutor;
import com.tg.osip.utils.log.Logger;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Custom ImageView with circle bounds, letter settings with background (if there is no photo) and photo downloading from ChatInfo
 *
 * @author e.matsyuk
 */
public class PhotoView extends ImageView {

    @Inject
    FileDownloaderManager fileDownloaderManager;
    private int fileId;
    private Subscription downloadChannelSubscription;
    private boolean circleRounds;

    public PhotoView(Context context) {
        super(context);
        provideDependency();
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        provideDependency();
    }

    public PhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        provideDependency();
    }

    private void provideDependency() {
        ApplicationSIP.get().applicationComponent().inject(this);
    }

    public void setImageLoaderI(@NonNull ImageLoaderI imageLoaderI) {
        startImageLoading(imageLoaderI);
    }

    private void startImageLoading(ImageLoaderI imageLoaderI) {
        unSubscribe();
        Logger.debug("FileId: " + imageLoaderI.getFileId());
        Logger.debug("FilePath: " + imageLoaderI.getFilePath());
        // set default drawable
        if (imageLoaderI.getPlugFile() != null && FileDownloaderUtils.isFileIdValid(imageLoaderI.getPlugFile().getFileId())) {
            if (FileDownloaderUtils.isFilePathValid(imageLoaderI.getPlugFile().getFilePath())) {
                setFileToView(imageLoaderI.getPlugFile().getFilePath());
            } else if (fileDownloaderManager.isFileInCache(imageLoaderI.getPlugFile().getFileId())) {
                setFileToView(fileDownloaderManager.getFilePath(imageLoaderI.getPlugFile().getFileId()));
            }
        } else {
            setImageDrawable(imageLoaderI.getPlug());
        }
        if (FileDownloaderUtils.isFileIdValid(imageLoaderI.getFileId())) {
            if (FileDownloaderUtils.isFilePathValid(imageLoaderI.getFilePath())) {
                setFileToView(imageLoaderI.getFilePath());
                fileId = 0;
                return;
            }
            // test file downloaded cache
            if (fileDownloaderManager.isFileInCache(imageLoaderI.getFileId())) {
                Logger.debug("FilePath cache: " + fileDownloaderManager.getFilePath(imageLoaderI.getFileId()));
                setFileToView(fileDownloaderManager.getFilePath(imageLoaderI.getFileId()));
                fileId = 0;
                return;
            }
        }
        // start update manager listening
        fileId = imageLoaderI.getFileId();
        subscribeToDownloadChannel();
    }

    /**
     * Request before startImageLoading method!
     * Or value == false
     */
    public void setCircleRounds(boolean circleRounds) {
        this.circleRounds = circleRounds;
    }

    private void setFileToView(String fileToPath) {
        if (circleRounds) {
            Picasso.with(
                    ApplicationSIP.applicationContext).
                    load(fileToPath).
                    placeholder(getDrawable()).
                    transform(new CirclePicassoTransformation()).
                    into(this
                    );
        } else {
            Picasso.with(
                    ApplicationSIP.applicationContext).
                    load(fileToPath).
                    placeholder(getDrawable()).
                    into(this
                    );
        }
    }

    private void subscribeToDownloadChannel() {
        downloadChannelSubscription = fileDownloaderManager.getDownloadChannel()
                .filter(downloadedFileId -> downloadedFileId == fileId)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(Integer downloadedFileId) {
                        unsubscribe();
                        if (fileDownloaderManager.isFileInCache(downloadedFileId)) {
                            setFileToView(fileDownloaderManager.getFilePath(downloadedFileId));
                        }
                    }
                });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unSubscribe();
    }

    private void unSubscribe() {
        if (downloadChannelSubscription != null && !downloadChannelSubscription.isUnsubscribed()) {
            downloadChannelSubscription.unsubscribe();
        }
    }

}
