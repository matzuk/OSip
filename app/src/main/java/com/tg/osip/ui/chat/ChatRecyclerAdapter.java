package com.tg.osip.ui.chat;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.business.chat.UserChatListItem;
import com.tg.osip.ui.views.auto_loading.AutoLoadingRecyclerViewAdapter;
import com.tg.osip.ui.views.images.SIPAvatar;
import com.tg.osip.utils.time.TimeUtils;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.HashMap;
import java.util.Map;

/**
 * @author e.matsyuk
 */
public class ChatRecyclerAdapter extends AutoLoadingRecyclerViewAdapter<TdApi.Message> {

    private static final int TEMP_SEND_STATE_IS_SENDING = 1000000000;
    private static final int TEMP_SEND_STATE_IS_ERROR = 0;

    private static final int MAIN_VIEW = 0;
    private static final int UNSUPPORTED_VIEW = 4;

    private int myUserId;
    private int lastChatReadOutboxId;
    private Map<Integer, UserChatListItem> usersMap = new HashMap<>();

    static class MainViewHolder extends RecyclerView.ViewHolder {

        SIPAvatar avatar;
        TextView chatMessageName;
        TextView chatMessageText;
        TextView chatMessageSendingTime;
        ImageView chatMessageUnreadOutbox;

        public MainViewHolder(View itemView) {
            super(itemView);
            avatar = (SIPAvatar) itemView.findViewById(R.id.avatar);
            chatMessageName = (TextView) itemView.findViewById(R.id.chat_message_name);
            chatMessageText = (TextView) itemView.findViewById(R.id.chat_message_text);
            chatMessageSendingTime = (TextView) itemView.findViewById(R.id.chat_message_sending_time);
            chatMessageUnreadOutbox = (ImageView) itemView.findViewById(R.id.chat_message_unread_outbox);
        }
    }

    static class UnsupportedViewHolder extends RecyclerView.ViewHolder {

        SIPAvatar avatar;
        TextView chatMessageName;
        TextView chatMessageText;
        TextView chatMessageSendingTime;

        public UnsupportedViewHolder(View itemView) {
            super(itemView);
            avatar = (SIPAvatar) itemView.findViewById(R.id.avatar);
            chatMessageName = (TextView) itemView.findViewById(R.id.chat_message_name);
            chatMessageText = (TextView) itemView.findViewById(R.id.chat_message_text);
            chatMessageSendingTime = (TextView) itemView.findViewById(R.id.chat_message_sending_time);
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MAIN_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_list_message_text, parent, false);
            return new MainViewHolder(v);
        } else if (viewType == UNSUPPORTED_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_list_message_unsupport, parent, false);
            return new UnsupportedViewHolder(v);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).message.getClass() == TdApi.MessageText.class) {
            return MAIN_VIEW;
        }
        return UNSUPPORTED_VIEW;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case MAIN_VIEW:
                onBindTextHolder(holder, position);
                break;
            case UNSUPPORTED_VIEW:
                onBindUnsupportedHolder(holder, position);
                break;
        }
    }

    private void onBindTextHolder(RecyclerView.ViewHolder holder, int position) {
        MainViewHolder mainHolder = (MainViewHolder) holder;
        TdApi.Message message = getItem(position);
        if (message == null) {
            return;
        }
        TdApi.MessageContent messageContent = message.message;
        if (messageContent == null) {
            return;
        }
        if (messageContent instanceof TdApi.MessageText) {
            TdApi.MessageText messageText = (TdApi.MessageText)messageContent;
            mainHolder.chatMessageText.setText(messageText.text);
        }

        UserChatListItem user = usersMap.get(message.fromId);
        // set name
        String name = user.getUser().firstName + " " + user.getUser().lastName;
        mainHolder.chatMessageName.setText(name);
        // Set avatar
        mainHolder.avatar.setImageLoaderI(user);

        String dataString = TimeUtils.stringForMessageListDate(message.date);
        mainHolder.chatMessageSendingTime.setText(dataString);

        // set unread outbox image
        if (message.date == TEMP_SEND_STATE_IS_ERROR) {
            mainHolder.chatMessageUnreadOutbox.setVisibility(View.VISIBLE);
            mainHolder.chatMessageUnreadOutbox.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_message_error, ApplicationSIP.applicationContext.getTheme()));
        } else if (myUserId == message.fromId) {
            if (lastChatReadOutboxId >= message.id) {
                mainHolder.chatMessageUnreadOutbox.setVisibility(View.GONE);
            } else {
                mainHolder.chatMessageUnreadOutbox.setVisibility(View.VISIBLE);
                setSendStateMessage(message, mainHolder.chatMessageUnreadOutbox);
            }
        } else {
            mainHolder.chatMessageUnreadOutbox.setVisibility(View.GONE);
        }
    }

    private void setSendStateMessage(TdApi.Message message, ImageView imageView) {
        if (message.id >= TEMP_SEND_STATE_IS_SENDING) {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_clock, ApplicationSIP.applicationContext.getTheme()));
        } else {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_not_read, ApplicationSIP.applicationContext.getTheme()));
        }
    }

    private void onBindUnsupportedHolder(RecyclerView.ViewHolder holder, int position) {
        UnsupportedViewHolder unsupportedHolder = (UnsupportedViewHolder) holder;
        TdApi.Message message = getItem(position);
        if (message == null) {
            return;
        }
        TdApi.MessageContent messageContent = message.message;
        if (messageContent == null) {
            return;
        }

        UserChatListItem user = usersMap.get(message.fromId);
        // set name
        String name = user.getUser().firstName + " " + user.getUser().lastName;
        unsupportedHolder.chatMessageName.setText(name);
        // Set avatar
        unsupportedHolder.avatar.setImageLoaderI(user);

        String dataString = TimeUtils.stringForMessageListDate(message.date);
        unsupportedHolder.chatMessageSendingTime.setText(dataString);

    }

    public void setMyUserId(int myUserId) {
        this.myUserId = myUserId;
    }

    public int getMyUserId() {
        return myUserId;
    }

    public void setLastChatReadOutboxId(int lastChatReadOutboxId) {
        this.lastChatReadOutboxId = lastChatReadOutboxId;
    }

    public void setChatUsers(Map<Integer, UserChatListItem> users) {
        usersMap.putAll(users);
    }

}
