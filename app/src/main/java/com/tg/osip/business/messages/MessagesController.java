package com.tg.osip.business.messages;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tg.osip.R;
import com.tg.osip.business.models.ChatItem;
import com.tg.osip.business.models.UserItem;
import com.tg.osip.business.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.ui.messages.MessagesRecyclerAdapter;
import com.tg.osip.ui.views.images.SIPAvatar;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.ui.views.auto_loading.AutoLoadingRecyclerView;
import com.tg.osip.ui.views.auto_loading.ILoading;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
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
    private WeakReference<AutoLoadingRecyclerView<TdApi.Message>> recyclerViewWeakReference;
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

    public void setRecyclerView(AutoLoadingRecyclerView<TdApi.Message> autoLoadingRecyclerView) {
        recyclerViewWeakReference = new WeakReference<>(autoLoadingRecyclerView);
        // set adapter to new or recreated recyclerView
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
                .sendTD(new TdApi.GetMe(), TdApi.User.class)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(user -> messagesRecyclerAdapter.setMyUserId(user.id))
                .concatMap(user -> TGProxy.getInstance().sendTD(new TdApi.GetChat(chatId), TdApi.Chat.class))
                .concatMap(chat -> {
                    List<TdApi.Message> messages = new ArrayList<>(1);
                    messages.add(chat.topMessage);
                    getUsersDownloadingObservable(messages).subscribe();
                    return Observable.just(chat);
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
        messagesRecyclerAdapter.addNewItem(chat.topMessage);
        messagesRecyclerAdapter.notifyItemInserted(0);
        if (recyclerViewWeakReference != null && recyclerViewWeakReference.get() != null) {
            AutoLoadingRecyclerView<TdApi.Message> autoLoadingRecyclerView = recyclerViewWeakReference.get();
            autoLoadingRecyclerView.setLoadingObservable(getILoading(chatId, chatItem.getChat().topMessage.id));
            autoLoadingRecyclerView.startLoading();
        }
    }

    private ILoading<TdApi.Message> getILoading(long chatId, int topMessageId) {
        return offsetAndLimit -> TGProxy.getInstance().sendTD(new TdApi.GetChatHistory(chatId, topMessageId, offsetAndLimit.getOffset(), offsetAndLimit.getLimit()), TdApi.Messages.class)
                .map(messages -> {
                    TdApi.Message messageMas[] = messages.messages;
                    return (List<TdApi.Message>) new ArrayList<>(Arrays.asList(messageMas));
                })
                .concatMap(messages -> {
                    getUsersDownloadingObservable(messages).subscribe();
                    return Observable.just(messages);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext((messageList) -> {
                    if (progressBarWeakReference != null && progressBarWeakReference.get() != null) {
                        progressBarWeakReference.get().setVisibility(View.GONE);
                    }
                });
    }

    private Observable<Map<Integer, UserItem>> getUsersDownloadingObservable(List<TdApi.Message> mainListItems) {
        return Observable.from(mainListItems)
//                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .map(message -> message.fromId)
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
                .doOnNext(integerUserItemMap -> {
                    if (messagesRecyclerAdapter != null) {
                        messagesRecyclerAdapter.setChatUsers(integerUserItemMap);
                    }
                });
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

        SIPAvatar headerAvatar = (SIPAvatar)headerView.findViewById(R.id.avatar);
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
            AutoLoadingRecyclerView<TdApi.Message> autoLoadingRecyclerView = recyclerViewWeakReference.get();
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
