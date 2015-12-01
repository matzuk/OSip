package com.tg.osip.business.main;

import com.tg.osip.business.models.UserItem;
import com.tg.osip.business.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.utils.common.BackgroundExecutor;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Interactor for presenter layout (MainActivity)
 *
 * @author e.matsyuk
 */
public class MainInteract {

    private static final int TIMER_IN_MS = 200;
    // field for preventing extra requests
    private UserItem userItem;

    public Observable<UserItem> getMeUserObservable() {
        if (userItem == null) {
            return TGProxy.getInstance()
                    .sendTD(new TdApi.GetMe(), TdApi.User.class)
                    .map(UserItem::new)
                    .doOnNext(item -> {
                        userItem = item;
                        FileDownloaderManager.getInstance().startFileDownloading(userItem);
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
                    FileDownloaderManager.getInstance().clearManager();
                    showLoading.run();
                })
                .concatMap(aLong -> TGProxy.getInstance().sendTD(new TdApi.ResetAuth(false), TdApi.AuthState.class))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(authState -> hideLoading.run());
    }

}
