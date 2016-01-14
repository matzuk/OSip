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

    // field for preventing extra requests
    private UserItem userItem;

    TGProxyI tgProxy;
    FileDownloaderManager fileDownloaderManager;

    public MainInteract(TGProxyI tgProxy, FileDownloaderManager fileDownloaderManager) {
        this.tgProxy = tgProxy;
        this.fileDownloaderManager = fileDownloaderManager;
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

    public Observable<TdApi.AuthState> getLogoutObservable() {
        return tgProxy.sendTD(new TdApi.ResetAuth(false), TdApi.AuthState.class)
                .doOnNext(aLong -> {
                    // clear FileDownloaderManager
                    fileDownloaderManager.clearManager();
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

}
