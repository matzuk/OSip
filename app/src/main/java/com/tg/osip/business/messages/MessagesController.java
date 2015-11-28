package com.tg.osip.business.messages;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tg.osip.R;
import com.tg.osip.business.models.ChatItem;
import com.tg.osip.business.models.MessageItem;
import com.tg.osip.business.models.PhotoItem;
import com.tg.osip.business.models.UserItem;
import com.tg.osip.business.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.ui.messages.MessagesRecyclerAdapter;
import com.tg.osip.ui.general.views.images.PhotoView;
import com.tg.osip.ui.messages.OnMessageClickListener;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.ui.general.views.auto_loading.AutoLoadingRecyclerView;
import com.tg.osip.ui.general.views.auto_loading.ILoading;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Controller for Chat screen (MessagesFragment)
 *
 * @author e.matsyuk
 */
public class MessagesController {

    // views from fragment
    private WeakReference<ProgressBar> progressBarWeakReference;
    private WeakReference<AutoLoadingRecyclerView<MessageItem>> recyclerViewWeakReference;
    private WeakReference<Context> contextWeakReference;
    private WeakReference<Toolbar> toolbarWeakReference;
    // views was created in controller
    private View headerView;
    // adapters
    private MessagesRecyclerAdapter messagesRecyclerAdapter;
    // subscriptions
    private Subscription firstStartRecyclerViewSubscription;
    // needed members
    private ChatItem chatItem;
    private long chatId;

    public MessagesController(long chatId) {
        this.chatId = chatId;
        initAdapters();
    }

    private void initAdapters() {
        messagesRecyclerAdapter = new MessagesRecyclerAdapter();
        messagesRecyclerAdapter.setHasStableIds(true);
    }

    public void setProgressBar(ProgressBar progressBar) {
        progressBarWeakReference = new WeakReference<>(progressBar);
    }

    public void setRecyclerView(AutoLoadingRecyclerView<MessageItem> autoLoadingRecyclerView, OnMessageClickListener onMessageClickListener) {
        recyclerViewWeakReference = new WeakReference<>(autoLoadingRecyclerView);
        // set adapter to new or recreated recyclerView
        messagesRecyclerAdapter.setOnMessageClickListener(onMessageClickListener);
        autoLoadingRecyclerView.setAdapter(messagesRecyclerAdapter);
    }

    public void setToolbar(Context context, Toolbar toolbar) {
        contextWeakReference = new WeakReference<>(context);
        toolbarWeakReference = new WeakReference<>(toolbar);
        initToolbar();
    }

