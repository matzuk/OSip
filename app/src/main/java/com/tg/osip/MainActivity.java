package com.tg.osip;

import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tg.osip.business.AuthManager;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.ui.launcher_and_registration.PhoneRegistrationFragment;
import com.tg.osip.ui.launcher_and_registration.SplashFragment;
import com.tg.osip.utils.log.Logger;

import org.drinkless.td.libcore.telegram.TdApi;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity {

    Subscription channelSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        subscribeToChannel();
        startSplash();
    }

    private void subscribeToChannel() {
        channelSubscription = AuthManager.getInstance().getAuthChannel()
                .subscribe(new Subscriber<AuthManager.AuthStateEnum>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(AuthManager.AuthStateEnum authStateEnum) {
                        Logger.debug(authStateEnum);
                        if (authStateEnum == AuthManager.AuthStateEnum.AUTH_STATE_WAIT_PHONE_NUMBER) {
                            PhoneRegistrationFragment newFragment = new PhoneRegistrationFragment();
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.addToBackStack(null);
                            transaction.replace(R.id.container, newFragment);
                            transaction.commit();
                        }
                    }
                });
    }

    private void startSplash() {
        SplashFragment newFragment = new SplashFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.commit();
    }
}
