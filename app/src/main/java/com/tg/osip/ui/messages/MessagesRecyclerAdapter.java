package com.tg.osip.ui.messages;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.business.models.UserItem;
import com.tg.osip.ui.general.views.auto_loading.AutoLoadingRecyclerViewAdapter;
import com.tg.osip.ui.general.views.images.SIPAvatar;
import com.tg.osip.utils.time.TimeUtils;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.HashMap;
import java.util.Map;

/**
 * @author e.matsyuk
 */
public class MessagesRecyclerAdapter extends AutoLoadingRecyclerViewAdapter<TdApi.Message> {

    private static final int TEMP_SEND_STATE_IS_SENDING = 1000000000;
    private static final int TEMP_SEND_STATE_IS_ERROR = 0;

    private static final int MAIN_VIEW = 0;
    private static final int ACTION_VIEW = 3;
    private static final int UNSUPPORTED_VIEW = 4;

    private int myUserId;
    private int lastChatReadOutboxId;
    private Map<Integer, UserItem> usersMap = new HashMap<>();

    static class MainViewHolder extends RecyclerView.ViewHolder {

        SIPAvatar avatar;
        TextView messageName;
        TextView messageText;
        TextView messageSendingTime;
        ImageView messageUnreadOutbox;

        public MainViewHolder(View itemView) {
            super(itemView);
            avatar = (SIPAvatar) itemView.findViewById(R.id.avatar);
            messageName = (TextView) itemView.findViewById(R.id.message_name);
            messageText = (TextView) itemView.findViewById(R.id.message_text);
            messageSendingTime = (TextView) itemView.findViewById(R.id.message_sending_time);
            messageUnreadOutbox = (ImageView) itemView.findViewById(R.id.message_unread_outbox);
        }
    }

    static class ActionViewHolder extends RecyclerView.ViewHolder {

        TextView messageText;
        ImageView photo;

