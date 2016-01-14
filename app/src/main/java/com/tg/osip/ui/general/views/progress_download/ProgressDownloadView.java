package com.tg.osip.ui.general.views.progress_download;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.tg.osip.R;
import com.tg.osip.tdclient.update_managers.FileDownloaderI;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.update_managers.FileDownloaderUtils;
import com.tg.osip.ui.general.views.progress_download.view_resources.IViewResources;
import com.tg.osip.utils.CommonStaticFields;
import com.tg.osip.utils.common.BackgroundExecutor;
import com.tg.osip.utils.log.Logger;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * View for downloading audio, document files
 *
 * @author e.matsyuk
 */
public abstract class ProgressDownloadView extends FrameLayout {

    private static final float VISIBLE_ALPHA = 1f;
    private static final float INVISIBLE_ALPHA = 0f;
    private static final int ANIM_DURATION = 100;
    private static final int ANIM_SET_DURATION = 200;

    protected static final int IMAGE_DOWNLOAD_LEVEL = 0;
    protected static final int IMAGE_PAUSE_LEVEL = 1;
    protected static final int IMAGE_PLAY_LEVEL = 2;
    protected static final int IMAGE_PLAY_PAUSE_LEVEL = 3;

    private static final int INNER_DOWNLOAD_LEVEL = 0;
    private static final int INNER_PLAY_LEVEL = 1;

    enum ViewState {
        START, // download icon
        DOWNLOADING, // pause icon
        READY,  // downloading was done and playing is ready
        PLAY, // play
        PAUSE_PLAY // playing was paused
    }

    ProgressBar progressBar;
    protected ImageView downloadImage;
    protected ImageView downloadInner;

    ViewState viewState;
    protected FileDownloaderI fileDownloader;
    PlayInfo playInfo;

    Subscription downloadProgressChannelSubscription;
    Subscription downloadChannelSubscription;
    Subscription playChannelSubscription;

    public ProgressDownloadView(Context context) {
        super(context);
        initViews();
    }

    public ProgressDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ProgressDownloadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        IViewResources iViewResources = getIViewResources();

