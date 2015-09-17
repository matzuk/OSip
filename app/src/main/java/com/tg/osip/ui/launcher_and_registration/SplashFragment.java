package com.tg.osip.ui.launcher_and_registration;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tg.osip.R;
import com.tg.osip.business.AuthManager;
import com.tg.osip.utils.ui.ScalableImageView;

/**
 * @author e.matsyuk
 */
public class SplashFragment extends Fragment {

    // in MS
    private static final long preDelayForAnimation = 1000;
    // in MS
    private static final long preDelayForRequest = 2000;

    private ScalableImageView imageView1;
    private ScalableImageView imageView2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.splash_fragment, container, false);
        imageView1 = (ScalableImageView) rootView.findViewById(R.id.imageView1);
        imageView1.setDelayForVisibleAnim(preDelayForAnimation);
        imageView2 = (ScalableImageView) rootView.findViewById(R.id.imageView2);
        imageView2.setDelayForVisibleAnim(preDelayForAnimation);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        AuthManager.getInstance().authStateRequestWithDelay(preDelayForRequest);
        imageView1.setVisibility(View.VISIBLE);
        imageView2.setVisibility(View.VISIBLE);
    }

}