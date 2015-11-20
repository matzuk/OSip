package com.tg.osip.ui.general.views.images;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * @author e.matsyuk
 */
public class ScalableImageView extends ImageView {

    private static final long ANIM_DURATION = 100;

    private long delayForVisibleAnim;

    public ScalableImageView(Context context) {
        super(context);
    }

    public ScalableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScalableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     *
     * @param delayForVisibleAnim delay in milliseconds
     */
    public void setDelayForVisibleAnim(long delayForVisibleAnim) {
        this.delayForVisibleAnim = delayForVisibleAnim;
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        if (visibility == View.VISIBLE) {
            setAlpha(0f);
            super.onVisibilityChanged(changedView, visibility);
            startAnimation();
        } else {
            super.onVisibilityChanged(changedView, visibility);
        }
    }

    public void startAnimation() {
        AnimatorSet animatorSetBig = new AnimatorSet();
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator expansionAnimX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.4f);
        ObjectAnimator expansionAnimY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.4f);
        ObjectAnimator compressionAnimX = ObjectAnimator.ofFloat(this, "scaleX", 1.4f, 0.6f);
        ObjectAnimator compressionAnimY = ObjectAnimator.ofFloat(this, "scaleY", 1.4f, 0.6f);
        ObjectAnimator toNormalAnimX = ObjectAnimator.ofFloat(this, "scaleX", 0.6f, 1f);
        ObjectAnimator toNormalAnimY = ObjectAnimator.ofFloat(this, "scaleY", 0.6f, 1f);
        animatorSet
                .play(compressionAnimX)
                .with(compressionAnimY)
                .after(expansionAnimX)
                .after(expansionAnimY)
                .before(toNormalAnimX)
                .before(toNormalAnimY);

        ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f);
        animatorSetBig
                .play(animatorSet)
                .with(alpha);

        animatorSetBig.setDuration(ANIM_DURATION);
        animatorSetBig.setStartDelay(delayForVisibleAnim);
        animatorSetBig.start();
    }

}
