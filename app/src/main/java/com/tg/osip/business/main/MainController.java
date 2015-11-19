package com.tg.osip.business.main;

import android.view.View;
import android.widget.TextView;

import com.tg.osip.R;
import com.tg.osip.business.models.UserItem;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.ui.views.images.SIPAvatar;
import com.tg.osip.utils.log.Logger;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * Controller for {@link com.tg.osip.ui.activities.MainActivity MainActivity}
 *
 * @author e.matsyuk
 */
public class MainController {

    // views from activity
    private WeakReference<View> viewWeakReference;
    // subscriptions
    private Subscription loadUserInfoSubscription;
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
                        bindView();
                    }
                });
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

    /**
     * Required method for memory leaks preventing
     */
    public void onDestroy() {
        if (loadUserInfoSubscription != null && !loadUserInfoSubscription.isUnsubscribed()) {
            loadUserInfoSubscription.unsubscribe();
        }
    }

}
