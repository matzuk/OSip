package com.tg.osip;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tg.osip.business.AuthManager;
import com.tg.osip.business.AuthManager.AuthStateEnum;
import com.tg.osip.ui.launcher_and_registration.CodeVerificationFragment;
import com.tg.osip.ui.launcher_and_registration.PhoneRegistrationFragment;
import com.tg.osip.ui.launcher_and_registration.SplashFragment;
import com.tg.osip.utils.log.Logger;

import rx.Subscriber;
import rx.Subscription;

public class MainActivity extends AppCompatActivity {

    private Subscription channelSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);
        subscribeToChannel();
        startSplash();
    }

    private void subscribeToChannel() {
        channelSubscription = AuthManager.getInstance().getAuthChannel().subscribe(channelSubscriptionSubscriber);
    }

    private Subscriber<AuthStateEnum> channelSubscriptionSubscriber = new Subscriber<AuthStateEnum>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Logger.error(e);
        }

        @Override
        public void onNext(AuthStateEnum authStateEnum) {
            Logger.debug(authStateEnum);
            startNextFragment(authStateEnum);
        }
    };

    private void startNextFragment(AuthStateEnum authStateEnum) {
        switch (authStateEnum) {
            case AUTH_STATE_WAIT_PHONE_NUMBER:
                startPhoneNumberFragment();
                break;
            case AUTH_STATE_LOGGING_OUT:
                break;
            case AUTH_STATE_OK:
                break;
            case AUTH_STATE_WAIT_CODE:
                startCodeVerificationFragment();
                break;
            case AUTH_STATE_WAIT_NAME:
                break;
            case AUTH_STATE_WAIT_PASSWORD:
                break;
        }
    }

    private void startPhoneNumberFragment() {
        PhoneRegistrationFragment newFragment = new PhoneRegistrationFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.commit();
    }

    private void startCodeVerificationFragment() {
        CodeVerificationFragment newFragment = new CodeVerificationFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.container, newFragment);
        transaction.commit();
    }

    private void startSplash() {
        SplashFragment newFragment = new SplashFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.commit();
    }
}
