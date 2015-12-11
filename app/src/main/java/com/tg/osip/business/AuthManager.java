package com.tg.osip.business;

import android.content.Context;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.tdclient.TGProxyI;
import com.tg.osip.utils.log.Logger;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

/**
 * Authentication manager
 *
 * @author e.matsyuk
 */
public class AuthManager {

    public enum AuthStateEnum {
        AUTH_STATE_LOGGING_OUT,
        AUTH_STATE_OK,
        AUTH_STATE_WAIT_CODE,
        AUTH_STATE_WAIT_NAME,
        AUTH_STATE_WAIT_PASSWORD,
        AUTH_STATE_WAIT_PHONE_NUMBER
    }

    private static volatile AuthManager instance;
    private PublishSubject<AuthStateEnum> authChannel = PublishSubject.create();

    @Inject
    TGProxyI tgProxy;

    public static AuthManager getInstance() {
        if (instance == null) {
            synchronized (AuthManager.class) {
                if (instance == null) {
                    instance = new AuthManager();
                }
            }
        }
        return instance;
    }

    private AuthManager() {
        ApplicationSIP.get().applicationComponent().inject(this);
    }

    public void authStateRequest() {
        sendToChannel(getAuthStateObs());
    }

    public void authStateRequestWithDelay(long delayInMS) {
        sendToChannel(
                getAuthStateObs()
                .delay(delayInMS, TimeUnit.MILLISECONDS)
        );
    }

    private Observable<AuthStateEnum> getAuthStateObs() {
        return tgProxy.sendTD(new TdApi.GetAuthState(), TdApi.AuthState.class)
                .map(this::mappingToAuthStateEnum);
    }

    private void sendToChannel(Observable<AuthStateEnum> observable) {
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AuthStateEnum>() {
                    @Override
                    public void onCompleted() {
                        Logger.debug("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.debug(e.getMessage());
                        authChannel.onError(e);
                    }

                    @Override
                    public void onNext(AuthStateEnum authStateEnum) {
                        Logger.debug(authStateEnum);
                        authChannel.onNext(authStateEnum);
                    }
                });
    }

    private AuthStateEnum mappingToAuthStateEnum(TdApi.AuthState authState) {
        Logger.debug(authState);
        if (authState instanceof TdApi.AuthStateLoggingOut) {
            return AuthStateEnum.AUTH_STATE_LOGGING_OUT;
        } else if (authState instanceof TdApi.AuthStateOk) {
            return AuthStateEnum.AUTH_STATE_OK;
        } else if (authState instanceof TdApi.AuthStateWaitCode) {
            return AuthStateEnum.AUTH_STATE_WAIT_CODE;
        } else if (authState instanceof TdApi.AuthStateWaitName) {
            return AuthStateEnum.AUTH_STATE_WAIT_NAME;
        } else if (authState instanceof TdApi.AuthStateWaitPassword) {
            return AuthStateEnum.AUTH_STATE_WAIT_PASSWORD;
        } else if (authState instanceof TdApi.AuthStateWaitPhoneNumber) {
            return AuthStateEnum.AUTH_STATE_WAIT_PHONE_NUMBER;
        }
        // return default value -> start registration
        return AuthStateEnum.AUTH_STATE_WAIT_PHONE_NUMBER;
    }

    public void setAuthPhoneNumberRequest(String phoneNumber) {
        sendToChannel(setAuthPhoneNumberObs(phoneNumber));
    }

    private Observable<AuthStateEnum> setAuthPhoneNumberObs(String phoneNumber) {
        return tgProxy.sendTD(new TdApi.SetAuthPhoneNumber(phoneNumber), TdApi.AuthState.class)
                .map(this::mappingToAuthStateEnum);
    }

    public void setAuthNameRequest(String firstName, String lastName) {
        sendToChannel(setAuthNameObs(firstName, lastName));
    }

    private Observable<AuthStateEnum> setAuthNameObs(String firstName, String lastName) {
        return tgProxy.sendTD(new TdApi.SetAuthName(firstName, lastName), TdApi.AuthState.class)
                .map(this::mappingToAuthStateEnum);
    }

    public void setAuthCodeRequest(String code) {
        sendToChannel(setAuthCodeObs(code));
    }

    private Observable<AuthStateEnum> setAuthCodeObs(String code) {
        return tgProxy.sendTD(new TdApi.SetAuthCode(code), TdApi.AuthState.class)
                .map(this::mappingToAuthStateEnum);
    }

    public void resetAuth(boolean force) {
        sendToChannel(resetAuthObs(force));
    }

    private Observable<AuthStateEnum> resetAuthObs(boolean force) {
        return tgProxy.sendTD(new TdApi.ResetAuth(force), TdApi.AuthState.class)
                .map(this::mappingToAuthStateEnum);
    }

    public PublishSubject<AuthStateEnum> getAuthChannel() {
        return authChannel;
    }

    public Observable<TdApi.User> loadNeededInfo() {
        return tgProxy.sendTD(new TdApi.GetMe(), TdApi.User.class);
    }

}
