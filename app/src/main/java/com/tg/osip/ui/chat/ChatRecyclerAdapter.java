package com.tg.osip.ui.chat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tg.osip.R;
import com.tg.osip.utils.log.Logger;
import com.tg.osip.utils.time.TimeUtils;
import com.tg.osip.utils.ui.auto_loading.AutoLoadingRecyclerViewAdapter;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class ChatRecyclerAdapter extends AutoLoadingRecyclerViewAdapter<TdApi.Message> {

    private static final int MAIN_VIEW = 0;
    private static final int UNSUPPORTED_VIEW = 4;

    static class MainViewHolder extends RecyclerView.ViewHolder {

        ImageView avatar;
        TextView chatMessageName;
        TextView chatMessageText;
        TextView chatMessageSendingTime;
        ImageView chatMessageUnreadOutbox;

        public MainViewHolder(View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            chatMessageName = (TextView) itemView.findViewById(R.id.chat_message_name);
            chatMessageText = (TextView) itemView.findViewById(R.id.chat_message_text);
            chatMessageSendingTime = (TextView) itemView.findViewById(R.id.chat_message_sending_time);
            chatMessageUnreadOutbox = (ImageView) itemView.findViewById(R.id.chat_message_unread_outbox);
        }
    }

    static class UnsupportedViewHolder extends RecyclerView.ViewHolder {

        ImageView avatar;
        TextView chatMessageName;
        TextView chatMessageText;
        TextView chatMessageSendingTime;

        public UnsupportedViewHolder(View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
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
        if (getItem(position).message instanceof TdApi.MessageText) {
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

//        TdApi.User user = userHashMap.get(message.fromId);
//        if (user == null) {
//            return;
//        }
//
//        TDLibUtils.setLetterDrawable(mainHolder.avatar, user);
//        if (PicassoProxy.getInstance().containInCache(user)) {
//            Picasso.with(
//                    ApplicationLoader.applicationContext).
//                    load(PicassoProxy.getInstance().getFilePath(user)).
//                    placeholder(mainHolder.avatar.getDrawable()).
//                    transform(new CirclePicassoTransformation()).
//                    into(mainHolder.avatar
//                    );
//        } else {
//            PicassoProxy.getInstance().downloadFile(user);
//        }

//        mainHolder.chatMessageName.setText(user.firstName + " " + user.lastName);

        String dataString = TimeUtils.stringForMessageListDate(message.date);
        mainHolder.chatMessageSendingTime.setText(dataString);

        // set unread outbox image
//        if (message.date == TEMP_SEND_STATE_IS_ERROR) {
//            mainHolder.chatMessageUnreadOutbox.setVisibility(View.VISIBLE);
//            mainHolder.chatMessageUnreadOutbox.setImageDrawable(ResourcesCompat.getDrawable(ApplicationLoader.applicationContext.getResources(), R.drawable.ic_message_error, ApplicationLoader.applicationContext.getTheme()));
//        } else if (TGProxy.getInstance().getUserId() == message.fromId) {
//            if (lastChatReadOutboxId >= message.id) {
//                mainHolder.chatMessageUnreadOutbox.setVisibility(View.GONE);
//            } else {
//                mainHolder.chatMessageUnreadOutbox.setVisibility(View.VISIBLE);
//                setSendStateMessage(message, mainHolder.chatMessageUnreadOutbox);
//            }
//        } else {
//            mainHolder.chatMessageUnreadOutbox.setVisibility(View.GONE);
//        }
    }

    private void onBindUnsupportedHolder(RecyclerView.ViewHolder holder, int position) {
        UnsupportedViewHolder mainHolder = (UnsupportedViewHolder) holder;
        TdApi.Message message = getItem(position);
        if (message == null) {
            return;
        }
        TdApi.MessageContent messageContent = message.message;
        if (messageContent == null) {
            return;
        }

//        TdApi.User user = userHashMap.get(message.fromId);
//        if (user == null) {
//            return;
//        }

//        TDLibUtils.setLetterDrawable(mainHolder.avatar, user);
//        if (PicassoProxy.getInstance().containInCache(user)) {
//            Picasso.with(
//                    ApplicationLoader.applicationContext).
//                    load(PicassoProxy.getInstance().getFilePath(user)).
//                    placeholder(mainHolder.avatar.getDrawable()).
//                    transform(new CirclePicassoTransformation()).
//                    into(mainHolder.avatar
//                    );
//        } else {
//            PicassoProxy.getInstance().downloadFile(user);
//        }
//
//        mainHolder.chatMessageName.setText(user.firstName + " " + user.lastName);

        String dataString = TimeUtils.stringForMessageListDate(message.date);
        mainHolder.chatMessageSendingTime.setText(dataString);

    }

}
