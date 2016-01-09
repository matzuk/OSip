package com.tg.osip.ui.messages;

import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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
import com.tg.osip.business.models.messages.contents.AudioItem;
import com.tg.osip.business.models.messages.contents.ChatAddParticipantItem;
import com.tg.osip.business.models.messages.contents.ChatChangePhotoItem;
import com.tg.osip.business.models.messages.contents.ChatChangeTitleItem;
import com.tg.osip.business.models.messages.contents.ChatDeleteParticipantItem;
import com.tg.osip.business.models.messages.contents.GroupChatCreate;
import com.tg.osip.business.models.messages.contents.MessageContentPhotoItem;
import com.tg.osip.business.models.messages.contents.MessageContentTextItem;
import com.tg.osip.ui.general.views.ProgressTextView;
import com.tg.osip.ui.general.views.progress_download.ProgressDownloadView;
import com.tg.osip.ui.general.views.images.PhotoView;
import com.tg.osip.utils.time.TimeUtils;

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
    private static final int CHAT_ADD_PARTICIPANT_VIEW = 2;
    private static final int CHAT_CHANGE_PHOTO_VIEW = 3;
    private static final int CHAT_CHANGE_TITLE_VIEW = 4;
    private static final int CHAT_DELETE_PARTICIPANT_VIEW = 5;
    private static final int CHAT_DELETE_PHOTO_VIEW = 6;
    // FIXME how work TdApi.MessageChatJoinByLink
//    private static final int CHAT_JOIN_BY_LINK_VIEW = 7;
    private static final int GROUP_CHAT_CREATE_VIEW = 8;
    private static final int AUDIO_VIEW = 9;
    private static final int UNSUPPORTED_VIEW = 10;

    private int myUserId;
    private int lastChatReadOutboxId;
    private List<MessageItem> listElements = new ArrayList<>();
    private Map<Integer, UserItem> usersMap = new HashMap<>();
    private WeakReference<OnMessageClickListener> onMessageClickListenerWeakReference;

    public MessagesRecyclerAdapter(int myUserId) {
        this.myUserId = myUserId;
    }

    ///// ViewHolders /////
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

    static class ChatChangesViewHolder extends RecyclerView.ViewHolder {

        TextView messageText;

        public ChatChangesViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.message_text);
        }
    }

    static class ChatChangesPhotoViewHolder extends RecyclerView.ViewHolder {

        TextView messageText;
        PhotoView photo;

        public ChatChangesPhotoViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.message_text);
            photo = (PhotoView) itemView.findViewById(R.id.photo);
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

    static class AudioViewHolder extends RecyclerView.ViewHolder {

        PhotoView avatar;
        TextView messageName;
        TextView messageSendingTime;
        ImageView messageUnreadOutbox;
        ProgressDownloadView progressDownloadView;
        TextView messageAudioTitle;
        ProgressTextView messageAudioPerformer;

        public AudioViewHolder(View itemView) {
            super(itemView);
            avatar = (PhotoView) itemView.findViewById(R.id.avatar);
            messageName = (TextView) itemView.findViewById(R.id.message_name);
            messageSendingTime = (TextView) itemView.findViewById(R.id.message_sending_time);
            messageUnreadOutbox = (ImageView) itemView.findViewById(R.id.message_unread_outbox);
            progressDownloadView = (ProgressDownloadView) itemView.findViewById(R.id.progressDownloadView);
            messageAudioTitle = (TextView) itemView.findViewById(R.id.message_audio_title);
            messageAudioPerformer = (ProgressTextView) itemView.findViewById(R.id.message_audio_performer);
        }
    }

    ///// overrides methods /////
    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public int getItemCount() {
        return listElements.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TEXT_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list_text, parent, false);
            return new TextViewHolder(v);
        } else if (viewType == PHOTO_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list_photo, parent, false);
            return new PhotoViewHolder(v);
        } else if (viewType == CHAT_ADD_PARTICIPANT_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list_chat_changes, parent, false);
            return new ChatChangesViewHolder(v);
        } else if (viewType == CHAT_CHANGE_PHOTO_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list_chat_changes_photo, parent, false);
            return new ChatChangesPhotoViewHolder(v);
        } else if (viewType == CHAT_CHANGE_TITLE_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list_chat_changes, parent, false);
            return new ChatChangesViewHolder(v);
        } else if (viewType == CHAT_DELETE_PARTICIPANT_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list_chat_changes, parent, false);
            return new ChatChangesViewHolder(v);
        } else if (viewType == CHAT_DELETE_PHOTO_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list_chat_changes, parent, false);
            return new ChatChangesViewHolder(v);
