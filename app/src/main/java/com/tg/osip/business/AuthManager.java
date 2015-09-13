package com.tg.osip.business;

import com.tg.osip.tdclient.TGProxy;

import org.drinkless.td.libcore.telegram.TdApi;

import rx.Observable;
import rx.functions.Func1;

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
        AUTH_STATE_WAITH_PHONE_NUMBER
    }

    private static volatile AuthManager instance;

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

    public Observable<AuthStateEnum> getAuthState() {
        return TGProxy.getInstance().sendTD(new TdApi.GetAuthState(), TdApi.AuthState.class)
                .map(authState -> {
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
                        return AuthStateEnum.AUTH_STATE_WAITH_PHONE_NUMBER;
                    }
                    // return default value -> start registration
                    return AuthStateEnum.AUTH_STATE_WAITH_PHONE_NUMBER;
                });
    }

}
