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
    private static final int DOWNLOAD_IMAGE_LEVEL = 0;
    private static final int PAUSE_IMAGE_LEVEL = 1;

    public enum DownloadingState {
        START, // download icon
        DOWNLOADING, // pause icon
        PAUSE_DOWNLOADING, // download icon
        READY // downloading was done
    }

    @Inject
    FileDownloaderManager fileDownloaderManager;

    ProgressBar progressBar;
    ImageView startImage;
    ImageView downloadedImage;
    ImageView startInner;
    ImageView downloadedInner;

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
        startImage = (ImageView)findViewById(R.id.start_image);
        downloadedImage = (ImageView)findViewById(R.id.downloaded_image);
        startInner = (ImageView)findViewById(R.id.start_inner);
        downloadedInner = (ImageView)findViewById(R.id.downloaded_inner);
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
                progressBar.setAlpha(1f);
                startImage.setVisibility(View.VISIBLE);
                startImage.setAlpha(1f);
                startImage.setImageLevel(DOWNLOAD_IMAGE_LEVEL);
                startInner.setVisibility(View.VISIBLE);
                startInner.setAlpha(1f);
                downloadedImage.setVisibility(View.GONE);
                downloadedImage.setAlpha(0f);
                downloadedInner.setVisibility(View.GONE);
                downloadedInner.setAlpha(0f);
                break;
            case DOWNLOADING:
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setAlpha(1f);
                startImage.setVisibility(View.VISIBLE);
                startImage.setImageLevel(PAUSE_IMAGE_LEVEL);
                startImage.setAlpha(1f);
                startInner.setVisibility(View.VISIBLE);
                startInner.setAlpha(1f);
                downloadedImage.setVisibility(View.GONE);
                downloadedImage.setAlpha(0f);
                downloadedInner.setVisibility(View.GONE);
                downloadedInner.setAlpha(0f);
                break;
            case READY:
                progressBar.setVisibility(View.GONE);
                progressBar.setAlpha(0f);
                startImage.setVisibility(View.GONE);
                startImage.setAlpha(0f);
                startInner.setVisibility(View.GONE);
                startInner.setAlpha(0f);
                downloadedImage.setVisibility(View.VISIBLE);
                downloadedImage.setAlpha(1f);
                downloadedInner.setVisibility(View.VISIBLE);
                downloadedInner.setAlpha(1f);
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
                    animateStateChanging(startImage, DOWNLOAD_IMAGE_LEVEL, PAUSE_IMAGE_LEVEL);
                    downloadingState = DownloadingState.DOWNLOADING;
                    if (onDownloadClickListener != null) {
                        onDownloadClickListener.onClick(downloadingState);
                    }
                    startDownloading();
                    break;
                case DOWNLOADING:
                    animateStateChanging(startImage, PAUSE_IMAGE_LEVEL, DOWNLOAD_IMAGE_LEVEL);
                    downloadingState = DownloadingState.PAUSE_DOWNLOADING;
                    if (onDownloadClickListener != null) {
                        onDownloadClickListener.onClick(downloadingState);
                    }
                    break;
                case PAUSE_DOWNLOADING:
                    animateStateChanging(startImage, DOWNLOAD_IMAGE_LEVEL, PAUSE_IMAGE_LEVEL);
                    downloadingState = DownloadingState.DOWNLOADING;
                    if (onDownloadClickListener != null) {
                        onDownloadClickListener.onClick(downloadingState);
                    }
                    break;
            }
        });
    }

    private void animateStateChanging(final ImageView imageView, int from, int to) {
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
        animatorSet.start();
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

        ObjectAnimator alphaDisappearStartImage = ObjectAnimator.ofFloat(startImage, "alpha", 1f, 0f);
        ObjectAnimator alphaDisappearStartInner = ObjectAnimator.ofFloat(startInner, "alpha", 1f, 0f);
        ObjectAnimator alphaDisappearProgress = ObjectAnimator.ofFloat(progressBar, "alpha", 1f, 0f);
        AnimatorSet animatorSetDisappear = new AnimatorSet();
        animatorSetDisappear.setDuration(100);
        animatorSetDisappear
                .play(alphaDisappearStartImage)
                .with(alphaDisappearStartInner)
                .with(alphaDisappearProgress);

        downloadedImage.setAlpha(0f);
        downloadedInner.setAlpha(0f);
        downloadedImage.setVisibility(View.VISIBLE);
        downloadedInner.setVisibility(View.VISIBLE);

        ObjectAnimator alphaAppearStartImage = ObjectAnimator.ofFloat(downloadedImage, "alpha", 0f, 1f);
        ObjectAnimator alphaAppearStartInner = ObjectAnimator.ofFloat(downloadedInner, "alpha", 0f, 1f);
        AnimatorSet animatorSetAppear = new AnimatorSet();
        animatorSetAppear.setDuration(100);
        animatorSetAppear
                .play(alphaAppearStartImage)
                .with(alphaAppearStartInner);

        animatorSet
                .play(animatorSetDisappear)
                .before(animatorSetAppear);

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
