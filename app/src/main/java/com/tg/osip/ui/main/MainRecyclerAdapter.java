package com.tg.osip.ui.main;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.tdclient.models.MainListItem;
import com.tg.osip.ui.views.auto_loading.AutoLoadingRecyclerViewAdapter;
import com.tg.osip.utils.log.Logger;

import org.drinkless.td.libcore.telegram.TdApi;

import rx.functions.Action1;

/**
 * @author e.matsyuk
 */
public class MainRecyclerAdapter extends AutoLoadingRecyclerViewAdapter<MainListItem>  {

    private static final int TEMP_SEND_STATE_IS_ERROR = 0;
    private static final int TEMP_SEND_STATE_IS_SENDING = 1000000000;

    private int userId;

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
        return getItem(position).getApiChat().id;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder mainHolder = (ViewHolder) holder;
        TdApi.Chat concreteChat = getItem(position).getApiChat();
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
                textYou = ApplicationSIP.applicationContext.getResources().getString(R.string.chat_list_message_text_you);
            }

            // very heavy operation for list
            String dataString = getItem(position).getLastMessageDate();
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



//            if (chatInfo instanceof TdApi.PrivateChatInfo) {
//                TdApi.PrivateChatInfo pr = ((TdApi.PrivateChatInfo) chatInfo);
//                TGProxy.getInstance().sendTD(new TdApi.DownloadFile(1), TdApi.UpdateFile.class)
//                        .subscribe(new Action1<TdApi.UpdateFile>() {
//                            @Override
//                            public void call(TdApi.UpdateFile updateFile) {
//                                Logger.debug(updateFile);
//                            }
//                        });
//            }

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
        return "";
    }

    private boolean isChatGroup(TdApi.ChatInfo chatInfo) {
        if (chatInfo instanceof TdApi.GroupChatInfo) {
            return true;
        }
        return false;
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