        public ActionViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.message_text);
            photo = (ImageView) itemView.findViewById(R.id.photo);
        }
    }

    static class UnsupportedViewHolder extends RecyclerView.ViewHolder {

        SIPAvatar avatar;
        TextView messageName;
        TextView messageText;
        TextView messageSendingTime;

        public UnsupportedViewHolder(View itemView) {
            super(itemView);
            avatar = (SIPAvatar) itemView.findViewById(R.id.avatar);
            messageName = (TextView) itemView.findViewById(R.id.message_name);
            messageText = (TextView) itemView.findViewById(R.id.message_text);
            messageSendingTime = (TextView) itemView.findViewById(R.id.message_sending_time);
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MAIN_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list_text, parent, false);
            return new MainViewHolder(v);
        } else if (viewType == ACTION_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list_actions, parent, false);
            return new ActionViewHolder(v);
        }
        else if (viewType == UNSUPPORTED_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list_unsupport, parent, false);
            return new UnsupportedViewHolder(v);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).message.getClass() == TdApi.MessageText.class) {
            return MAIN_VIEW;
        } else if (getItem(position).message.getClass() == TdApi.MessageChatAddParticipant.class ||
                getItem(position).message.getClass() == TdApi.MessageChatChangePhoto.class ||
                getItem(position).message.getClass() == TdApi.MessageChatChangeTitle.class ||
                getItem(position).message.getClass() == TdApi.MessageChatDeleteParticipant.class ||
                getItem(position).message.getClass() == TdApi.MessageChatDeletePhoto.class) {
            return ACTION_VIEW;
        }
        return UNSUPPORTED_VIEW;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case MAIN_VIEW:
                onBindTextHolder(holder, position);
                break;
            case ACTION_VIEW:
                onBindActionHolder(holder, position);
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
            mainHolder.messageText.setText(messageText.text);
        }

        UserItem user = usersMap.get(message.fromId);
        // set name
        String name = user.getName();
        mainHolder.messageName.setText(name);
        // Set avatar
        mainHolder.avatar.setImageLoaderI(user);

        String dataString = TimeUtils.stringForMessageListDate(message.date);
        mainHolder.messageSendingTime.setText(dataString);

        // set unread outbox image
        if (message.date == TEMP_SEND_STATE_IS_ERROR) {
            mainHolder.messageUnreadOutbox.setVisibility(View.VISIBLE);
            mainHolder.messageUnreadOutbox.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_message_error, ApplicationSIP.applicationContext.getTheme()));
        } else if (myUserId == message.fromId) {
            if (lastChatReadOutboxId >= message.id) {
                mainHolder.messageUnreadOutbox.setVisibility(View.GONE);
            } else {
                mainHolder.messageUnreadOutbox.setVisibility(View.VISIBLE);
                setSendStateMessage(message, mainHolder.messageUnreadOutbox);
            }
        } else {
            mainHolder.messageUnreadOutbox.setVisibility(View.GONE);
        }
    }

    private void setSendStateMessage(TdApi.Message message, ImageView imageView) {
        if (message.id >= TEMP_SEND_STATE_IS_SENDING) {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_clock, ApplicationSIP.applicationContext.getTheme()));
        } else {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_not_read, ApplicationSIP.applicationContext.getTheme()));
        }
    }

    private void onBindActionHolder(RecyclerView.ViewHolder holder, int position) {
        ActionViewHolder actionHolder = (ActionViewHolder) holder;
        TdApi.Message message = getItem(position);
        if (message == null) {
            return;
        }
        TdApi.MessageContent messageContent = message.message;
        if (messageContent == null) {
            return;
        }
        String actionText = getActionText(messageContent, usersMap.get(message.fromId));
        if (actionText != null) {
            actionHolder.messageText.setText(actionText);
        }
        if (messageContent.getClass() == TdApi.MessageChatChangePhoto.class) {
            TdApi.Photo photo = ((TdApi.MessageChatChangePhoto)messageContent).photo;
            actionHolder.photo.setVisibility(View.VISIBLE);
            // TODO add later
            //setPhoto(actionHolder.photo, photo, GROUP_SMALL_PHOTO, true);
        } else {
            actionHolder.photo.setImageDrawable(null);
            actionHolder.photo.setVisibility(View.GONE);
        }
    }

    // TODO add name selecting
    private String getActionText(TdApi.MessageContent messageContent, UserItem userItem) {
        String text;
        TdApi.User userFrom = userItem.getUser();
        if (messageContent.getClass() == TdApi.MessageChatAddParticipant.class) {
            text = userItem.getName();
            text = text + " " + ApplicationSIP.applicationContext.getString(R.string.invited);
            text = text + " " +((TdApi.MessageChatAddParticipant)messageContent).user.firstName + " " + ((TdApi.MessageChatAddParticipant)messageContent).user.lastName;
            return text;
        } else if (messageContent.getClass() == TdApi.MessageChatChangePhoto.class) {
            text = userItem.getName();
            text = text + " " + ApplicationSIP.applicationContext.getString(R.string.changed_group_photo);
            return text;
        } else if (messageContent.getClass() == TdApi.MessageChatChangeTitle.class) {
            text = userItem.getName();
            text = text + " " + ApplicationSIP.applicationContext.getString(R.string.changed_group_name);
            text = text + " " +((TdApi.MessageChatChangeTitle)messageContent).title;
            return text;
        } else if (messageContent.getClass() == TdApi.MessageChatDeleteParticipant.class) {
            text = ((TdApi.MessageChatDeleteParticipant)messageContent).user.firstName + " " + ((TdApi.MessageChatDeleteParticipant)messageContent).user.lastName;
            if (userFrom != null) {
                text = userItem.getName() + ApplicationSIP.applicationContext.getString(R.string.removed) + " " + text;
            } else {
                text = text + " " + ApplicationSIP.applicationContext.getString(R.string.left_group);
            }
            return text;
        } else if (messageContent instanceof TdApi.MessageChatDeletePhoto) {
            text = userItem.getName();
            text = text + " " + ApplicationSIP.applicationContext.getString(R.string.removed_group_photo);
            return text;
        }
        return "";
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

        UserItem user = usersMap.get(message.fromId);
        // set name
        String name = user.getName();
        unsupportedHolder.messageName.setText(name);
        // Set avatar
        unsupportedHolder.avatar.setImageLoaderI(user);

        String dataString = TimeUtils.stringForMessageListDate(message.date);
        unsupportedHolder.messageSendingTime.setText(dataString);

    }

    public void setMyUserId(int myUserId) {
        this.myUserId = myUserId;
    }

    public void setLastChatReadOutboxId(int lastChatReadOutboxId) {
        this.lastChatReadOutboxId = lastChatReadOutboxId;
    }

    public void setChatUsers(Map<Integer, UserItem> users) {
        usersMap.putAll(users);
    }

}
