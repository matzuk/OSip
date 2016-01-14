package com.tg.osip.ui.messages;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;

import com.tg.osip.business.PersistentInfo;
import com.tg.osip.business.media.MediaManager;
import com.tg.osip.business.messages.MessagesInteract;
import com.tg.osip.business.models.messages.MessageAdapterModel;
import com.tg.osip.business.models.PhotoItem;
import com.tg.osip.ui.activities.PhotoMediaActivity;
import com.tg.osip.ui.general.views.pagination.PaginationTool;
import com.tg.osip.utils.common.BackgroundExecutor;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author e.matsyuk
 */
public class MessagesPresenter implements MessagesContract.UserActionsListener {

    private static final int LIMIT = 50;
    // one item from previous screen (chats)
    private static final int EMPTY_COUNT_LIST = 1;

    MessagesInteract messagesInteract;
    PersistentInfo persistentInfo;
    MediaManager mediaManager;

    private WeakReference<MessagesContract.View> messagesContractViewWeakReference;
    private MessagesRecyclerAdapter messagesRecyclerAdapter;
    private OnMessageClickListener onMessageClickListener;

    private Subscription loadDataSubscription;

    public MessagesPresenter(MessagesInteract messagesInteract, PersistentInfo persistentInfo, MediaManager mediaManager) {
        this.messagesInteract = messagesInteract;
        this.mediaManager = mediaManager;
        messagesRecyclerAdapter = new MessagesRecyclerAdapter(persistentInfo.getMeUserId());
    }

    @Override
    public void bindView(MessagesContract.View messagesContractView) {
        this.messagesContractViewWeakReference = new WeakReference<>(messagesContractView);
    }

    @Override
    public void loadViewsData(Activity activity, RecyclerView recyclerView, long chatId) {
        messagesRecyclerAdapter.setOnMessageClickListener(getOnMessageClickListener(activity));
        recyclerView.setAdapter(messagesRecyclerAdapter);
        loadData(activity, recyclerView, chatId);
    }

    private OnMessageClickListener getOnMessageClickListener(Activity activity) {
        onMessageClickListener = new OnMessageClickListener() {
            @Override
            public void onPhotoMessageClick(int clickedPosition, List<PhotoItem> photoLargeItemList) {
                Intent intent = new Intent(activity, PhotoMediaActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(PhotoMediaActivity.CLICKED_POSITION, clickedPosition);
                bundle.putSerializable(PhotoMediaActivity.PHOTO_LARGE, (Serializable) photoLargeItemList);
                intent.putExtras(bundle);
                activity.startActivity(intent);
            }
        };
        return onMessageClickListener;
    }

    private void loadData(Activity activity, RecyclerView recyclerView, long chatId) {
        loadDataSubscription = messagesInteract.getFirstChatData(chatId)
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(chatMessageAdapterModelPair -> getChatLoadingSuccess(activity, chatMessageAdapterModelPair))
                .concatMap(chatMessageAdapterModelPair -> getMessagesPagingObservable(recyclerView, chatId, chatMessageAdapterModelPair.first.topMessage.id))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::getNextDataPortionSuccess);
    }

    private Observable<MessageAdapterModel> getMessagesPagingObservable(RecyclerView recyclerView, long chatId, int topMessageId) {
        PaginationTool<MessageAdapterModel> paginationTool = PaginationTool.buildPagingObservable(
                recyclerView,
                offset -> messagesInteract.getNextDataPortionInList(offset, LIMIT, chatId, topMessageId))
                .setLimit(LIMIT)
                .setEmptyListCount(EMPTY_COUNT_LIST)
                .build();
        return paginationTool.getPagingObservable();
    }

    private void getChatLoadingSuccess(Activity activity, Pair<TdApi.Chat, MessageAdapterModel> chatMessageAdapterModelPair) {
        // toolbar
        if (messagesContractViewWeakReference != null && messagesContractViewWeakReference.get() != null) {
            messagesContractViewWeakReference.get().updateToolBar(MessageToolbarViewFactory.getUserToolbarView(activity, chatMessageAdapterModelPair.first));
        }
        // adapter
        // add lastChatReadOutboxId field
        // add message from Chat topMessage only one
        if (messagesRecyclerAdapter.getItemCount() < EMPTY_COUNT_LIST) {
            messagesRecyclerAdapter.setLastChatReadOutboxId(chatMessageAdapterModelPair.first.lastReadOutboxMessageId);
            MessageAdapterModel messageAdapterModel = chatMessageAdapterModelPair.second;
            messagesRecyclerAdapter.addMessageAdapterModel(messageAdapterModel);
        }
    }

    private void getNextDataPortionSuccess(MessageAdapterModel messageAdapterModel) {
        // adapter
        messagesRecyclerAdapter.addMessageAdapterModel(messageAdapterModel);
    }

    @Override
    public void onDestroy() {
        mediaManager.reset();
        if (loadDataSubscription != null && !loadDataSubscription.isUnsubscribed()) {
            loadDataSubscription.unsubscribe();
        }
    }
}