    /**
     * load data to views
     */
    public void loadData() {
        if (firstStartRecyclerViewSubscription != null) {
            return;
        }
        // load needed data (in adapters and members) in the first start
        firstStartRecyclerViewSubscription = TGProxy.getInstance()
                // get // get my info
                .sendTD(new TdApi.GetMe(), TdApi.User.class)
                .observeOn(AndroidSchedulers.mainThread())
                // set myUserId to adapter
                .doOnNext(user -> messagesRecyclerAdapter.setMyUserId(user.id))
                .concatMap(user -> TGProxy.getInstance().sendTD(new TdApi.GetChat(chatId), TdApi.Chat.class))
                .concatMap(chat -> {
                    List<MessageItem> messages = new ArrayList<>(1);
                    messages.add(new MessageItem(chat.topMessage));
                    // download photo content from messages in another Stream
                    getPhotoTypeMDownloadingObservable(messages).subscribe();
                    // download users info for messages adapter in this Stream
                    return Observable.zip(getUsersDownloadingObservable(messages), Observable.just(chat), (integerUserItemMap, chat1) -> chat1);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TdApi.Chat>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(e);
                    }

                    @Override
                    public void onNext(TdApi.Chat chat) {
                        successLoadData(chat);
                    }
                });
    }

    private void successLoadData(TdApi.Chat chat) {
        chatItem = new ChatItem(chat);
        initToolbar();
        messagesRecyclerAdapter.setLastChatReadOutboxId(chat.lastReadOutboxMessageId);
        messagesRecyclerAdapter.addNewItem(new MessageItem(chat.topMessage));
        messagesRecyclerAdapter.notifyItemInserted(0);
        if (recyclerViewWeakReference != null && recyclerViewWeakReference.get() != null) {
            AutoLoadingRecyclerView<MessageItem> autoLoadingRecyclerView = recyclerViewWeakReference.get();
            autoLoadingRecyclerView.setLoadingObservable(getILoading(chatId, chatItem.getChat().topMessage.id));
            autoLoadingRecyclerView.startLoading();
        }
    }

    private ILoading<MessageItem> getILoading(long chatId, int topMessageId) {
        return offsetAndLimit -> TGProxy.getInstance().sendTD(new TdApi.GetChatHistory(chatId, topMessageId, offsetAndLimit.getOffset(), offsetAndLimit.getLimit()), TdApi.Messages.class)
                .map(messages -> {
                    List<MessageItem> messageItemList = new ArrayList<>();
                    for (TdApi.Message message : messages.messages) {
                        messageItemList.add(new MessageItem(message));
                    }
                    return messageItemList;
                })
                .concatMap(messages -> {
                    // download photo content from messages in another Stream
                    getPhotoTypeMDownloadingObservable(messages).subscribe();
                    // download users info for messages adapter in this Stream
                    return Observable.zip(getUsersDownloadingObservable(messages), Observable.just(messages), (integerUserItemMap, messageItems) -> messageItems);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext((messageList) -> {
                    // hide start progressbar
                    if (progressBarWeakReference != null && progressBarWeakReference.get() != null && progressBarWeakReference.get().getVisibility() == View.VISIBLE) {
                        progressBarWeakReference.get().setVisibility(View.GONE);
                    }
                });
    }

    private Observable<Map<Integer, UserItem>> getUsersDownloadingObservable(List<MessageItem> mainListItems) {
        return Observable.from(mainListItems)
                .map(message -> message.getMessage().fromId)
                .distinct()
                .concatMap(integer -> TGProxy.getInstance().sendTD(new TdApi.GetUser(integer), TdApi.User.class))
                .map(UserItem::new)
                .toList()
                .doOnNext(userChatListItems -> FileDownloaderManager.getInstance().startFileListDownloading(userChatListItems))
                .map(users -> {
                    Map<Integer, UserItem> map = new HashMap<>();
                    for (UserItem user : users) {
                        map.put(user.getUser().id, user);
                    }
                    return map;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(integerUserItemMap -> {
                    if (messagesRecyclerAdapter != null) {
                        messagesRecyclerAdapter.setChatUsers(integerUserItemMap);
                    }
                });
    }

    private Observable<List<PhotoItem>> getPhotoTypeMDownloadingObservable(List<MessageItem> mainListItems) {
        return Observable.from(mainListItems)
                .filter(MessageItem::isPhotoMessage)
                .map(MessageItem::getPhotoItemM)
                .toList()
                .doOnNext(photoItems -> FileDownloaderManager.getInstance().startFileListDownloading(photoItems));
    }

    private void initToolbar() {
        if (chatItem == null || contextWeakReference == null || contextWeakReference.get() == null ||
                toolbarWeakReference == null || toolbarWeakReference.get() == null) {
            return;
        }
        Context context = contextWeakReference.get();
        Toolbar toolbar = toolbarWeakReference.get();

        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        headerView = layoutInflater.inflate(R.layout.toolbar_messages, null);

        PhotoView headerAvatar = (PhotoView)headerView.findViewById(R.id.avatar);
        headerAvatar.setImageLoaderI(chatItem);
        // not start file downloading because all chats avatars downloading was started in ChatsController

        TextView chatNameView = (TextView)headerView.findViewById(R.id.chat_name);
        chatNameView.setText(chatItem.getUserName());

        TextView chatInfoView = (TextView)headerView.findViewById(R.id.chat_info);
        chatInfoView.setText(chatItem.getInfo());

        toolbar.addView(headerView);
    }

    /**
     * restore data to recreated views
     */
    public void restoreDataToViews() {
        if (recyclerViewWeakReference != null && recyclerViewWeakReference.get() != null) {
            AutoLoadingRecyclerView<MessageItem> autoLoadingRecyclerView = recyclerViewWeakReference.get();
            autoLoadingRecyclerView.setLoadingObservable(getILoading(chatId, chatItem.getChat().topMessage.id));
            autoLoadingRecyclerView.startLoading();
        }
    }

    /**
     * Required method for memory leaks preventing
     */
    public void onDestroy() {
        if (firstStartRecyclerViewSubscription != null && !firstStartRecyclerViewSubscription.isUnsubscribed()) {
            firstStartRecyclerViewSubscription.unsubscribe();
        }
        if (toolbarWeakReference != null && toolbarWeakReference.get() != null && headerView != null) {
            Toolbar toolbar = toolbarWeakReference.get();
            toolbar.removeView(headerView);
        }
    }

}
