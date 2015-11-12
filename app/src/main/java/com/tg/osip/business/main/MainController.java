package com.tg.osip.business.main;

import com.tg.osip.business.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.ui.main_screen.MainRecyclerAdapter;
import com.tg.osip.ui.views.auto_loading.AutoLoadingRecyclerView;
import com.tg.osip.ui.views.auto_loading.ILoading;
import com.tg.osip.utils.common.BackgroundExecutor;
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

    private ILoading<MainListItem> getILoading() {
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

    /**
     * load fresh my user.id id and start RecyclerView for first one
     */
    public void firstStartRecyclerView(AutoLoadingRecyclerView<com.tg.osip.business.main.MainListItem> autoLoadingRecyclerView, MainRecyclerAdapter mainRecyclerAdapter) {
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
                        autoLoadingRecyclerView.setLoadingObservable(getILoading());
                        autoLoadingRecyclerView.startLoading();
                    }
                });
    }

    /**
     * set parameters to RecyclerView after screen reorientation
     * so we should not load my user.id for ILoading of RecyclerView
     */
    public void startRecyclerView(AutoLoadingRecyclerView<com.tg.osip.business.main.MainListItem> autoLoadingRecyclerView) {
        autoLoadingRecyclerView.setLoadingObservable(getILoading());
        autoLoadingRecyclerView.startLoading();
    }

}
