package com.tg.osip.ui.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tg.osip.R;
import com.tg.osip.business.AuthManager;
import com.tg.osip.business.AuthManager.AuthStateEnum;
import com.tg.osip.tdclient.exceptions.TdApiErrorException;
import com.tg.osip.ui.launcher_and_registration.CodeVerificationFragment;
import com.tg.osip.ui.launcher_and_registration.NameRegistrationFragment;
import com.tg.osip.ui.launcher_and_registration.PhoneRegistrationFragment;
import com.tg.osip.ui.launcher_and_registration.SplashFragment;
import com.tg.osip.utils.AndroidUtils;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.utils.ui.SimpleAlertDialog;

import rx.Subscriber;
import rx.Subscription;

public class LoginActivity extends AppCompatActivity {

    private Subscription channelSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_login);
        subscribeToChannel();
        startFragment(new SplashFragment(), false);
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
            errorHandling(e);

        }

        @Override
        public void onNext(AuthStateEnum authStateEnum) {
            Logger.debug(authStateEnum);
            startNextFragment(authStateEnum);
        }
    };

    private void errorHandling(Throwable e) {
        if (e instanceof TdApiErrorException) {
            TdApiErrorException tdApiErrorException = (TdApiErrorException)e;
            String message = "ERROR \ncode:" + tdApiErrorException.getCode() + "\ntext:" + tdApiErrorException.getText();
            SimpleAlertDialog.show(
                    LoginActivity.this,
                    getResources().getString(R.string.app_name),
                    message
            );
        }
    }

    private void startNextFragment(AuthStateEnum authStateEnum) {
        AndroidUtils.hideKeyboard(this);
        switch (authStateEnum) {
            case AUTH_STATE_WAIT_PHONE_NUMBER:
                startFragment(new PhoneRegistrationFragment(), true);
                break;
            case AUTH_STATE_LOGGING_OUT:
                break;
            case AUTH_STATE_OK:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case AUTH_STATE_WAIT_CODE:
                startFragment(new CodeVerificationFragment(), true);
                break;
            case AUTH_STATE_WAIT_NAME:
                startFragment(new NameRegistrationFragment(), true);
                break;
            case AUTH_STATE_WAIT_PASSWORD:
                break;
        }
    }

    private void startFragment(Fragment fragment, boolean withBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (withBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (channelSubscription != null && !channelSubscription.isUnsubscribed()) {
            channelSubscription.unsubscribe();
        }
    }

}
