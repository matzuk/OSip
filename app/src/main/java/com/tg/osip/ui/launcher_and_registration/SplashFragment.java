package com.tg.osip.ui.launcher_and_registration;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.tg.osip.R;
import com.tg.osip.utils.BackgroundExecutor;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.utils.ui.ScalableImageView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

/**
 * @author e.matsyuk
 */
public class SplashFragment extends Fragment {

    private ScrollView scrollView;
    private ScalableImageView imageView1;
    private ScalableImageView imageView2;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.splash_fragment, container, false);
        imageView1 = (ScalableImageView) rootView.findViewById(R.id.imageView1);
        imageView2 = (ScalableImageView) rootView.findViewById(R.id.imageView2);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        Observable.just(null)
                .delay(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    imageView1.setVisibility(View.VISIBLE);
                    imageView2.setVisibility(View.VISIBLE);
                });

    }

}