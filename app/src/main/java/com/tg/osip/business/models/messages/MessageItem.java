package com.tg.osip.business.models.messages;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.tg.osip.business.models.messages.contents.AudioItem;
import com.tg.osip.business.models.messages.contents.ChatDeleteParticipantItem;
import com.tg.osip.business.models.messages.contents.ChatDeletePhoto;
import com.tg.osip.business.models.messages.contents.ChatJoinByLink;
import com.tg.osip.business.models.messages.contents.ChatAddParticipantItem;
import com.tg.osip.business.models.messages.contents.ChatChangePhotoItem;
import com.tg.osip.business.models.messages.contents.ChatChangeTitleItem;
import com.tg.osip.business.models.messages.contents.GroupChatCreate;
import com.tg.osip.business.models.messages.contents.MessageContentItem;
import com.tg.osip.business.models.messages.contents.MessageContentPhotoItem;
import com.tg.osip.business.models.messages.contents.MessageContentTextItem;
import com.tg.osip.business.models.messages.contents.VideoItem;
import com.tg.osip.ui.messages.MessagesRecyclerAdapter;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Comfortable model for {@link MessagesRecyclerAdapter MessagesRecyclerAdapter}
 *
 * @author e.matsyuk
 */
public class MessageItem {

    public enum ContentType {
        PHOTO_MESSAGE_TYPE,
        TEXT_MESSAGE_TYPE,
        CHAT_ADD_PARTICIPANT,
        CHAT_CHANGE_PHOTO,
        CHAT_CHANGE_TITLE,
        CHAT_DELETE_PARTICIPANT,
        CHAT_DELETE_PHOTO,
        CHAT_JOIN_BY_LINK,
        GROUP_CHAT_CREATE,
        AUDIO,
        VIDEO,
        UNSUPPORTED_TYPE,
        NULL_TYPE
    }

    private int id;
    private int fromId;
    private long chatId;
    private int date;
    private int forwardFromId;
    private int forwardDate;

    private ContentType contentType;
    private MessageContentItem messageContentItem;

    @VisibleForTesting
    MessageItem() { }

    public MessageItem(TdApi.Message message) {
        initMessageFields(message);
        initMessageContent(message);
    }

    void initMessageFields(TdApi.Message message) {
        if (message == null) {
            return;
        }
        id = message.id;
        fromId = message.fromId;
        chatId = message.chatId;
        date = message.date;
        forwardFromId = message.forwardFromId;
        forwardDate = message.forwardDate;
    }

    void initMessageContent(TdApi.Message message) {
        if (message == null) {
            contentType = ContentType.NULL_TYPE;
        } else if (message.message == null) {
            contentType = ContentType.NULL_TYPE;
        } else if (message.message.getClass() == TdApi.MessageText.class) {
            contentType = ContentType.TEXT_MESSAGE_TYPE;
            messageContentItem = new MessageContentTextItem((TdApi.MessageText)message.message);
        } else if (message.message.getClass() == TdApi.MessagePhoto.class) {
            contentType = ContentType.PHOTO_MESSAGE_TYPE;
            messageContentItem = new MessageContentPhotoItem((TdApi.MessagePhoto)message.message);
        } else if (message.message.getClass() == TdApi.MessageChatAddParticipant.class) {
            contentType = ContentType.CHAT_ADD_PARTICIPANT;
            messageContentItem = new ChatAddParticipantItem((TdApi.MessageChatAddParticipant)message.message);
        } else if (message.message.getClass() == TdApi.MessageChatChangePhoto.class) {
            contentType = ContentType.CHAT_CHANGE_PHOTO;
            messageContentItem = new ChatChangePhotoItem((TdApi.MessageChatChangePhoto)message.message);
        } else if (message.message.getClass() == TdApi.MessageChatChangeTitle.class) {
            contentType = ContentType.CHAT_CHANGE_TITLE;
            messageContentItem = new ChatChangeTitleItem((TdApi.MessageChatChangeTitle)message.message);
        } else if (message.message.getClass() == TdApi.MessageChatDeleteParticipant.class) {
            contentType = ContentType.CHAT_DELETE_PARTICIPANT;
            messageContentItem = new ChatDeleteParticipantItem((TdApi.MessageChatDeleteParticipant)message.message);
        } else if (message.message.getClass() == TdApi.MessageChatDeletePhoto.class) {
            contentType = ContentType.CHAT_DELETE_PHOTO;
            messageContentItem = new ChatDeletePhoto();
        } else if (message.message.getClass() == TdApi.MessageChatJoinByLink.class) {
            contentType = ContentType.CHAT_JOIN_BY_LINK;
            messageContentItem = new ChatJoinByLink((TdApi.MessageChatJoinByLink)message.message);
        } else if (message.message.getClass() == TdApi.MessageGroupChatCreate.class) {
            contentType = ContentType.GROUP_CHAT_CREATE;
            messageContentItem = new GroupChatCreate((TdApi.MessageGroupChatCreate)message.message);
        } else if (message.message.getClass() == TdApi.MessageAudio.class) {
            contentType = ContentType.AUDIO;
            messageContentItem = new AudioItem((TdApi.MessageAudio)message.message);
        } else if (message.message.getClass() == TdApi.MessageVideo.class) {
            contentType = ContentType.VIDEO;
            messageContentItem = new VideoItem((TdApi.MessageVideo)message.message);
        }  else {
            contentType = ContentType.UNSUPPORTED_TYPE;
        }
    }

    public int getId() {
        return id;
    }

    public int getFromId() {
        return fromId;
    }

    public long getChatId() {
        return chatId;
    }

    public int getDate() {
        return date;
    }

    public int getForwardFromId() {
        return forwardFromId;
    }

    public int getForwardDate() {
        return forwardDate;
    }

    public ContentType getContentType() {
        return contentType;
    }

    @Nullable
    public MessageContentItem getMessageContentItem() {
        return messageContentItem;
    }

}
