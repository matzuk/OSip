package com.tg.osip.ui.general.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.tdclient.update_managers.FileDownloaderI;
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
 * View for downloading audio, document files
 *
 * @author e.matsyuk
 */
public class ProgressDownloadView extends FrameLayout {

    public enum State {
        START,
        DOWNLOADING,
        PAUSE,
        READY
    }

    @Inject
    FileDownloaderManager fileDownloaderManager;
    private ProgressBar progressBar;
    private ImageView downloadImage;
    private ImageView pauseImage;
    private State state = State.START;
    private FileDownloaderI fileDownloaderI;
    private Subscription downloadProgressChannelSubscription;

    private OnDownloadClickListener onDownloadClickListener;

    public ProgressDownloadView(Context context) {
        super(context);
        provideDependency();
        init();
    }

    public ProgressDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        provideDependency();
        init();
    }

    public ProgressDownloadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        provideDependency();
        init();
    }

    private void provideDependency() {
        ApplicationSIP.get().applicationComponent().inject(this);
    }

    private void init() {
        inflate(getContext(), R.layout.progress_download_view, this);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        downloadImage = (ImageView)findViewById(R.id.download_image);
        pauseImage = (ImageView)findViewById(R.id.pause_image);
        setOnClickListener(v -> {
            switch (state) {
                case START:
                    animateStateChanging(downloadImage, pauseImage);
                    state = State.DOWNLOADING;
                    if (onDownloadClickListener != null) {
                        onDownloadClickListener.onClick(state);
                    }
                    startDownloading();
                    break;
                case DOWNLOADING:
                    animateStateChanging(pauseImage, downloadImage);
                    state = State.PAUSE;
                    if (onDownloadClickListener != null) {
                        onDownloadClickListener.onClick(state);
                    }
                    break;
                case PAUSE:
                    animateStateChanging(downloadImage, pauseImage);
                    state = State.DOWNLOADING;
                    if (onDownloadClickListener != null) {
                        onDownloadClickListener.onClick(state);
                    }
                    break;
            }
        });
    }

    public void setOnDownloadClickListener(OnDownloadClickListener onDownloadClickListener) {
        this.onDownloadClickListener = onDownloadClickListener;
    }

    private void animateStateChanging(final ImageView imageViewDisappear, final ImageView imageViewAppear) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator alphaAppear = ObjectAnimator.ofFloat(imageViewAppear, "alpha", 0f, 1f);
        ObjectAnimator alphaDisappear = ObjectAnimator.ofFloat(imageViewDisappear, "alpha", 1f, 0f);
        animatorSet
                .play(alphaAppear)
                .after(alphaDisappear);
        animatorSet.setDuration(200);
        animatorSet.start();
    }

    public void setFileDownloaderI(@NonNull FileDownloaderI fileDownloaderI) {
        this.fileDownloaderI = fileDownloaderI;
    }

    private void startDownloading() {
        if (FileDownloaderUtils.isFileIdValid(fileDownloaderI.getFileId())) {
            if (FileDownloaderUtils.isFilePathValid(fileDownloaderI.getFilePath())) {
//                setFileToView(imageLoaderI.getFilePath());
                return;
            }
            // test file downloaded cache
            if (fileDownloaderManager.isFileInCache(fileDownloaderI.getFileId())) {
//                setFileToView(fileDownloaderManager.getFilePath(imageLoaderI.getFileId()));
                return;
            }
        }
        // start downloading
        fileDownloaderManager.startFileDownloading(fileDownloaderI);
        // start update manager listening
        subscribeToDownloadChannel();
    }

    private void subscribeToDownloadChannel() {
        downloadProgressChannelSubscription = fileDownloaderManager.getDownloadProgressChannel()
                .filter(progressPair -> progressPair.first == fileDownloaderI.getFileId())
                .map(progressPair -> progressPair.second)
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
                    public void onNext(Integer progress) {
                        progressBar.setProgress(progress);
                        if (progress == 100) {
                            unsubscribe();
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
        if (downloadProgressChannelSubscription != null && !downloadProgressChannelSubscription.isUnsubscribed()) {
            downloadProgressChannelSubscription.unsubscribe();
        }
    }

    public interface OnDownloadClickListener {
        void onClick(State state);
    }

}
