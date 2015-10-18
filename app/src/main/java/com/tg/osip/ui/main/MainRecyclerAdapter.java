package com.tg.osip.ui.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.ui.views.auto_loading.AutoLoadingRecyclerViewAdapter;
import com.tg.osip.utils.time.TimeUtils;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class MainRecyclerAdapter extends AutoLoadingRecyclerViewAdapter<TdApi.Chat>  {

    private static final int TEMP_SEND_STATE_IS_ERROR = 0;

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView avatar;
        TextView chatUserName;
        ImageView chatGroupIcon;
        TextView chatUserLastMessage;
        TextView chatMessageSendingTime;
        ImageView chatUnreadOutboxMessage;
        TextView chatUnreadMessageCount;

        public ViewHolder(View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            chatUserName = (TextView) itemView.findViewById(R.id.chat_user_name);
            chatGroupIcon = (ImageView) itemView.findViewById(R.id.chat_group_icon);
            chatUserLastMessage = (TextView) itemView.findViewById(R.id.chat_user_last_message);
            chatMessageSendingTime = (TextView) itemView.findViewById(R.id.chat_message_sending_time);
            chatUnreadOutboxMessage = (ImageView) itemView.findViewById(R.id.chat_unread_outbox_messages);
            chatUnreadMessageCount = (TextView) itemView.findViewById(R.id.chat_unread_messages_count);
        }
    }

    @Override
    public MainRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder mainHolder = (ViewHolder) holder;
        TdApi.Chat concreteChat = getItem(position);
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
//            if (TGProxy.getInstance().getAccountId() == concreteChat.topMessage.fromId) {
//                textYou = ApplicationLoader.applicationContext.getResources().getString(R.string.chat_list_message_text_you);
//            }

            // very heavy operation for list
//            String dataString = TimeUtils.stringForMessageListDate(message.date);
            String dataString = "";
            mainHolder.chatMessageSendingTime.setText(dataString);
            // get message content and set last message
            mainHolder.chatUserLastMessage.setText(textYou + " " + getChatLastMessage(message));
        }
        // get ChatInfo
        TdApi.ChatInfo chatInfo = concreteChat.type;
        if (chatInfo != null) {
            //  Set name
            mainHolder.chatUserName.setText(getName(chatInfo));
            // Set avatar
//            TDLibUtils.setLetterDrawable(holder.avatar, chatInfo);
//            if (PicassoProxy.getInstance().containInCache(chatInfo)) {
//                Picasso.with(
//                        ApplicationLoader.applicationContext).
//                        load(PicassoProxy.getInstance().getFilePath(chatInfo)).
//                        placeholder(holder.avatar.getDrawable()).
//                        transform(new CirclePicassoTransformation()).
//                        into(holder.avatar
//                        );
//            } else {
//                PicassoProxy.getInstance().downloadFile(chatInfo);
//            }
        }
        // is chat group?
        mainHolder.chatGroupIcon.setVisibility(isChatGroup(concreteChat.type)? View.VISIBLE : View.GONE);
        // set unread outbox image
//        if (TGProxy.getInstance().getAccountId() == concreteChat.topMessage.fromId) {
//            if (concreteChat.lastReadOutboxMessageId >= concreteChat.topMessage.id) {
//                holder.chatUnreadOutboxMessage.setVisibility(View.GONE);
//            } else {
//                holder.chatUnreadOutboxMessage.setVisibility(View.VISIBLE);
//                setSendStateMessage(concreteChat, holder.chatUnreadOutboxMessage);
//            }
//        } else {
//            holder.chatUnreadOutboxMessage.setVisibility(View.GONE);
//        }

    }

    // FIXME: add another message types
    private String getChatLastMessage(TdApi.Message message) {
        TdApi.MessageContent messageContent = message.message;
        if (messageContent == null) {
            return "";
        }
        if (messageContent instanceof TdApi.MessageText) {
            return ((TdApi.MessageText)messageContent).text;
        }
        if (messageContent instanceof TdApi.MessageAudio) {
            return ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_type_audio);
        }
        if (messageContent instanceof TdApi.MessageVideo) {
            return ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_type_video);
        }
        if (messageContent instanceof TdApi.MessagePhoto) {
            return ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_type_photo);
        }
        if (messageContent instanceof TdApi.MessageSticker) {
            return ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_type_sticker);
        }
        return ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_type_other);
    }

    private String getName(TdApi.ChatInfo chatInfo) {
        if (chatInfo instanceof TdApi.GroupChatInfo) {
            return ((TdApi.GroupChatInfo)chatInfo).groupChat.title;
        }
        if (chatInfo instanceof TdApi.PrivateChatInfo) {
            return ((TdApi.PrivateChatInfo)chatInfo).user.firstName + " " + ((TdApi.PrivateChatInfo)chatInfo).user.lastName;
        }
//        if (chatInfo instanceof TdApi.UnknownGroupChatInfo) {
//            return ApplicationLoader.applicationContext.getResources().getString(R.string.chat_list_unknown_group_chat);
//        }
//        if (chatInfo instanceof TdApi.UnknownPrivateChatInfo) {
//            return ApplicationLoader.applicationContext.getResources().getString(R.string.chat_list_unknown_chat);
//        }
        return "";
    }

    private boolean isChatGroup(TdApi.ChatInfo chatInfo) {
        if (chatInfo instanceof TdApi.GroupChatInfo) {
            return true;
        }
//        if (chatInfo instanceof TdApi.UnknownGroupChatInfo) {
//            return true;
//        }
        return false;
    }

}
