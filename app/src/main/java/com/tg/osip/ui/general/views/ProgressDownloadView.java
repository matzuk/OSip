package com.tg.osip.ui.general.views;

import android.animation.Animator;
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
import com.tg.osip.utils.CommonStaticFields;
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

    private static final float VISIBLE_ALPHA = 1f;
    private static final float INVISIBLE_ALPHA = 0f;
    private static final int ANIM_DURATION = 100;
    private static final int ANIM_SET_DURATION = 200;

    private static final int IMAGE_DOWNLOAD_LEVEL = 0;
    private static final int IMAGE_PAUSE_LEVEL = 1;
    private static final int IMAGE_PLAY_LEVEL = 2;
    private static final int INNER_DOWNLOAD_LEVEL = 0;
    private static final int INNER_PLAY_LEVEL = 1;

    public enum DownloadingState {
        START, // download icon
        DOWNLOADING, // pause icon
        READY // downloading was done
    }

    @Inject
    FileDownloaderManager fileDownloaderManager;

    ProgressBar progressBar;
    private ImageView downloadImage;
    private ImageView downloadInner;

    DownloadingState downloadingState;
    private FileDownloaderI fileDownloaderI;
    Subscription downloadProgressChannelSubscription;

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
        if (downloadProgressChannelSubscription != null && !downloadProgressChannelSubscription.isUnsubscribed()) {
            downloadProgressChannelSubscription.unsubscribe();
        }
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
                progressBar.setAlpha(VISIBLE_ALPHA);
                progressBar.setVisibility(View.VISIBLE);
                downloadImage.setImageLevel(IMAGE_DOWNLOAD_LEVEL);
                downloadInner.setImageLevel(INNER_DOWNLOAD_LEVEL);
                break;
            case DOWNLOADING:
                progressBar.setAlpha(VISIBLE_ALPHA);
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
            subscribeToDownloadChannel();
        } else {
            progressBar.setProgress(CommonStaticFields.EMPTY_PROGRESS);
        }

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
                    animateStartToDownloadingStateChanging();
                    downloadingState = DownloadingState.DOWNLOADING;
                    if (onDownloadClickListener != null) {
                        onDownloadClickListener.onClick(downloadingState);
                    }
                    startDownloading();
                    break;
                case DOWNLOADING:
                    animateDownloadingToStartStateChanging();
                    downloadingState = DownloadingState.START;
                    if (onDownloadClickListener != null) {
                        onDownloadClickListener.onClick(downloadingState);
                    }
                    stopDownloading();
                    break;
            }
        });
    }

    private void animateStartToDownloadingStateChanging() {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator alphaAppearProgressBar = ObjectAnimator.ofFloat(progressBar, "alpha", INVISIBLE_ALPHA, VISIBLE_ALPHA);
        alphaAppearProgressBar.setDuration(ANIM_SET_DURATION);
        AnimatorSet imageLevelChangingAnim = getAnimateImageLevelChanging(downloadImage, IMAGE_DOWNLOAD_LEVEL, IMAGE_PAUSE_LEVEL);
        animatorSet
                .play(alphaAppearProgressBar)
                .with(imageLevelChangingAnim);
        animatorSet.start();
    }

    private void animateDownloadingToStartStateChanging() {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator alphaDisappearProgressBar = ObjectAnimator.ofFloat(progressBar, "alpha", VISIBLE_ALPHA, INVISIBLE_ALPHA);
        alphaDisappearProgressBar.setDuration(ANIM_SET_DURATION);
        AnimatorSet imageLevelChangingAnim = getAnimateImageLevelChanging(downloadImage, IMAGE_PAUSE_LEVEL, IMAGE_DOWNLOAD_LEVEL);
        animatorSet
                .play(alphaDisappearProgressBar)
                .with(imageLevelChangingAnim);
        animatorSet.start();
    }

    private AnimatorSet getAnimateImageLevelChanging(final ImageView imageView, int from, int to) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator alphaAppear = ObjectAnimator.ofFloat(imageView, "alpha", INVISIBLE_ALPHA, VISIBLE_ALPHA);
        alphaAppear.setDuration(ANIM_DURATION);
        ObjectAnimator alphaDisappear = ObjectAnimator.ofFloat(imageView, "alpha", VISIBLE_ALPHA, INVISIBLE_ALPHA);
        alphaDisappear.setDuration(ANIM_DURATION);
        ObjectAnimator alphaImageLevelChanging = ObjectAnimator.ofInt(imageView, "ImageLevel", from, to);
        alphaImageLevelChanging.setDuration(ANIM_DURATION);
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
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(Integer progress) {
                        progressBar.setProgress(progress);
                        if (progress == CommonStaticFields.FULL_PROGRESS) {
                            unsubscribe();
                            setReadyStatus();
                        }

                    }
                });
    }

    private void setReadyStatus() {
        downloadingState = DownloadingState.READY;
        animateToReadyStatus();
    }

    private void stopDownloading() {
        if (downloadProgressChannelSubscription != null && !downloadProgressChannelSubscription.isUnsubscribed()) {
            downloadProgressChannelSubscription.unsubscribe();
        }
        fileDownloaderManager.stopFileDownloading(fileDownloaderI);
        progressBar.setProgress(CommonStaticFields.EMPTY_PROGRESS);
    }

    private void animateToReadyStatus() {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator alphaDisappearProgress = ObjectAnimator.ofFloat(progressBar, "alpha", VISIBLE_ALPHA, INVISIBLE_ALPHA);
        alphaDisappearProgress.setDuration(ANIM_DURATION);
        alphaDisappearProgress.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        AnimatorSet imageAnimatorSet = getAnimateImageLevelChanging(downloadImage, IMAGE_PAUSE_LEVEL, IMAGE_PLAY_LEVEL);
        AnimatorSet innerAnimatorSet = getAnimateImageLevelChanging(downloadInner, INNER_DOWNLOAD_LEVEL, INNER_PLAY_LEVEL);

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
