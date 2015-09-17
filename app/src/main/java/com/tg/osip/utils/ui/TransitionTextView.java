package com.tg.osip.utils.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.tg.osip.utils.AndroidUtils;

/**
 * @author e.matsyuk
 */
public class TransitionTextView extends TextView {

    private static final int TEMP_HEIGHT_IN_DP = 60;

    public TransitionTextView(Context context) {
        super(context);
    }

    public TransitionTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TransitionTextView(Context context, AttributeSet attrs, int defStyleAttr) {
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

        int transY = AndroidUtils.fromDp(TEMP_HEIGHT_IN_DP);

        ObjectAnimator animTransY = ObjectAnimator.ofFloat(this, "translationY", -transY, 0);
        ObjectAnimator animAlpha = ObjectAnimator.ofFloat(this, "alpha", 0, 1);
        animatorSet.
                play(animTransY).
                with(animAlpha);
        animatorSet.setDuration(150);
        animatorSet.start();
    }

}