        inflate(getContext(), R.layout.progress_download_view, this);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setProgressDrawable(iViewResources.getProgressDrawable(getResources(), getContext()));
        downloadImage = (ImageView)findViewById(R.id.download_image);
        downloadImage.setImageResource(iViewResources.getDownloadImage());
        downloadInner = (ImageView)findViewById(R.id.download_inner);
        downloadInner.setImageResource(iViewResources.getDownloadInner());
    }

    @NonNull
    protected abstract FileDownloaderManager getFileDownloaderManager();
    @NonNull
    protected abstract IViewResources getIViewResources();
    @Nullable
    protected abstract ViewState getPlayingState();
    @Nullable
    protected abstract Subscription getPlayChannelSubscription();

    public void setFileDownloader(@NonNull FileDownloaderI fileDownloader) {
        if (downloadProgressChannelSubscription != null && !downloadProgressChannelSubscription.isUnsubscribed()) {
            downloadProgressChannelSubscription.unsubscribe();
        }
        if (downloadChannelSubscription != null && !downloadChannelSubscription.isUnsubscribed()) {
            downloadChannelSubscription.unsubscribe();
        }
        if (playChannelSubscription != null && !playChannelSubscription.isUnsubscribed()) {
            playChannelSubscription.unsubscribe();
        }
        this.fileDownloader = fileDownloader;
        viewState = getViewState();
        setViews();
        setProgress();
        setPlayParams();
    }

    private ViewState getViewState() {
        ViewState viewState = getDownloadingViewState();
        if (viewState == ViewState.READY && getPlayingState() != null) {
            viewState = getPlayingState();
        }
        return viewState;
    }

    private ViewState getDownloadingViewState() {
        ViewState viewState;
        if (isFileDownloaded(fileDownloader)) {
            viewState = ViewState.READY;
        } else if (isFileInProgress(fileDownloader)) {
            viewState = ViewState.DOWNLOADING;
        } else {
            viewState = ViewState.START;
        }
        return viewState;
    }

    private void setPlayParams() {
        if (viewState != ViewState.READY && viewState != ViewState.PLAY && viewState != ViewState.PAUSE_PLAY) {
            return;
        }
        if (isFileInCache(fileDownloader)) {
            setPlayInfo(fileDownloader.getFileId(), getFileDownloaderManager().getFilePath(fileDownloader.getFileId()), getFileDownloaderManager().getTGFilePath(fileDownloader.getFileId()));
        } else {
            setPlayInfo(fileDownloader.getFileId(), fileDownloader.getFilePath(), fileDownloader.getTGFilePath());
        }
        if (viewState == ViewState.PLAY || viewState == ViewState.PAUSE_PLAY) {
            subscribeToPlayChannel();
        }
    }

    private boolean isFileDownloaded(FileDownloaderI fileDownloaderI) {
        boolean isFileIdValid = FileDownloaderUtils.isFileIdValid(fileDownloaderI.getFileId());
        boolean isFilePathValid = FileDownloaderUtils.isFilePathValid(fileDownloaderI.getTGFilePath());
        boolean isFileInCache = isFileInCache(fileDownloaderI);
        return isFileIdValid && (isFilePathValid || isFileInCache);
    }

    private boolean isFileInCache(FileDownloaderI fileDownloaderI) {
        return getFileDownloaderManager().isFileInCache(fileDownloaderI.getFileId());
    }

    private boolean isFileInProgress(FileDownloaderI fileDownloaderI) {
        return getFileDownloaderManager().isFileInProgress(fileDownloaderI.getFileId());
    }

    private void setPlayInfo(int id, String path, String tgPath) {
        playInfo = new PlayInfo(id, path, tgPath);
    }

    private void setViews() {
        switch(viewState) {
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
            case PLAY:
                progressBar.setVisibility(View.GONE);
                downloadImage.setImageLevel(IMAGE_PLAY_PAUSE_LEVEL);
                downloadInner.setImageLevel(INNER_PLAY_LEVEL);
                break;
            case PAUSE_PLAY:
                progressBar.setVisibility(View.GONE);
                downloadImage.setImageLevel(IMAGE_PLAY_LEVEL);
                downloadInner.setImageLevel(INNER_PLAY_LEVEL);
                break;
        }
    }

    private void setProgress() {
        if (viewState == ViewState.DOWNLOADING) {
            progressBar.setProgress(getFileDownloaderManager().getProgressValue(fileDownloader.getFileId()));
            subscribeToDownloadProgressChannel();
            subscribeToDownloadChannel();
        } else {
            progressBar.setProgress(CommonStaticFields.EMPTY_PROGRESS);
        }

    }

    protected void animateStartToDownloadingStateChanging() {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator alphaAppearProgressBar = ObjectAnimator.ofFloat(progressBar, "alpha", INVISIBLE_ALPHA, VISIBLE_ALPHA);
        alphaAppearProgressBar.setDuration(ANIM_SET_DURATION);
        AnimatorSet imageLevelChangingAnim = getAnimateImageLevelChanging(downloadImage, IMAGE_DOWNLOAD_LEVEL, IMAGE_PAUSE_LEVEL);
        animatorSet
                .play(alphaAppearProgressBar)
                .with(imageLevelChangingAnim);
        animatorSet.start();
    }

    protected void animateDownloadingToStartStateChanging() {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator alphaDisappearProgressBar = ObjectAnimator.ofFloat(progressBar, "alpha", VISIBLE_ALPHA, INVISIBLE_ALPHA);
        alphaDisappearProgressBar.setDuration(ANIM_SET_DURATION);
        AnimatorSet imageLevelChangingAnim = getAnimateImageLevelChanging(downloadImage, IMAGE_PAUSE_LEVEL, IMAGE_DOWNLOAD_LEVEL);
        animatorSet
                .play(alphaDisappearProgressBar)
                .with(imageLevelChangingAnim);
        animatorSet.start();
    }

    protected AnimatorSet getAnimateImageLevelChanging(final ImageView imageView, int from, int to) {
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

    protected void startDownloading() {
        // start downloading
        getFileDownloaderManager().startFileDownloading(fileDownloader);
        // start update manager listening
        subscribeToDownloadProgressChannel();
        subscribeToDownloadChannel();
    }

    private void subscribeToDownloadProgressChannel() {
        downloadProgressChannelSubscription = getFileDownloaderManager().getDownloadProgressChannel()
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
                        progressBar.setProgress(progress);
                        if (progress == CommonStaticFields.FULL_PROGRESS) {
                            unsubscribe();
                        }

                    }
                });
    }

    private void subscribeToDownloadChannel() {
        downloadChannelSubscription = getFileDownloaderManager().getDownloadChannel()
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
                    public void onNext(Integer fileId) {
                        unsubscribe();
                        setReadyStatus();

                    }
                });
    }

    protected void subscribeToPlayChannel() {
        playChannelSubscription = getPlayChannelSubscription();
    }

    protected void setReadyStatus() {
        viewState = ViewState.READY;
        setPlayInfo(fileDownloader.getFileId(), getFileDownloaderManager().getFilePath(fileDownloader.getFileId()), getFileDownloaderManager().getTGFilePath(fileDownloader.getFileId()));
        animateToReadyStatus();
    }

    protected void stopDownloading() {
        if (downloadProgressChannelSubscription != null && !downloadProgressChannelSubscription.isUnsubscribed()) {
            downloadProgressChannelSubscription.unsubscribe();
        }
        if (downloadChannelSubscription != null && !downloadChannelSubscription.isUnsubscribed()) {
            downloadChannelSubscription.unsubscribe();
        }
        getFileDownloaderManager().stopFileDownloading(fileDownloader);
        progressBar.setProgress(CommonStaticFields.EMPTY_PROGRESS);
    }

    protected void animateToReadyStatus() {
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
        if (downloadChannelSubscription != null && !downloadChannelSubscription.isUnsubscribed()) {
            downloadChannelSubscription.unsubscribe();
        }
        if (playChannelSubscription != null && !playChannelSubscription.isUnsubscribed()) {
            playChannelSubscription.unsubscribe();
        }
    }

}
