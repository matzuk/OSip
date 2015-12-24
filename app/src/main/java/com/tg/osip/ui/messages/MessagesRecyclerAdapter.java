package com.tg.osip.ui.messages;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.business.models.messages.MessageAdapterModel;
import com.tg.osip.business.models.messages.MessageItem;
import com.tg.osip.business.models.PhotoItem;
import com.tg.osip.business.models.UserItem;
import com.tg.osip.business.models.messages.contents.MessageContentActionsItem;
import com.tg.osip.business.models.messages.contents.MessageContentPhotoItem;
import com.tg.osip.business.models.messages.contents.MessageContentTextItem;
import com.tg.osip.ui.general.views.images.PhotoView;
import com.tg.osip.utils.time.TimeUtils;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author e.matsyuk
 */
public class MessagesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TEMP_SEND_STATE_IS_SENDING = 1000000000;
    private static final int TEMP_SEND_STATE_IS_ERROR = 0;
    private static final int EMPTY_LIST = 0;

    private static final int TEXT_VIEW = 0;
    private static final int PHOTO_VIEW = 1;
    private static final int ACTION_VIEW = 3;
    private static final int UNSUPPORTED_VIEW = 4;

    private int myUserId;
    private int lastChatReadOutboxId;
    private List<MessageItem> listElements = new ArrayList<>();
    private Map<Integer, UserItem> usersMap = new HashMap<>();
    private WeakReference<OnMessageClickListener> onMessageClickListenerWeakReference;
    // after reorientation test this member
    // or one extra request will be sent after each reorientation
    private boolean allItemsLoaded;

    static class TextViewHolder extends RecyclerView.ViewHolder {

        PhotoView avatar;
        TextView messageName;
        TextView messageText;
        TextView messageSendingTime;
        ImageView messageUnreadOutbox;

        public TextViewHolder(View itemView) {
            super(itemView);
            avatar = (PhotoView) itemView.findViewById(R.id.avatar);
            messageName = (TextView) itemView.findViewById(R.id.message_name);
            messageText = (TextView) itemView.findViewById(R.id.message_text);
            messageSendingTime = (TextView) itemView.findViewById(R.id.message_sending_time);
            messageUnreadOutbox = (ImageView) itemView.findViewById(R.id.message_unread_outbox);
        }
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout photoLayout;
        PhotoView avatar;
        TextView messageName;
        PhotoView photo;
        TextView messageSendingTime;
        ImageView messageUnreadOutbox;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            photoLayout = (RelativeLayout) itemView.findViewById(R.id.photo_layout);
            avatar = (PhotoView) itemView.findViewById(R.id.avatar);
            messageName = (TextView) itemView.findViewById(R.id.message_name);
            photo = (PhotoView) itemView.findViewById(R.id.photo);
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

        PhotoView avatar;
        TextView messageName;
        TextView messageText;
        TextView messageSendingTime;
        ImageView messageUnreadOutbox;

        public UnsupportedViewHolder(View itemView) {
            super(itemView);
            avatar = (PhotoView) itemView.findViewById(R.id.avatar);
            messageName = (TextView) itemView.findViewById(R.id.message_name);
            messageText = (TextView) itemView.findViewById(R.id.message_text);
            messageSendingTime = (TextView) itemView.findViewById(R.id.message_sending_time);
            messageUnreadOutbox = (ImageView) itemView.findViewById(R.id.message_unread_outbox);
        }
    }

    public void setOnMessageClickListener(OnMessageClickListener onMessageClickListener) {
        this.onMessageClickListenerWeakReference = new WeakReference<>(onMessageClickListener);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    public MessageItem getItem(int position) {
        return listElements.get(position);
    }

    @Override
    public int getItemCount() {
        return listElements.size();
    }

    public void addMessageAdapterModel(MessageAdapterModel messageAdapterModel) {
        if (messageAdapterModel == null) {
            allItemsLoaded = true;
            return;
        }
        List<MessageItem> messageItemList = messageAdapterModel.getMessageItemList();
        if (messageItemList == null || messageItemList.size() == EMPTY_LIST) {
            allItemsLoaded = true;
            return;
        }
        listElements.addAll(messageItemList);
        Map<Integer, UserItem> integerUserItemMap = messageAdapterModel.getIntegerUserItemMap();
        if (integerUserItemMap != null) {
            usersMap.putAll(integerUserItemMap);
        }
        notifyItemRangeInserted(getItemCount() - messageItemList.size(), messageItemList.size());
    }

    public boolean isAllItemsLoaded() {
        return allItemsLoaded;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TEXT_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list_text, parent, false);
            return new TextViewHolder(v);
        } else if (viewType == PHOTO_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list_photo, parent, false);
            return new PhotoViewHolder(v);
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
        MessageItem.ContentType contentType = getItem(position).getContentType();
        if (contentType == MessageItem.ContentType.TEXT_MESSAGE_TYPE) {
            return TEXT_VIEW;
        } else if (contentType == MessageItem.ContentType.PHOTO_MESSAGE_TYPE) {
            return PHOTO_VIEW;
        } else if (contentType == MessageItem.ContentType.ACTION_TYPE) {
            return ACTION_VIEW;
        }
        return UNSUPPORTED_VIEW;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TEXT_VIEW:
                onBindTextHolder(holder, position);
                break;
            case PHOTO_VIEW:
                onBindPhotoHolder(holder, position);
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
        TextViewHolder textHolder = (TextViewHolder) holder;
        MessageItem message = getItem(position);
        MessageContentTextItem messageContentTextItem = (MessageContentTextItem)message.getMessageContentItem();
        if (messageContentTextItem == null) {
            return;
        }
        textHolder.messageText.setText(messageContentTextItem.getText());
        // get user
        UserItem user = usersMap.get(message.getFromId());
        if (user != null) {
            // set name
            String name = user.getName();
            textHolder.messageName.setText(name);
            // Set avatar
            textHolder.avatar.setCircleRounds(true);
            textHolder.avatar.setImageLoaderI(user);
        }
        // set data
        String dataString = TimeUtils.stringForMessageListDate(message.getDate());
        textHolder.messageSendingTime.setText(dataString);

        // set unread outbox image
        if (message.getDate() == TEMP_SEND_STATE_IS_ERROR) {
            textHolder.messageUnreadOutbox.setVisibility(View.VISIBLE);
            textHolder.messageUnreadOutbox.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_message_error, ApplicationSIP.applicationContext.getTheme()));
        } else if (myUserId == message.getFromId()) {
            if (lastChatReadOutboxId >= message.getId()) {
                textHolder.messageUnreadOutbox.setVisibility(View.GONE);
            } else {
                textHolder.messageUnreadOutbox.setVisibility(View.VISIBLE);
                setSendStateMessage(message, textHolder.messageUnreadOutbox);
            }
        } else {
            textHolder.messageUnreadOutbox.setVisibility(View.GONE);
        }
    }

