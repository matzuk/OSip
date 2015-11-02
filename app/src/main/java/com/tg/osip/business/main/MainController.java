package com.tg.osip.business.main;

import android.view.View;

import com.tg.osip.business.FileDownloaderManager;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.tdclient.models.MainListItem;
import com.tg.osip.ui.main.MainRecyclerAdapter;
import com.tg.osip.utils.ui.auto_loading.AutoLoadingRecyclerView;
import com.tg.osip.utils.ui.auto_loading.ILoading;
import com.tg.osip.utils.ui.auto_loading.OffsetAndLimit;
import com.tg.osip.utils.BackgroundExecutor;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.utils.time.TimeUtils;
import com.tg.osip.utils.ui.PreLoader;

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

    private final static int EMPTY_FILE_ID = 0;

    // temp argument - preLoader, later to move in RecyclerView
    public ILoading<MainListItem> getILoading(PreLoader preLoader) {
        return new ILoading<MainListItem>() {
            @Override
            public Observable<List<MainListItem>> getLoadingObservable(OffsetAndLimit offsetAndLimit) {
                return TGProxy.getInstance().sendTD(new TdApi.GetChats(offsetAndLimit.getOffset(), offsetAndLimit.getLimit()), TdApi.Chats.class)
                        .map(chats -> {
                            TdApi.Chat chatsMas[] = chats.chats;
                            return new ArrayList<>(Arrays.asList(chatsMas));
                        })
                        .concatMap(Observable::from)
                        .map(chat -> {
                            MainListItem mainListItem = new MainListItem(chat);
                            mainListItem.setLastMessageDate(TimeUtils.stringForMessageListDate(chat.topMessage.date));
                            return mainListItem;
                        })
                        .toList()
                        .doOnNext(MainController.this::startFileDownloading);
            }
            @Override
            public void startLoadData() {

            }
            @Override
            public void endLoadData() {
                preLoader.setVisibility(View.GONE);

            }
        };
    }

    public void startFileDownloading(List<MainListItem> mainListItems) {
        Observable.from(mainListItems)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .map(mainListItem -> getFileId(mainListItem.getApiChat().type))
                .filter(integer -> (integer != EMPTY_FILE_ID) && (FileDownloaderManager.getInstance().getFilePath(integer).equals(FileDownloaderManager.FILE_PATH_EMPTY)))
                .concatMap(integer -> TGProxy.getInstance().sendTD(new TdApi.DownloadFile(integer), TdApi.Ok.class))
                .subscribe();
    }

    private Integer getFileId(TdApi.ChatInfo chatInfo) {
        if (chatInfo instanceof TdApi.GroupChatInfo) {
            return ((TdApi.GroupChatInfo)chatInfo).groupChat.photo.small.id;
        } else if (chatInfo instanceof TdApi.PrivateChatInfo) {
            return ((TdApi.PrivateChatInfo)chatInfo).user.profilePhoto.small.id;
        } else {
            return EMPTY_FILE_ID;
        }
    }

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
