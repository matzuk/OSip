package com.tg.osip.legacy.chatList;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.utils.time.TimeUtils;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class ChatListRecyclerAdapter extends RecyclerView.Adapter<ChatListRecyclerAdapter.ViewHolder>{

    private static final int TEMP_SEND_STATE_IS_SENDING = 1000000000;
    private static final int TEMP_SEND_STATE_IS_ERROR = 0;
    private ChatHashMap chatHashMap = new ChatHashMap();

    private boolean firstInit = true;

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
    public ChatListRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public long getItemId(int position) {
        return chatHashMap.getChatFromPosition(position).id;
    }

    @Override
    public void onBindViewHolder(ChatListRecyclerAdapter.ViewHolder holder, int position) {
        TdApi.Chat concreteChat = chatHashMap.getChatFromPosition(position);
        if (concreteChat == null) {
            return;
        }
        // set unread count
        if (concreteChat.unreadCount > 0) {
            holder.chatUnreadMessageCount.setVisibility(View.VISIBLE);
            holder.chatUnreadMessageCount.setText(String.valueOf(concreteChat.unreadCount));
        } else {
            holder.chatUnreadMessageCount.setVisibility(View.GONE);
        }
        // set error in messaged sending
        if (concreteChat.topMessage.date == TEMP_SEND_STATE_IS_ERROR) {
            holder.chatUnreadMessageCount.setBackgroundResource(R.drawable.ic_error);
            holder.chatUnreadMessageCount.setText("");
        }
        // get last message and set date
        TdApi.Message message = concreteChat.topMessage;
        if (message != null) {
            // set is last message from account
            String textYou = "";
//            if (TGProxy.getInstance().getAccountId() == concreteChat.topMessage.fromId) {
//                textYou = ApplicationLoader.applicationContext.getResources().getString(R.string.chat_list_message_text_you);
//            }
            String dataString = TimeUtils.stringForMessageListDate(message.date);
            holder.chatMessageSendingTime.setText(dataString);
            // get message content and set last message
            holder.chatUserLastMessage.setText(textYou + " " + getChatLastMessage(message));
        }
        // get ChatInfo
        TdApi.ChatInfo chatInfo = concreteChat.type;
        if (chatInfo != null) {
            //  Set name
            holder.chatUserName.setText(getName(chatInfo));
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
        holder.chatGroupIcon.setVisibility(isChatGroup(concreteChat.type)? View.VISIBLE : View.GONE);
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

    private void setSendStateMessage(TdApi.Chat chat, ImageView imageView) {
        if (chat.topMessage.id >= TEMP_SEND_STATE_IS_SENDING) {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_clock, ApplicationSIP.applicationContext.getTheme()));
        } else {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_not_read, ApplicationSIP.applicationContext.getTheme()));
        }
    }

    @Override
    public int getItemCount() {
        if (chatHashMap != null) {
            return chatHashMap.size();
        }
        return 0;
    }

    public void addFirstAllChat(TdApi.Chats chats) {
        chatHashMap.addAllChat(chats);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void addAllChat(TdApi.Chats chats) {
        chatHashMap.addAllChat(chats);
        updateInUI();
    }

//    private FirstChatLoadingListener firstChatLoadingListener;

    public void addChat(TdApi.Chat chat) {
        chatHashMap.addChat(chat);
        insertUpdateInUI();
    }

    public TdApi.Chat getChat(int position) {
        return chatHashMap.getChatFromPosition(position);
    }

    public boolean isChatInList(long chatId) {
        return chatHashMap.isChatInMap(chatId);
    }

    public void changeChat(TdApi.Chat chat) {
        chatHashMap.changeChat(chat);
        Log.d("pos1:", String.valueOf(chatHashMap.indexOf(chat)));
        updateInUI(chatHashMap.indexOf(chat));
    }

    private void updateInUI() {
        if (!firstInit) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    private void updateInUI(final int position) {
        if (!firstInit) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifyItemChanged(position);
            }
        });
    }

    private void insertUpdateInUI() {
        if (!firstInit) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifyItemInserted(0);
            }
        });
    }

    public void updatePhotoInUI(int fileId, int startPosition, int endPosition) {
        if (!firstInit) {
            return;
        }
        for (int i = startPosition; i < endPosition; i++) {
            TdApi.ChatInfo chatInfo = chatHashMap.getChatFromPosition(i).type;
//            int chatFileId = TDLibUtils.getFileId(chatInfo);
//            if (chatFileId == fileId) {
//                updateInUI(i);
//            }
        }
    }

    public void setFirstInit(boolean firstInit) {
        this.firstInit = firstInit;
    }

}
