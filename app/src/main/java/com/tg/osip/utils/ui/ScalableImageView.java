package com.tg.osip.utils.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * @author e.matsyuk
 */
public class ScalableImageView extends ImageView {

    public ScalableImageView(Context context) {
        super(context);
    }

    public ScalableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScalableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            startAnimation();
        }
    }

    public void startAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator expansionAnimX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.4f);
        ObjectAnimator expansionAnimY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.4f);
        ObjectAnimator compressionAnimX = ObjectAnimator.ofFloat(this, "scaleX", 1.4f, 0.6f);
        ObjectAnimator compressionAnimY = ObjectAnimator.ofFloat(this, "scaleY", 1.4f, 0.6f);
        ObjectAnimator toNormalAnimX = ObjectAnimator.ofFloat(this, "scaleX", 0.6f, 1f);
        ObjectAnimator toNormalAnimY = ObjectAnimator.ofFloat(this, "scaleY", 0.6f, 1f);
        animatorSet.
                play(compressionAnimX).
                with(compressionAnimY).
                after(expansionAnimX).
                after(expansionAnimY).
                before(toNormalAnimX).
                before(toNormalAnimY);
        animatorSet.setDuration(100);
        animatorSet.start();
    }

}
