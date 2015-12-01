package com.tg.osip.ui.chats;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.business.models.ChatItem;
import com.tg.osip.ui.general.views.images.PhotoView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.List;

/**
 * @author e.matsyuk
 */
public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TEMP_SEND_STATE_IS_ERROR = 0;
    private static final int TEMP_SEND_STATE_IS_SENDING = 1000000000;
    private static final int EMPTY_LIST = 0;

    private static final int MAIN_VIEW = 0;

    private int myUserId;
    protected List<ChatItem> listElements = new ArrayList<>();
    // after reorientation test this member
    // or one extra request will be sent after each reorientation
    private boolean allItemsLoaded;

    static class MainViewHolder extends RecyclerView.ViewHolder {

        PhotoView avatar;
        TextView chatUserName;
        ImageView chatGroupIcon;
        TextView chatUserLastMessage;
        TextView chatMessageSendingTime;
        ImageView chatUnreadOutboxMessage;
        TextView chatUnreadMessageCount;

        public MainViewHolder(View itemView) {
            super(itemView);
            avatar = (PhotoView) itemView.findViewById(R.id.avatar);
            chatUserName = (TextView) itemView.findViewById(R.id.chat_user_name);
            chatGroupIcon = (ImageView) itemView.findViewById(R.id.chat_group_icon);
            chatUserLastMessage = (TextView) itemView.findViewById(R.id.chat_user_last_message);
            chatMessageSendingTime = (TextView) itemView.findViewById(R.id.message_sending_time);
            chatUnreadOutboxMessage = (ImageView) itemView.findViewById(R.id.chat_unread_outbox_messages);
            chatUnreadMessageCount = (TextView) itemView.findViewById(R.id.chat_unread_messages_count);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MAIN_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_list, parent, false);
            return new MainViewHolder(v);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return MAIN_VIEW;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getChat().id;
    }

    public ChatItem getItem(int position) {
        return listElements.get(position);
    }

    @Override
    public int getItemCount() {
        return listElements.size();
    }

    public boolean isAllItemsLoaded() {
        return allItemsLoaded;
    }

    public void addNewItems(List<ChatItem> items) {
        if (items.size() == EMPTY_LIST) {
            allItemsLoaded = true;
            return;
        }
        listElements.addAll(items);
        notifyItemInserted(getItemCount() - listElements.size());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case MAIN_VIEW:
                onBindMainHolder(holder, position);
                break;
        }
    }

    public void onBindMainHolder(RecyclerView.ViewHolder holder, int position) {
        MainViewHolder mainHolder = (MainViewHolder) holder;
        ChatItem chatItem = getItem(position);
        TdApi.Chat concreteChat = chatItem.getChat();
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
            if (getMyUserId() == concreteChat.topMessage.fromId) {
                textYou = ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_text_you) + " ";
            }

            // very heavy operation for list
            String dataString = getItem(position).getLastMessageDate();
            mainHolder.chatMessageSendingTime.setText(dataString);
            // get message content and set last message
            String lastMessageText = textYou + chatItem.getLastMessageText();
            mainHolder.chatUserLastMessage.setText(lastMessageText);
        }
        // get ChatInfo
        TdApi.ChatInfo chatInfo = concreteChat.type;
        if (chatInfo != null) {
            //  Set name
            mainHolder.chatUserName.setText(chatItem.getUserName());
            // Set avatar
            mainHolder.avatar.setCircleRounds(true);
            mainHolder.avatar.setImageLoaderI(chatItem);
        }
        // is chat group?
        mainHolder.chatGroupIcon.setVisibility(chatItem.isGroupChat()? View.VISIBLE : View.GONE);
        // set unread outbox image
        if (getMyUserId() == concreteChat.topMessage.fromId) {
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

    public void setMyUserId(int myUserId) {
        this.myUserId = myUserId;
    }

    public int getMyUserId() {
        return myUserId;
    }

}
