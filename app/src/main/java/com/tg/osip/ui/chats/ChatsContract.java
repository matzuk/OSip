package com.tg.osip.ui.chats;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

/**
 * contract between view and presenter
 * @author e.matsyuk
 */
public interface ChatsContract {

    interface View {
        void showProgress();
        void hideProgress();
    }

    interface UserActionsListener {
        void bindView(ChatsContract.View chatsContractView);
        void loadChatsList(RecyclerView recyclerView);
        void clickChatItem(AppCompatActivity activity, int position);
        void onDestroy();
    }

}
