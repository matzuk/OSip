package com.tg.osip.business.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tg.osip.R;
import com.tg.osip.business.models.UserItem;
import com.tg.osip.business.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.ui.activities.LoginActivity;
import com.tg.osip.ui.views.images.SIPAvatar;
import com.tg.osip.utils.common.BackgroundExecutor;
import com.tg.osip.utils.log.Logger;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Controller for {@link com.tg.osip.ui.activities.MainActivity MainActivity}
 *
 * @author e.matsyuk
 */
public class MainController {

    private static final int TIMER_IN_MS = 200;

    // views from activity
    private WeakReference<View> viewWeakReference;
    // views
    private Dialog logoutProgressDialog;
    // subscriptions
    private Subscription loadUserInfoSubscription;
    private Subscription logoutSubscription;
    // needed members
    private UserItem userItem;

    public void setHeaderNavigationView(View view) {
        viewWeakReference = new WeakReference<>(view);
        if (userItem == null) {
            loadUserInfo();
        } else {
            bindView();
        }
    }

    private void loadUserInfo() {
        loadUserInfoSubscription = TGProxy.getInstance()
                .sendTD(new TdApi.GetMe(), TdApi.User.class)
                .map(UserItem::new)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserItem>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(UserItem userItem) {
                        Logger.debug("userItem data loaded, recyclerview is next");
                        MainController.this.userItem = userItem;
                        FileDownloaderManager.getInstance().startFileDownloading(userItem);
                        bindView();
                    }
                });
    }

    private Dialog createProgressDialog(Context context) {
        return new MaterialDialog.Builder(context)
                .cancelable(false)
                .content(context.getResources().getString(R.string.wait))
                .progress(true, 0)
                .show();
    }

    private void bindView() {
        if (viewWeakReference == null || viewWeakReference.get() == null || userItem == null) {
            return;
        }
        View view = viewWeakReference.get();

        SIPAvatar avatar = (SIPAvatar) view.findViewById(R.id.avatar);
        avatar.setImageLoaderI(userItem);
        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(userItem.getName());
        TextView phone = (TextView) view.findViewById(R.id.phone);
        phone.setText(userItem.getPhone());
    }

    public void logout(Activity activity) {
        // timer for more smooth animation (when DrawerLayout is hiding)
        logoutSubscription = Observable.timer(TIMER_IN_MS, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aLong -> {
                    logoutProgressDialog = createProgressDialog(activity);
                    logoutProgressDialog.show();
                })
                .concatMap(aLong -> TGProxy.getInstance().sendTD(new TdApi.ResetAuth(false), TdApi.AuthState.class))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TdApi.AuthState>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(TdApi.AuthState authState) {
                        if (logoutProgressDialog != null && logoutProgressDialog.isShowing()) {
                            logoutProgressDialog.dismiss();
                        }
                        if (activity != null) {
                            activity.startActivity(new Intent(activity, LoginActivity.class));
                            activity.finish();
                        }
                    }
                });
    }

    /**
     * Required method for memory leaks preventing
     */
    public void onDestroy() {
        if (logoutProgressDialog != null && logoutProgressDialog.isShowing()) {
            logoutProgressDialog.dismiss();
        }
        if (loadUserInfoSubscription != null && !loadUserInfoSubscription.isUnsubscribed()) {
            loadUserInfoSubscription.unsubscribe();
        }
        if (logoutSubscription != null && !logoutSubscription.isUnsubscribed()) {
            logoutSubscription.unsubscribe();
        }
    }

}
