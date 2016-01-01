package com.tg.osip.ui.general.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.view.View;
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

    private static final int FULL_PROGRESS = 100;
    private static final int IMAGE_DOWNLOAD_LEVEL = 0;
    private static final int IMAGE_PAUSE_LEVEL = 1;
    private static final int IMAGE_PLAY_LEVEL = 2;
    private static final int INNER_DOWNLOAD_LEVEL = 0;
    private static final int INNER_PLAY_LEVEL = 1;

    public enum DownloadingState {
        START, // download icon
        DOWNLOADING, // pause icon
        PAUSE_DOWNLOADING, // download icon
        READY // downloading was done
    }

    @Inject
    FileDownloaderManager fileDownloaderManager;

    ProgressBar progressBar;
    ImageView downloadImage;
    ImageView downloadInner;

    private DownloadingState downloadingState;
    private FileDownloaderI fileDownloaderI;
    private Subscription downloadProgressChannelSubscription;

    private OnDownloadClickListener onDownloadClickListener;

    @VisibleForTesting
    public ProgressDownloadView() {
        super(null);
    }

    public ProgressDownloadView(Context context) {
        super(context);
        provideDependency();
        initViews();
    }

    public ProgressDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        provideDependency();
        initViews();
    }

    public ProgressDownloadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        provideDependency();
        initViews();
    }

    @VisibleForTesting
    void setFileDownloaderManager(FileDownloaderManager fileDownloaderManager) {
        this.fileDownloaderManager = fileDownloaderManager;
    }

    private void provideDependency() {
        ApplicationSIP.get().applicationComponent().inject(this);
    }

    private void initViews() {
        inflate(getContext(), R.layout.progress_download_view, this);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        downloadImage = (ImageView)findViewById(R.id.download_image);
        downloadInner = (ImageView)findViewById(R.id.download_inner);
    }

    public void setFileDownloaderI(@NonNull FileDownloaderI fileDownloaderI) {
        this.fileDownloaderI = fileDownloaderI;
        downloadingState = getDownloadingState(fileDownloaderI);
        setViews();
        setProgress();
        setOnClickListeners();
    }

    DownloadingState getDownloadingState(FileDownloaderI fileDownloaderI) {
        if (isFileDownloaded(fileDownloaderI)) {
            return DownloadingState.READY;
        } else if (isFileInProgress(fileDownloaderI)) {
            return DownloadingState.DOWNLOADING;
        } else {
            return DownloadingState.START;
        }
    }

    boolean isFileDownloaded(FileDownloaderI fileDownloaderI) {
        boolean isFileIdValid = FileDownloaderUtils.isFileIdValid(fileDownloaderI.getFileId());
        boolean isFilePathValid = FileDownloaderUtils.isFilePathValid(fileDownloaderI.getFilePath());
        boolean isFileInCache = fileDownloaderManager.isFileInCache(fileDownloaderI.getFileId());
        return isFileIdValid && (isFilePathValid || isFileInCache);
    }

    boolean isFileInProgress(FileDownloaderI fileDownloaderI) {
        return fileDownloaderManager.isFileInProgress(fileDownloaderI.getFileId());
    }

    private void setViews() {
        switch(downloadingState) {
            case START:
                progressBar.setVisibility(View.VISIBLE);
                downloadImage.setImageLevel(IMAGE_DOWNLOAD_LEVEL);
                downloadInner.setImageLevel(INNER_DOWNLOAD_LEVEL);
                break;
            case DOWNLOADING:
                progressBar.setVisibility(View.VISIBLE);
                downloadImage.setImageLevel(IMAGE_PAUSE_LEVEL);
                downloadInner.setImageLevel(INNER_DOWNLOAD_LEVEL);
                break;
            case READY:
                progressBar.setVisibility(View.GONE);
                downloadImage.setImageLevel(IMAGE_PLAY_LEVEL);
                downloadInner.setImageLevel(INNER_PLAY_LEVEL);
                break;
        }
    }

    private void setProgress() {
        if (downloadingState == DownloadingState.DOWNLOADING) {
            progressBar.setProgress(fileDownloaderManager.getProgressValue(fileDownloaderI.getFileId()));
        }
        subscribeToDownloadChannel();
    }

    private void setOnClickListeners() {
        if (downloadingState == DownloadingState.START || downloadingState == DownloadingState.DOWNLOADING) {
            setOnClickListenerForDownloadingState();
        }
    }

    private void setOnClickListenerForDownloadingState() {
        setOnClickListener(v -> {
            switch (downloadingState) {
                case START:
                    animateStateChanging(downloadImage, IMAGE_DOWNLOAD_LEVEL, IMAGE_PAUSE_LEVEL).start();
                    downloadingState = DownloadingState.DOWNLOADING;
                    if (onDownloadClickListener != null) {
                        onDownloadClickListener.onClick(downloadingState);
                    }
                    startDownloading();
                    break;
                case DOWNLOADING:
                    animateStateChanging(downloadImage, IMAGE_PAUSE_LEVEL, IMAGE_DOWNLOAD_LEVEL).start();
                    downloadingState = DownloadingState.PAUSE_DOWNLOADING;
                    if (onDownloadClickListener != null) {
                        onDownloadClickListener.onClick(downloadingState);
                    }
                    break;
                case PAUSE_DOWNLOADING:
                    animateStateChanging(downloadImage, IMAGE_DOWNLOAD_LEVEL, IMAGE_PAUSE_LEVEL).start();
                    downloadingState = DownloadingState.DOWNLOADING;
                    if (onDownloadClickListener != null) {
                        onDownloadClickListener.onClick(downloadingState);
                    }
                    break;
            }
        });
    }

    private AnimatorSet animateStateChanging(final ImageView imageView, int from, int to) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator alphaAppear = ObjectAnimator.ofFloat(imageView, "alpha", 0f, 1f);
        alphaAppear.setDuration(100);
        ObjectAnimator alphaDisappear = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f);
        alphaDisappear.setDuration(100);
        ObjectAnimator alphaImageLevelChanging = ObjectAnimator.ofInt(imageView, "ImageLevel", from, to);
        alphaImageLevelChanging.setDuration(100);
        animatorSet
                .play(alphaDisappear)
                .with(alphaImageLevelChanging)
                .before(alphaAppear);
        return animatorSet;
    }

    public void setOnDownloadClickListener(OnDownloadClickListener onDownloadClickListener) {
        this.onDownloadClickListener = onDownloadClickListener;
    }

    private void startDownloading() {
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
                        if (progress == FULL_PROGRESS) {
                            unsubscribe();
                            setReadyStatus();
                        }

                    }
                });
    }

    private void setReadyStatus() {
        downloadingState = DownloadingState.READY;
        setOnClickListeners();
        animateToReadyStatus();
    }

    private void animateToReadyStatus() {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator alphaDisappearProgress = ObjectAnimator.ofFloat(progressBar, "alpha", 1f, 0f);
        AnimatorSet imageAnimatorSet = animateStateChanging(downloadImage, IMAGE_PAUSE_LEVEL, IMAGE_PLAY_LEVEL);
        AnimatorSet innerAnimatorSet = animateStateChanging(downloadInner, INNER_DOWNLOAD_LEVEL, INNER_PLAY_LEVEL);

        animatorSet
                .play(alphaDisappearProgress)
                .with(imageAnimatorSet)
                .with(innerAnimatorSet);

        animatorSet.start();
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
        void onClick(DownloadingState downloadingState);
    }

}
