package com.tg.osip.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.TextView;

import com.tg.osip.R;
import com.tg.osip.business.main.MainInteract;
import com.tg.osip.business.models.UserItem;
import com.tg.osip.ui.activities.LoginActivity;
import com.tg.osip.ui.general.DefaultSubscriber;
import com.tg.osip.ui.general.views.images.PhotoView;
import com.tg.osip.utils.common.BackgroundExecutor;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author e.matsyuk
 */
public class MainPresenter implements MainContract.UserActionsListener {

    private static final int TIMER_IN_MS = 200;

    MainInteract mainInteract;

    private Subscription getMeUserSubscription;
    private Subscription logoutSubscription;
    WeakReference<MainContract.View> messagesContractViewWeak;

    public MainPresenter(MainInteract mainInteract) {
        this.mainInteract = mainInteract;
    }

    @Override
    public void logout(@NonNull Activity activity, @NonNull DrawerLayout drawerLayout) {
        drawerLayout.closeDrawers();
        logoutSubscription = Observable.timer(TIMER_IN_MS, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aLong -> {
                    if (messagesContractViewWeak != null && messagesContractViewWeak.get() != null) {
                        messagesContractViewWeak.get().showProgress();
                    }
                })
                .concatMap(aLong -> mainInteract.getLogoutObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(authState -> {
                    if (messagesContractViewWeak != null && messagesContractViewWeak.get() != null) {
                        messagesContractViewWeak.get().hideProgress();
                    }
                })
                .subscribe(new DefaultSubscriber<TdApi.AuthState>() {
                    @Override
                    public void onNext(TdApi.AuthState authState) {
                        activity.startActivity(new Intent(activity, LoginActivity.class));
                        activity.finish();
                    }
                });
    }

    @Override
    public void bindView(MainContract.View messagesContractView) {
        messagesContractViewWeak = new WeakReference<>(messagesContractView);
    }

    @Override
    public void loadDataForNavigationHeaderView(View headerNavigationView) {
        getMeUserSubscription = mainInteract.getMeUserObservable()
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userItem -> bindView(headerNavigationView, userItem));
    }

    private void bindView(View headerNavigationView, UserItem userItem) {
        if (userItem == null || headerNavigationView == null) {
            return;
        }
        PhotoView avatar = (PhotoView) headerNavigationView.findViewById(R.id.avatar);
        avatar.setCircleRounds(true);
        avatar.setImageLoaderI(userItem);
        TextView name = (TextView) headerNavigationView.findViewById(R.id.name);
        name.setText(userItem.getName());
        TextView phone = (TextView) headerNavigationView.findViewById(R.id.phone);
        phone.setText(userItem.getPhone());
    }

    @Override
    public void onDestroy() {
        if (getMeUserSubscription != null && !getMeUserSubscription.isUnsubscribed()) {
            getMeUserSubscription.unsubscribe();
        }
        if (logoutSubscription != null && !logoutSubscription.isUnsubscribed()) {
            logoutSubscription.unsubscribe();
        }
    }

}
