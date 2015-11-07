package com.tg.osip.business.main;

import com.tg.osip.business.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.tdclient.models.MainListItem;
import com.tg.osip.ui.main.MainRecyclerAdapter;
import com.tg.osip.utils.ui.auto_loading.AutoLoadingRecyclerView;
import com.tg.osip.utils.ui.auto_loading.ILoading;
import com.tg.osip.utils.BackgroundExecutor;
import com.tg.osip.utils.log.Logger;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Controller for Main screen (MainFragment)
 *
 * @author e.matsyuk
 */
public class MainController {

    public ILoading<MainListItem> getILoading() {
        return offsetAndLimit -> TGProxy.getInstance().sendTD(new TdApi.GetChats(offsetAndLimit.getOffset(), offsetAndLimit.getLimit()), TdApi.Chats.class)
                .map(chats -> {
                    TdApi.Chat chatsMas[] = chats.chats;
                    return new ArrayList<>(Arrays.asList(chatsMas));
                })
                .concatMap(Observable::from)
                .map(MainListItem::new)
                .toList()
                .doOnNext(MainController.this::startFileDownloading);
    }

    public void startFileDownloading(List<MainListItem> mainListItems) {
        Observable.from(mainListItems)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .filter(mainListItem -> mainListItem.isSmallPhotoFileIdValid() && !mainListItem.isSmallPhotoFilePathValid() && !FileDownloaderManager.getInstance().isFileInCache(mainListItem.getSmallPhotoFileId()))
                .concatMap(mainListItem -> TGProxy.getInstance().sendTD(new TdApi.DownloadFile(mainListItem.getSmallPhotoFileId()), TdApi.Ok.class))
                .subscribe();
    }

    // get my user.id and start loading
    public <T> void startRecyclerView(AutoLoadingRecyclerView<T> autoLoadingRecyclerView, MainRecyclerAdapter mainRecyclerAdapter) {
        TGProxy.getInstance().sendTD(new TdApi.GetMe(), TdApi.User.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TdApi.User>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(TdApi.User user) {
                        Logger.debug("user data loaded, recyclerview is next");
                        mainRecyclerAdapter.setUserId(user.id);
                        autoLoadingRecyclerView.startLoading();
                    }
                });
    }

}