//    private boolean isItemsEqual(int position) {
//        if (position == getItemCount() - 1) {
//            return false;
//        }
//        int nextPosition = position + 1;
//        if (getItemViewType(position) != getItemViewType(nextPosition)) {
//            return false;
//        }
//        String dataString = TimeUtils.stringForMessageListDate(getItem(position).getMessage().date);
//        String dataPreviousString = TimeUtils.stringForMessageListDate(getItem(nextPosition).getMessage().date);
//        if (!dataPreviousString.equals(dataString)) {
//            return false;
//        }
//        return getItem(position).getMessage().fromId == getItem(nextPosition).getMessage().fromId;
//    }

    private void onBindPhotoHolder(RecyclerView.ViewHolder holder, int position) {
        PhotoViewHolder photoViewHolder = (PhotoViewHolder) holder;
        MessageItem message = getItem(position);
        MessageContentPhotoItem messageContentPhotoItem = (MessageContentPhotoItem)message.getMessageContentItem();
        if (messageContentPhotoItem == null) {
            return;
        }

        PhotoItem photoItem = messageContentPhotoItem.getPhotoItemMedium();
        if (photoItem != null) {
            photoViewHolder.photo.setImageLoaderI(photoItem);
        }

        UserItem user = usersMap.get(message.getFromId());
        if (user != null) {
            // set name
            String name = user.getName();
            photoViewHolder.messageName.setText(name);
            // Set avatar
            photoViewHolder.avatar.setCircleRounds(true);
            photoViewHolder.avatar.setImageLoaderI(user);
        }

        String dataString = TimeUtils.stringForMessageListDate(message.getDate());
        photoViewHolder.messageSendingTime.setText(dataString);

        // set unread outbox image
        if (message.getDate() == TEMP_SEND_STATE_IS_ERROR) {
            photoViewHolder.messageUnreadOutbox.setVisibility(View.VISIBLE);
            photoViewHolder.messageUnreadOutbox.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_message_error, ApplicationSIP.applicationContext.getTheme()));
        } else if (myUserId == message.getFromId()) {
            if (lastChatReadOutboxId >= message.getId()) {
                photoViewHolder.messageUnreadOutbox.setVisibility(View.GONE);
            } else {
                photoViewHolder.messageUnreadOutbox.setVisibility(View.VISIBLE);
                setSendStateMessage(message, photoViewHolder.messageUnreadOutbox);
            }
        } else {
            photoViewHolder.messageUnreadOutbox.setVisibility(View.GONE);
        }
        // set photo OnClickListener
        photoViewHolder.photoLayout.setOnClickListener(v -> {
            if (onMessageClickListenerWeakReference != null && onMessageClickListenerWeakReference.get() != null && messageContentPhotoItem.getPhotoItemLarge() != null) {
                OnMessageClickListener onMessageClickListener = onMessageClickListenerWeakReference.get();
                Pair<Integer, List<PhotoItem>> clickedPair = getPhotoLargeItemsWithClickedPos(messageContentPhotoItem.getPhotoItemLarge().getPhotoFileId());
                onMessageClickListener.onPhotoMessageClick(clickedPair.first, clickedPair.second);
            }
        });
    }

    // temp method for PhotoItems getting
    private Pair<Integer, List<PhotoItem>> getPhotoLargeItemsWithClickedPos(int photoFileId) {
        int currentPosInPhotoList = 0;
        int clickedPosInPhotoList = currentPosInPhotoList;
        List<PhotoItem> photoLargeItemList = new ArrayList<>();
        for (MessageItem messageItem : listElements) {
            if (messageItem.getContentType() == MessageItem.ContentType.PHOTO_MESSAGE_TYPE) {
                MessageContentPhotoItem messageContentPhotoItem = ((MessageContentPhotoItem)messageItem.getMessageContentItem());
                if (messageContentPhotoItem == null) {
                    continue;
                }
                PhotoItem photoItemLarge = messageContentPhotoItem.getPhotoItemLarge();
                if (photoItemLarge == null) {
                    continue;
                }
                photoLargeItemList.add(photoItemLarge);
                if (photoFileId == photoItemLarge.getPhotoFileId()) {
                    clickedPosInPhotoList = currentPosInPhotoList;
                }
                currentPosInPhotoList++;
            }
        }
        return new Pair<>(clickedPosInPhotoList, photoLargeItemList);
    }

    private void setSendStateMessage(MessageItem message, ImageView imageView) {
        if (message.getId() >= TEMP_SEND_STATE_IS_SENDING) {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_clock, ApplicationSIP.applicationContext.getTheme()));
        } else {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_not_read, ApplicationSIP.applicationContext.getTheme()));
        }
    }

    private void onBindActionHolder(RecyclerView.ViewHolder holder, int position) {
        ActionViewHolder actionHolder = (ActionViewHolder) holder;
        MessageItem message = getItem(position);
        MessageContentActionsItem messageContentActionsItem = (MessageContentActionsItem)message.getMessageContentItem();
        if (messageContentActionsItem == null) {
            return;
        }

        String actionText = getActionText(messageContentActionsItem.getMessageContent(), usersMap.get(message.getFromId()));
        if (actionText != null) {
            actionHolder.messageText.setText(actionText);
        }
        if (messageContentActionsItem.getMessageContent().getClass() == TdApi.MessageChatChangePhoto.class) {
            TdApi.Photo photo = ((TdApi.MessageChatChangePhoto)messageContentActionsItem.getMessageContent()).photo;
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
        MessageItem message = getItem(position);
        if (message == null) {
            return;
        }

        UserItem user = usersMap.get(message.getFromId());
        // set name
        String name = user.getName();
        unsupportedHolder.messageName.setText(name);
        // Set avatar
        unsupportedHolder.avatar.setCircleRounds(true);
        unsupportedHolder.avatar.setImageLoaderI(user);

        String dataString = TimeUtils.stringForMessageListDate(message.getDate());
        unsupportedHolder.messageSendingTime.setText(dataString);

        // set unread outbox image
        if (message.getDate() == TEMP_SEND_STATE_IS_ERROR) {
            unsupportedHolder.messageUnreadOutbox.setVisibility(View.VISIBLE);
            unsupportedHolder.messageUnreadOutbox.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_message_error, ApplicationSIP.applicationContext.getTheme()));
        } else if (myUserId == message.getFromId()) {
            if (lastChatReadOutboxId >= message.getId()) {
                unsupportedHolder.messageUnreadOutbox.setVisibility(View.GONE);
            } else {
                unsupportedHolder.messageUnreadOutbox.setVisibility(View.VISIBLE);
                setSendStateMessage(message, unsupportedHolder.messageUnreadOutbox);
            }
        } else {
            unsupportedHolder.messageUnreadOutbox.setVisibility(View.GONE);
        }
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