//        } else if (viewType == CHAT_JOIN_BY_LINK_VIEW) {
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list_chat_changes, parent, false);
//            return new ChatChangesViewHolder(v);
        } else if (viewType == GROUP_CHAT_CREATE_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list_chat_changes, parent, false);
            return new ChatChangesViewHolder(v);
        } else if (viewType == AUDIO_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list_audio, parent, false);
            return new AudioViewHolder(v);
        } else if (viewType == UNSUPPORTED_VIEW) {
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
        } else if (contentType == MessageItem.ContentType.CHAT_ADD_PARTICIPANT) {
            return CHAT_ADD_PARTICIPANT_VIEW;
        } else if (contentType == MessageItem.ContentType.CHAT_CHANGE_PHOTO) {
            return CHAT_CHANGE_PHOTO_VIEW;
        } else if (contentType == MessageItem.ContentType.CHAT_CHANGE_TITLE) {
            return CHAT_CHANGE_TITLE_VIEW;
        } else if (contentType == MessageItem.ContentType.CHAT_DELETE_PARTICIPANT) {
            return CHAT_DELETE_PARTICIPANT_VIEW;
        } else if (contentType == MessageItem.ContentType.CHAT_DELETE_PHOTO) {
            return CHAT_DELETE_PHOTO_VIEW;
//        } else if (contentType == MessageItem.ContentType.CHAT_JOIN_BY_LINK) {
//            return CHAT_JOIN_BY_LINK_VIEW;
        } else if (contentType == MessageItem.ContentType.GROUP_CHAT_CREATE) {
            return GROUP_CHAT_CREATE_VIEW;
        } else if (contentType == MessageItem.ContentType.AUDIO) {
            return AUDIO_VIEW;
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
            case CHAT_ADD_PARTICIPANT_VIEW:
                onBindChatAddParticipantHolder(holder, position);
                break;
            case CHAT_CHANGE_PHOTO_VIEW:
                onBindChatChangePhotoHolder(holder, position);
                break;
            case CHAT_CHANGE_TITLE_VIEW:
                onBindChatChangeTitleHolder(holder, position);
                break;
            case CHAT_DELETE_PARTICIPANT_VIEW:
                onBindChatDeleteParticipantHolder(holder, position);
                break;
            case CHAT_DELETE_PHOTO_VIEW:
                onBindChatDeletePhotoHolder(holder, position);
                break;
            case GROUP_CHAT_CREATE_VIEW:
                onBindGroupChatCreateHolder(holder, position);
                break;
//            case CHAT_JOIN_BY_LINK_VIEW:
//                onBindChatJoinByLinkHolder(holder, position);
//                break;
            case AUDIO_VIEW:
                onBindAudioHolder(holder, position);
                break;
            case UNSUPPORTED_VIEW:
                onBindUnsupportedHolder(holder, position);
                break;
        }
    }

    ///// Other methods /////
    public void setOnMessageClickListener(OnMessageClickListener onMessageClickListener) {
        this.onMessageClickListenerWeakReference = new WeakReference<>(onMessageClickListener);
    }

    public MessageItem getItem(int position) {
        return listElements.get(position);
    }

    public void addMessageAdapterModel(MessageAdapterModel messageAdapterModel) {
        if (messageAdapterModel == null) {
            return;
        }
        List<MessageItem> messageItemList = messageAdapterModel.getMessageItemList();
        if (messageItemList == null || messageItemList.size() == EMPTY_LIST) {
            return;
        }
        listElements.addAll(messageItemList);
        Map<Integer, UserItem> integerUserItemMap = messageAdapterModel.getIntegerUserItemMap();
        if (integerUserItemMap != null) {
            usersMap.putAll(integerUserItemMap);
        }
        notifyItemRangeInserted(getItemCount() - messageItemList.size(), messageItemList.size());
    }

    public void setLastChatReadOutboxId(int lastChatReadOutboxId) {
        this.lastChatReadOutboxId = lastChatReadOutboxId;
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

    ///// onBindViewHolders /////
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
        setUnreadOutboxImages(message, textHolder.messageUnreadOutbox);
    }

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
        setUnreadOutboxImages(message, photoViewHolder.messageUnreadOutbox);
        // set photo OnClickListener
        photoViewHolder.photoLayout.setOnClickListener(v -> {
            if (onMessageClickListenerWeakReference != null && onMessageClickListenerWeakReference.get() != null && messageContentPhotoItem.getPhotoItemLarge() != null) {
                OnMessageClickListener onMessageClickListener = onMessageClickListenerWeakReference.get();
                Pair<Integer, List<PhotoItem>> clickedPair = getPhotoLargeItemsWithClickedPos(messageContentPhotoItem.getPhotoItemLarge().getFileId());
                onMessageClickListener.onPhotoMessageClick(clickedPair.first, clickedPair.second);
            }
        });
    }

    private void onBindChatAddParticipantHolder(RecyclerView.ViewHolder holder, int position) {
        ChatChangesViewHolder actionHolder = (ChatChangesViewHolder) holder;
        MessageItem message = getItem(position);
        ChatAddParticipantItem chatAddParticipantItem = (ChatAddParticipantItem)message.getMessageContentItem();
        if (chatAddParticipantItem == null) {
            return;
        }
        String fromUserName = getUserName(message.getFromId());
        SpannableString text = new SpannableString(ApplicationSIP.applicationContext.getResources().getString(R.string.added, fromUserName, chatAddParticipantItem.getName()));
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ApplicationSIP.applicationContext, R.color.color_primary_dark)), 0, fromUserName.length(), 0);
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ApplicationSIP.applicationContext, R.color.color_primary_dark)), text.length() - chatAddParticipantItem.getName().length(), text.length(), 0);

        actionHolder.messageText.setText(text);
    }

    private void onBindChatChangePhotoHolder(RecyclerView.ViewHolder holder, int position) {
        ChatChangesPhotoViewHolder actionHolder = (ChatChangesPhotoViewHolder) holder;
        MessageItem message = getItem(position);
        ChatChangePhotoItem chatChangePhotoItem = (ChatChangePhotoItem)message.getMessageContentItem();
        if (chatChangePhotoItem == null) {
            return;
        }
        String fromUserName = getUserName(message.getFromId());
        SpannableString text = new SpannableString(ApplicationSIP.applicationContext.getResources().getString(R.string.changed_group_photo, fromUserName));
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ApplicationSIP.applicationContext, R.color.color_primary_dark)), 0, fromUserName.length(), 0);

        if (chatChangePhotoItem.getPhotoItemMedium() != null) {
            actionHolder.photo.setVisibility(View.VISIBLE);
            actionHolder.photo.setCircleRounds(true);
            actionHolder.photo.setImageLoaderI(chatChangePhotoItem.getPhotoItemMedium());
        } else {
            actionHolder.photo.setVisibility(View.GONE);
        }
        actionHolder.messageText.setText(text);
    }

    private void onBindChatChangeTitleHolder(RecyclerView.ViewHolder holder, int position) {
        ChatChangesViewHolder actionHolder = (ChatChangesViewHolder) holder;
        MessageItem message = getItem(position);
        ChatChangeTitleItem chatChangeTitleItem = (ChatChangeTitleItem)message.getMessageContentItem();
        if (chatChangeTitleItem == null) {
            return;
        }
        String fromUserName = getUserName(message.getFromId());
        SpannableString text = new SpannableString(ApplicationSIP.applicationContext.getResources().getString(R.string.changed_group_title, fromUserName, chatChangeTitleItem.getTitle()));
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ApplicationSIP.applicationContext, R.color.color_primary_dark)), 0, fromUserName.length(), 0);

        actionHolder.messageText.setText(text);
    }

    private void onBindChatDeleteParticipantHolder(RecyclerView.ViewHolder holder, int position) {
        ChatChangesViewHolder actionHolder = (ChatChangesViewHolder) holder;
        MessageItem message = getItem(position);
        ChatDeleteParticipantItem chatDeleteParticipantItem = (ChatDeleteParticipantItem)message.getMessageContentItem();
        if (chatDeleteParticipantItem == null) {
            return;
        }
        String fromUserName = getUserName(message.getFromId());
        SpannableString text = new SpannableString(ApplicationSIP.applicationContext.getResources().getString(R.string.removed_participant, fromUserName, chatDeleteParticipantItem.getName()));
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ApplicationSIP.applicationContext, R.color.color_primary_dark)), 0, fromUserName.length(), 0);
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ApplicationSIP.applicationContext, R.color.color_primary_dark)), text.length() - chatDeleteParticipantItem.getName().length(), text.length(), 0);

        actionHolder.messageText.setText(text);
    }

    private void onBindChatDeletePhotoHolder(RecyclerView.ViewHolder holder, int position) {
        ChatChangesViewHolder actionHolder = (ChatChangesViewHolder) holder;
        MessageItem message = getItem(position);
        String fromUserName = getUserName(message.getFromId());
        SpannableString text = new SpannableString(ApplicationSIP.applicationContext.getResources().getString(R.string.removed_photo, fromUserName));
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ApplicationSIP.applicationContext, R.color.color_primary_dark)), 0, fromUserName.length(), 0);

        actionHolder.messageText.setText(text);
    }

    private void onBindGroupChatCreateHolder(RecyclerView.ViewHolder holder, int position) {
        ChatChangesViewHolder actionHolder = (ChatChangesViewHolder) holder;
        MessageItem message = getItem(position);
        GroupChatCreate groupChatCreate = (GroupChatCreate)message.getMessageContentItem();
        if (groupChatCreate == null) {
            return;
        }
        String fromUserName = getUserName(message.getFromId());
        SpannableString text = new SpannableString(ApplicationSIP.applicationContext.getResources().getString(R.string.created_group, fromUserName, groupChatCreate.getTitle()));
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ApplicationSIP.applicationContext, R.color.color_primary_dark)), 0, fromUserName.length(), 0);

        actionHolder.messageText.setText(text);
    }

    private void onBindAudioHolder(RecyclerView.ViewHolder holder, int position) {
        AudioViewHolder audioViewHolder = (AudioViewHolder) holder;
        MessageItem message = getItem(position);
        AudioItem audioItem = (AudioItem)message.getMessageContentItem();
        if (audioItem == null) {
            return;
        }
        // get user
        UserItem user = usersMap.get(message.getFromId());
        if (user != null) {
            // set name
            String name = user.getName();
            audioViewHolder.messageName.setText(name);
            // Set avatar
            audioViewHolder.avatar.setCircleRounds(true);
            audioViewHolder.avatar.setImageLoaderI(user);
        }
        // set data
        String dataString = TimeUtils.stringForMessageListDate(message.getDate());
        audioViewHolder.messageSendingTime.setText(dataString);
        // set unread outbox image
        setUnreadOutboxImages(message, audioViewHolder.messageUnreadOutbox);
        // set audio title and performer
        audioViewHolder.messageAudioTitle.setText(audioItem.getTitle());
        audioViewHolder.messageAudioPerformer.setDownloadingInfo(audioItem, audioItem.getPerformer(), audioItem.getAudioFileSize());
        //
        audioViewHolder.progressDownloadView.setFileDownloader(audioItem);
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

    ///// Helper methods for onBindViewHolders /////
    private void setUnreadOutboxImages(MessageItem message, ImageView messageUnreadOutbox) {
        if (message.getDate() == TEMP_SEND_STATE_IS_ERROR) {
            messageUnreadOutbox.setVisibility(View.VISIBLE);
            messageUnreadOutbox.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_message_error, ApplicationSIP.applicationContext.getTheme()));
        } else if (myUserId == message.getFromId()) {
            if (lastChatReadOutboxId >= message.getId()) {
                messageUnreadOutbox.setVisibility(View.GONE);
            } else {
                messageUnreadOutbox.setVisibility(View.VISIBLE);
                setSendStateMessage(message, messageUnreadOutbox);
            }
        } else {
            messageUnreadOutbox.setVisibility(View.GONE);
        }
    }

    private void setSendStateMessage(MessageItem message, ImageView imageView) {
        if (message.getId() >= TEMP_SEND_STATE_IS_SENDING) {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_clock, ApplicationSIP.applicationContext.getTheme()));
        } else {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(ApplicationSIP.applicationContext.getResources(), R.drawable.ic_not_read, ApplicationSIP.applicationContext.getTheme()));
        }
    }

    private String getUserName(int id) {
        UserItem userItem = usersMap.get(id);
        String fromUserName = "";
        if (userItem != null) {
            fromUserName = userItem.getName();
        }
        return fromUserName;
    }

    ///// ClickListeners helpers for onBindViewHolders /////
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
                if (photoFileId == photoItemLarge.getFileId()) {
                    clickedPosInPhotoList = currentPosInPhotoList;
                }
                currentPosInPhotoList++;
            }
        }
        return new Pair<>(clickedPosInPhotoList, photoLargeItemList);
    }

}
