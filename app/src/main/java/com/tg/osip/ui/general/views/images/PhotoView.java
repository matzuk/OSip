package com.tg.osip.ui.general.views.images;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tg.osip.ApplicationSIP;
import com.tg.osip.business.update_managers.FileDownloaderManager;
import com.tg.osip.utils.common.BackgroundExecutor;
import com.tg.osip.utils.log.Logger;

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

    private int fileId;
    private Subscription downloadChannelSubscription;
    private boolean circleRounds;

    public PhotoView(Context context) {
        super(context);
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageLoaderI(ImageLoaderI imageLoaderI) {
        startImageLoading(imageLoaderI);
    }

    private void startImageLoading(ImageLoaderI imageLoaderI) {
        unSubscribe();
        // set default drawable
        if (imageLoaderI.getPlugFile() != null && ImageLoaderUtils.isPhotoFileIdValid(imageLoaderI.getPlugFile().getPhotoFileId())) {
            if (ImageLoaderUtils.isPhotoFilePathValid(imageLoaderI.getPlugFile().getPhotoFilePath())) {
                setFileToView(imageLoaderI.getPlugFile().getPhotoFilePath());
            } else if (FileDownloaderManager.getInstance().isFileInCache(imageLoaderI.getPlugFile().getPhotoFileId())) {
                setFileToView(FileDownloaderManager.getInstance().getFilePath(imageLoaderI.getPlugFile().getPhotoFileId()));
            }
        } else {
            setImageDrawable(imageLoaderI.getPlug());
        }
        if (ImageLoaderUtils.isPhotoFileIdValid(imageLoaderI.getPhotoFileId())) {
            if (ImageLoaderUtils.isPhotoFilePathValid(imageLoaderI.getPhotoFilePath())) {
                setFileToView(imageLoaderI.getPhotoFilePath());
                return;
            }
            // test file downloaded cache
            if (FileDownloaderManager.getInstance().isFileInCache(imageLoaderI.getPhotoFileId())) {
                setFileToView(FileDownloaderManager.getInstance().getFilePath(imageLoaderI.getPhotoFileId()));
                return;
            }
        }
        // start update manager listening
        fileId = imageLoaderI.getPhotoFileId();
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
        downloadChannelSubscription = FileDownloaderManager.getInstance().getDownloadChannel()
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
                        if (FileDownloaderManager.getInstance().isFileInCache(downloadedFileId)) {
                            setFileToView(FileDownloaderManager.getInstance().getFilePath(downloadedFileId));
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
