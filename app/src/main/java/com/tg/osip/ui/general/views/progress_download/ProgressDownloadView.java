package com.tg.osip.ui.general.views.progress_download;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
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

    private static final int INCORRECT_TYPE = -1;

    private static final float VISIBLE_ALPHA = 1f;
    private static final float INVISIBLE_ALPHA = 0f;
    private static final int ANIM_DURATION = 100;
    private static final int ANIM_SET_DURATION = 200;

    private static final int IMAGE_DOWNLOAD_LEVEL = 0;
    private static final int IMAGE_PAUSE_LEVEL = 1;
    private static final int IMAGE_PLAY_LEVEL = 2;
    private static final int INNER_DOWNLOAD_LEVEL = 0;
    private static final int INNER_PLAY_LEVEL = 1;

    enum Type {
        AUDIO(0),  DOCUMENT(1), VIDEO(2);
        private int typeInt;
        Type(int type) {
            typeInt = type;
        }
        static Type getType(int typeInt) {
            for (Type type : values()) {
                if (type.typeInt == typeInt) return type;
            }
            throw new ProgressDownloadViewException("incorrect getType");
        }
    }

    enum DownloadingState {
        START, // download icon
        DOWNLOADING, // pause icon
        PLAY, // downloading was done and playing is ready
        PAUSE_PLAY // playing was stopped
    }

    @Inject
    FileDownloaderManager fileDownloaderManager;

    Type type;

    ProgressBar progressBar;
    private ImageView downloadImage;
    private ImageView downloadInner;

    DownloadingState downloadingState;
    private FileDownloaderI fileDownloader;
    Subscription downloadProgressChannelSubscription;
    private PlayActionI playAction;

    @VisibleForTesting
    public ProgressDownloadView() {
        super(null);
    }

    @VisibleForTesting
    public ProgressDownloadView(Context context) {
        super(context);
        provideDependency();
        initViews();
    }

    public ProgressDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        provideDependency();
        initType(context, attrs, 0);
        initViews();
    }

    public ProgressDownloadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        provideDependency();
        initType(context, attrs, defStyleAttr);
        initViews();
    }

    private void provideDependency() {
        ApplicationSIP.get().applicationComponent().inject(this);
    }

    private void initType(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressDownloadView, defStyleAttr, 0);
        int param = typedArray.getInt(R.styleable.ProgressDownloadView_type, INCORRECT_TYPE);
        if (param == INCORRECT_TYPE) {
            throw new ProgressDownloadViewException();
        }
        type = Type.getType(param);
        typedArray.recycle();
    }

    private void initPlayAction() {
        playAction = PlayActionsFactory.getPlayAction(type, fileDownloader);
        if (playAction == null) {
            throw new ProgressDownloadViewException("null playAction");
        }
    }

    private void initViews() {
        inflate(getContext(), R.layout.progress_download_view, this);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        downloadImage = (ImageView)findViewById(R.id.download_image);
        downloadInner = (ImageView)findViewById(R.id.download_inner);
    }

    @VisibleForTesting
    void setFileDownloaderManager(FileDownloaderManager fileDownloaderManager) {
        this.fileDownloaderManager = fileDownloaderManager;
    }

    public void setFileDownloader(@NonNull FileDownloaderI fileDownloader) {
        if (downloadProgressChannelSubscription != null && !downloadProgressChannelSubscription.isUnsubscribed()) {
            downloadProgressChannelSubscription.unsubscribe();
        }
        this.fileDownloader = fileDownloader;
        downloadingState = getDownloadingState(fileDownloader);
        setViews();
        initPlayAction();
        setProgress();
        setOnClickListeners();
    }

    DownloadingState getDownloadingState(FileDownloaderI fileDownloaderI) {
        if (isFileDownloaded(fileDownloaderI)) {
            return DownloadingState.PLAY;
        } else if (isFileInProgress(fileDownloaderI)) {
            return DownloadingState.DOWNLOADING;
        } else {
            return DownloadingState.START;
        }
    }

    boolean isFileDownloaded(FileDownloaderI fileDownloaderI) {
        boolean isFileIdValid = FileDownloaderUtils.isFileIdValid(fileDownloaderI.getFileId());
        boolean isFilePathValid = FileDownloaderUtils.isFilePathValid(fileDownloaderI.getTGFilePath());
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
            case PLAY:
                progressBar.setVisibility(View.GONE);
                downloadImage.setImageLevel(IMAGE_PLAY_LEVEL);
                downloadInner.setImageLevel(INNER_PLAY_LEVEL);
                break;
        }
    }

    private void setProgress() {
        if (downloadingState == DownloadingState.DOWNLOADING) {
            progressBar.setProgress(fileDownloaderManager.getProgressValue(fileDownloader.getFileId()));
            subscribeToDownloadChannel();
        } else {
            progressBar.setProgress(CommonStaticFields.EMPTY_PROGRESS);
        }

    }

    private void setOnClickListeners() {
//        if (downloadingState == DownloadingState.START || downloadingState == DownloadingState.DOWNLOADING) {
            setOnClickListenerForDownloadingState();
//        }
    }

    private void setOnClickListenerForDownloadingState() {
        setOnClickListener(v -> {
            switch (downloadingState) {
                case START:
                    animateStartToDownloadingStateChanging();
                    downloadingState = DownloadingState.DOWNLOADING;
                    startDownloading();
                    break;
                case DOWNLOADING:
                    animateDownloadingToStartStateChanging();
                    downloadingState = DownloadingState.START;
                    stopDownloading();
                    break;
                case PLAY:
                    playAction.play();
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

    private void startDownloading() {
        // start downloading
        fileDownloaderManager.startFileDownloading(fileDownloader);
        // start update manager listening
        subscribeToDownloadChannel();
    }

    private void subscribeToDownloadChannel() {
        downloadProgressChannelSubscription = fileDownloaderManager.getDownloadProgressChannel()
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
                            setReadyStatus();
                        }

                    }
                });
    }

    private void setReadyStatus() {
        downloadingState = DownloadingState.PLAY;
        animateToReadyStatus();
    }

    private void stopDownloading() {
        if (downloadProgressChannelSubscription != null && !downloadProgressChannelSubscription.isUnsubscribed()) {
            downloadProgressChannelSubscription.unsubscribe();
        }
        fileDownloaderManager.stopFileDownloading(fileDownloader);
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

}
