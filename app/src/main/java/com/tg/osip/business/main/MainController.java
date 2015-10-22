package com.tg.osip.business.main;

import android.view.View;

import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.ui.main.MainRecyclerAdapter;
import com.tg.osip.ui.views.auto_loading.AutoLoadingRecyclerView;
import com.tg.osip.ui.views.auto_loading.ILoading;
import com.tg.osip.ui.views.auto_loading.OffsetAndLimit;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.utils.ui.PreLoader;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Controller for Main screen (MainFragment)
 *
 * @author e.matsyuk
 */
public class MainController {

    // temp argument - preLoader, later to move in RecyclerView
    public ILoading<TdApi.Chat> getILoading(PreLoader preLoader) {
        return new ILoading<TdApi.Chat>() {
            @Override
            public Observable<List<TdApi.Chat>> getLoadingObservable(OffsetAndLimit offsetAndLimit) {
                return TGProxy.getInstance().sendTD(new TdApi.GetChats(offsetAndLimit.getOffset(), offsetAndLimit.getLimit()), TdApi.Chats.class)
                        .map(chats -> {
                            TdApi.Chat chatsMas[] = chats.chats;
                            return new ArrayList<>(Arrays.asList(chatsMas));
                        });
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
