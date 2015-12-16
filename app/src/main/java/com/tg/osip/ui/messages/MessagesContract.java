package com.tg.osip.ui.messages;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

/**
 * contract between view and presenter
 * @author e.matsyuk
 */
public interface MessagesContract {

    interface View {
        void updateToolBar(android.view.View toolbarView);
    }

    interface UserActionsListener {
        void bindView(MessagesContract.View messagesContractView);
        void loadViewsData(Activity activity, RecyclerView recyclerView, long chatId);
        void onDestroy();
    }

}
