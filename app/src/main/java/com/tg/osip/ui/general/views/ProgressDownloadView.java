package com.tg.osip.ui.general.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.tg.osip.R;

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

    private ProgressBar progressBar;
    private ImageView downloadImage;
    private ImageView pauseImage;
    private State state = State.START;

    private OnDownloadClickListener onDownloadClickListener;

    public ProgressDownloadView(Context context) {
        super(context);
        init();
    }

    public ProgressDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressDownloadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
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

    public interface OnDownloadClickListener {
        void onClick(State state);
    }

}
