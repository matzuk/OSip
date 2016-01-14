package com.tg.osip.ui.main;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;

/**
 * contract between view and presenter
 * @author e.matsyuk
 */
public interface MainContract {

    interface View {
        void showProgress();
        void hideProgress();
    }

    interface UserActionsListener {
        void bindView(MainContract.View messagesContractView);
        void logout(Activity activity, DrawerLayout drawerLayout);
        void loadDataForNavigationHeaderView(android.view.View headerNavigationView);
        void onDestroy();
    }

}
