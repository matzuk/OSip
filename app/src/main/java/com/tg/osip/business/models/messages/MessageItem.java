package com.tg.osip.business.models.messages;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.tg.osip.business.models.PhotoItem;
import com.tg.osip.business.models.messages.contents.MessageContentActionsItem;
import com.tg.osip.business.models.messages.contents.MessageContentItem;
import com.tg.osip.business.models.messages.contents.MessageContentPhotoItem;
import com.tg.osip.business.models.messages.contents.MessageContentTextItem;
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
        ACTION_TYPE,
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
        initMessageType(message);
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

    void initMessageType(TdApi.Message message) {
        if (message == null) {
            contentType = ContentType.NULL_TYPE;
        } else if (message.message == null) {
            contentType = ContentType.NULL_TYPE;
        } else if (message.message.getClass() == TdApi.MessageText.class) {
            contentType = ContentType.TEXT_MESSAGE_TYPE;
        } else if (message.message.getClass() == TdApi.MessagePhoto.class) {
            contentType = ContentType.PHOTO_MESSAGE_TYPE;
        } else if (message.message.getClass() == TdApi.MessageChatAddParticipant.class ||
                message.message.getClass() == TdApi.MessageChatChangePhoto.class ||
                message.message.getClass() == TdApi.MessageChatChangeTitle.class ||
                message.message.getClass() == TdApi.MessageChatDeleteParticipant.class ||
                message.message.getClass() == TdApi.MessageChatDeletePhoto.class) {
            contentType = ContentType.ACTION_TYPE;
        } else {
            contentType = ContentType.UNSUPPORTED_TYPE;
        }
    }

    void initMessageContent(TdApi.Message message) {
        if (message == null || message.message == null) {
            return;
        }
        switch (contentType) {
            case TEXT_MESSAGE_TYPE:
                messageContentItem = new MessageContentTextItem(message.message);
                break;
            case PHOTO_MESSAGE_TYPE:
                messageContentItem = new MessageContentPhotoItem(message.message);
                break;
            case ACTION_TYPE:
                messageContentItem = new MessageContentActionsItem(message.message);
                break;
            case UNSUPPORTED_TYPE:
                messageContentItem = null;
                break;
            case NULL_TYPE:
                messageContentItem = null;
                break;
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
