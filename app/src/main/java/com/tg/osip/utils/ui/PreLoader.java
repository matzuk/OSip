package com.tg.osip.utils.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.tg.osip.R;

/**
 * @author e.matsyuk
 */
public class PreLoader extends ImageView {

    private AnimatorSet animatorSet;
    private boolean manualCancel = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    public PreLoader(Context context) {
        super(context.getApplicationContext());
        setParamsForAnimation();
    }

    public PreLoader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreLoader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context.getApplicationContext(), attrs, defStyleAttr);
        setParamsForAnimation();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PreLoader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context.getApplicationContext(), attrs, defStyleAttr, defStyleRes);
    }

    private void setParamsForAnimation() {

        setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pre_loader, null));
        setImageLevel(0);

        animatorSet = new AnimatorSet();
        ObjectAnimator animRotate1 = ObjectAnimator.ofInt(this, "imageLevel", 95);
        ObjectAnimator animRotate2 = ObjectAnimator.ofInt(this, "imageLevel", 95);
        ObjectAnimator animRotate3 = ObjectAnimator.ofInt(this, "imageLevel", 95);

        // general animation
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!manualCancel) {
                    animatorSet.start();
                }
            }
        });
        animatorSet.setDuration(2000);
        animatorSet.play(animRotate1).before(animRotate2).after(animRotate3);

    }

    private Runnable startAnimationInUI = new Runnable() {
        @Override
        public void run() {
            animatorSet.start();
        }
    };

    private void startOrStopAnim(int visibility) {
        if (animatorSet != null && (visibility == GONE || visibility == INVISIBLE)) {
            manualCancel = true;
            animatorSet.cancel();
            animatorSet = null;
            return;
        }
        if (visibility == VISIBLE) {
            if (animatorSet == null) {
                setParamsForAnimation();
            }
            if (!animatorSet.isStarted()) {
                manualCancel = false;
//                handler.post(startAnimationInUI);
                animatorSet.start();

            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animatorSet != null) {
            manualCancel = true;
            animatorSet.cancel();
            animatorSet = null;
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        startOrStopAnim(visibility);
    }

}