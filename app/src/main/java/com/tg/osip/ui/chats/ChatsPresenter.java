package com.tg.osip.ui.chats;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.tg.osip.R;
import com.tg.osip.business.PersistentInfo;
import com.tg.osip.business.chats.ChatsInteract;
import com.tg.osip.business.models.ChatItem;
import com.tg.osip.ui.general.DefaultSubscriber;
import com.tg.osip.ui.general.views.pagination.PaginationTool;
import com.tg.osip.ui.messages.MessagesFragment;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author e.matsyuk
 */
public class ChatsPresenter implements ChatsContract.UserActionsListener {

    private static final int LIMIT = 50;

    ChatsInteract chatsInteract;

    private ChatRecyclerAdapter chatRecyclerAdapter;
    private Subscription listPagingSubscription;
    private ChatsContract.View chatsContractView;

    public ChatsPresenter(ChatsInteract chatsInteract) {
        this.chatsInteract = chatsInteract;
    }

    @Override
    public void bindView(ChatsContract.View chatsContractView) {
        this.chatsContractView = chatsContractView;
    }

    @Override
    public void loadChatsList(RecyclerView recyclerView) {
        // first start
        if (chatRecyclerAdapter == null) {
            // start progressbar
            chatsContractView.showProgress();
            // init Controller
            chatRecyclerAdapter = new ChatRecyclerAdapter();
            chatRecyclerAdapter.setMyUserId(PersistentInfo.getInstance().getMeUserId());
        }
        recyclerView.setAdapter(chatRecyclerAdapter);
        startListPaging(recyclerView);
    }

    private void startListPaging(RecyclerView recyclerView) {
        if (chatRecyclerAdapter.isAllItemsLoaded()) {
            return;
        }
        listPagingSubscription = PaginationTool
                .paging(recyclerView, offset -> chatsInteract.getNextDataPortionInList(offset, LIMIT))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<List<ChatItem>>() {
                    @Override
                    public void onNext(List<ChatItem> chatItems) {
                        // hide progressbar
                        chatsContractView.hideProgress();
                        chatRecyclerAdapter.addNewItems(chatItems);
                    }
                });
    }

    @Override
    public void clickChatItem(AppCompatActivity activity, int position) {
        long chatId = chatRecyclerAdapter.getItem(position).getChat().id;
        MessagesFragment messagesFragment = MessagesFragment.newInstance(chatId);
        startFragment(activity, messagesFragment);
    }

    private void startFragment(AppCompatActivity activity, Fragment fragment) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    @Override
    public void onDestroy() {
        if (listPagingSubscription != null && !listPagingSubscription.isUnsubscribed()) {
            listPagingSubscription.unsubscribe();
        }
    }

}
