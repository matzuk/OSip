package com.tg.osip.business.main;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.business.models.UserItem;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.TGProxyI;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Interactor for presenter layout (MainActivity)
 *
 * @author e.matsyuk
 */
public class MainInteract {

    private static final int TIMER_IN_MS = 200;
    // field for preventing extra requests
    private UserItem userItem;

    @Inject
    TGProxyI tgProxy;
    @Inject
    FileDownloaderManager fileDownloaderManager;

    public MainInteract() {
        ApplicationSIP.get().applicationComponent().inject(this);
    }

    public Observable<UserItem> getMeUserObservable() {
        if (userItem == null) {
            return tgProxy
                    .sendTD(new TdApi.GetMe(), TdApi.User.class)
                    .map(UserItem::new)
                    .doOnNext(item -> {
                        userItem = item;
                        fileDownloaderManager.startFileDownloading(userItem);
                    });
        } else {
            return Observable.just(userItem);
        }
    }

    public Observable<TdApi.AuthState> getLogoutObservable(Runnable showLoading, Runnable hideLoading) {
        return Observable.timer(TIMER_IN_MS, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aLong -> {
                    // clear FileDownloaderManager
                    fileDownloaderManager.clearManager();
                    showLoading.run();
                })
                .concatMap(aLong -> tgProxy.sendTD(new TdApi.ResetAuth(false), TdApi.AuthState.class))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(authState -> hideLoading.run());
    }

}
