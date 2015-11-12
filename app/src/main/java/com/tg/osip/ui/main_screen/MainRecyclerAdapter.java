package com.tg.osip.ui.main_screen;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.business.main.MainListItem;
import com.tg.osip.ui.views.common_adapters.CommonRecyclerViewAdapter;
import com.tg.osip.ui.views.images.SIPAvatar;
import com.tg.osip.ui.views.auto_loading.AutoLoadingRecyclerViewAdapter;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class MainRecyclerAdapter extends CommonRecyclerViewAdapter<MainListItem> {

    private static final int TEMP_SEND_STATE_IS_ERROR = 0;
    private static final int TEMP_SEND_STATE_IS_SENDING = 1000000000;
    private static final int DEFAULT_VALUE = 0;
    private static final int COUNT_FOR_LOADER_VIEW = 1;

    private static final int MAIN_VIEW = 0;
    private static final int LOADER_VIEW = 1;

    private int userId;

    static class MainViewHolder extends RecyclerView.ViewHolder {

        SIPAvatar avatar;
        TextView chatUserName;
        ImageView chatGroupIcon;
        TextView chatUserLastMessage;
        TextView chatMessageSendingTime;
        ImageView chatUnreadOutboxMessage;
        TextView chatUnreadMessageCount;

        public MainViewHolder(View itemView) {
            super(itemView);
            avatar = (SIPAvatar) itemView.findViewById(R.id.avatar);
            chatUserName = (TextView) itemView.findViewById(R.id.chat_user_name);
            chatGroupIcon = (ImageView) itemView.findViewById(R.id.chat_group_icon);
            chatUserLastMessage = (TextView) itemView.findViewById(R.id.chat_user_last_message);
            chatMessageSendingTime = (TextView) itemView.findViewById(R.id.chat_message_sending_time);
            chatUnreadOutboxMessage = (ImageView) itemView.findViewById(R.id.chat_unread_outbox_messages);
            chatUnreadMessageCount = (TextView) itemView.findViewById(R.id.chat_unread_messages_count);
        }
    }

    static class LoaderViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout layout;
        public LoaderViewHolder(View itemView) {
            super(itemView);
            layout = (RelativeLayout) itemView.findViewById(R.id.layout);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MAIN_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_list, parent, false);
            return new MainViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loader_list, parent, false);
            return new LoaderViewHolder(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isFirstPortionLoaded()) {
            return MAIN_VIEW;
        } else {
            return LOADER_VIEW;
        }
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == MAIN_VIEW) {
            return getItem(position).getApiChat().id;
        } else {
            return DEFAULT_VALUE;
        }
    }

    @Override
    public int getItemCount() {
        if (getItemViewType(DEFAULT_VALUE) == MAIN_VIEW) {
            return super.getItemCount();
        } else {
            return COUNT_FOR_LOADER_VIEW;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case MAIN_VIEW:
                onBindMainHolder(holder, position);
                break;
            case LOADER_VIEW:
                onBindLoaderHolder(holder, position);
                break;
        }
    }

    public void onBindLoaderHolder(RecyclerView.ViewHolder holder, int position) {
        LoaderViewHolder loaderViewHolder = (LoaderViewHolder) holder;
    }

    public void onBindMainHolder(RecyclerView.ViewHolder holder, int position) {
        MainViewHolder mainHolder = (MainViewHolder) holder;
        MainListItem mainListItem = getItem(position);
        TdApi.Chat concreteChat = mainListItem.getApiChat();
        if (concreteChat == null) {
            return;
        }
        // set unread count
        if (concreteChat.unreadCount > 0) {
            mainHolder.chatUnreadMessageCount.setVisibility(View.VISIBLE);
            mainHolder.chatUnreadMessageCount.setText(String.valueOf(concreteChat.unreadCount));
        } else {
            mainHolder.chatUnreadMessageCount.setVisibility(View.GONE);
        }
        // set error in messaged sending
        if (concreteChat.topMessage.date == TEMP_SEND_STATE_IS_ERROR) {
            mainHolder.chatUnreadMessageCount.setBackgroundResource(R.drawable.ic_error);
            mainHolder.chatUnreadMessageCount.setText("");
        }
        // get last message and set date
        TdApi.Message message = concreteChat.topMessage;
        if (message != null) {
            // set is last message from account
            String textYou = "";
            if (getUserId() == concreteChat.topMessage.fromId) {
                textYou = ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_text_you) + " ";
            }

            // very heavy operation for list
            String dataString = getItem(position).getLastMessageDate();
            mainHolder.chatMessageSendingTime.setText(dataString);
            // get message content and set last message
            mainHolder.chatUserLastMessage.setText(textYou + mainListItem.getLastMessageText());
        }
        // get ChatInfo
        TdApi.ChatInfo chatInfo = concreteChat.type;
        if (chatInfo != null) {
            //  Set name
            mainHolder.chatUserName.setText(mainListItem.getUserName());
            // Set avatar
            mainHolder.avatar.setMainListItem(mainListItem);
        }
        // is chat group?
        mainHolder.chatGroupIcon.setVisibility(mainListItem.isGroupChat()? View.VISIBLE : View.GONE);
        // set unread outbox image
        if (getUserId() == concreteChat.topMessage.fromId) {
            if (concreteChat.lastReadOutboxMessageId >= concreteChat.topMessage.id) {
                mainHolder.chatUnreadOutboxMessage.setVisibility(View.GONE);
            } else {
                mainHolder.chatUnreadOutboxMessage.setVisibility(View.VISIBLE);
                setSendStateMessage(concreteChat, mainHolder.chatUnreadOutboxMessage);
            }
        } else {
            mainHolder.chatUnreadOutboxMessage.setVisibility(View.GONE);
        }

    }

    private void setSendStateMessage(TdApi.Chat chat, ImageView imageView) {
        if (chat.topMessage.id >= TEMP_SEND_STATE_IS_SENDING) {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_clock, ApplicationSIP.applicationContext.getTheme()));
        } else {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_not_read, ApplicationSIP.applicationContext.getTheme()));
        }
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

}
