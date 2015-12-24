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

    enum ContentType {
        PHOTO_MESSAGE_TYPE,
        TEXT_MESSAGE_TYPE,
        ACTION_TYPE,
        UNSUPPORTED_TYPE,
        NULL_TYPE
    }

    private static final String PHOTO_TYPE_S = "s";
    private static final String PHOTO_TYPE_M = "m";
    private static final String PHOTO_TYPE_X = "x";
    private static final String PHOTO_TYPE_Y = "y";

    private int id;
    private int fromId;
    private long chatId;
    private int date;
    private int forwardFromId;
    private int forwardDate;

    private ContentType contentType;

    private MessageContentItem messageContentItem;

    private TdApi.Message message;

    private PhotoItem photoItemMedium;
    private PhotoItem photoItemLarge;

    @VisibleForTesting
    MessageItem() { }

    public MessageItem(TdApi.Message message) {
        this.message = message;
        initMessageFields(message);
        initMessageType(message);
        initMessageContent(message);
        init(message);
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

    private void init(TdApi.Message message) {
        if (!isPhotoMessage()) {
            return;
        }
        TdApi.MessagePhoto messagePhoto = (TdApi.MessagePhoto)message.message;
        TdApi.PhotoSize[] photoSizes = messagePhoto.photo.photos;
        for (TdApi.PhotoSize photoSize : photoSizes) {
            if (photoSize.type.equals(PHOTO_TYPE_M)) {
                photoItemMedium = new PhotoItem(photoSize);
            } else if (photoSize.type.equals(PHOTO_TYPE_Y)) {
                photoItemLarge = new PhotoItem(photoSize);
                photoItemLarge.setPlugFile(photoItemMedium);
            }
        }
        // if type there is not TYPE_M then set TYPE_S
        if (photoItemMedium == null) {
            for (TdApi.PhotoSize photoSize : photoSizes) {
                if (photoSize.type.equals(PHOTO_TYPE_S)) {
                    photoItemMedium = new PhotoItem(photoSize);
                }
            }
        }
        // if type there is not TYPE_Y then set TYPE_X
        if (photoItemLarge == null) {
            for (TdApi.PhotoSize photoSize : photoSizes) {
                if (photoSize.type.equals(PHOTO_TYPE_X)) {
                    photoItemLarge = new PhotoItem(photoSize);
                    photoItemLarge.setPlugFile(photoItemMedium);
                }
            }
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


    public TdApi.Message getMessage() {
        return message;
    }

    public boolean isPhotoMessage() {
        return message.message.getClass() == TdApi.MessagePhoto.class;
    }

    /**
     * @return if isPhotoMessage == true then PhotoItem M type or null
     */
    public PhotoItem getPhotoItemMedium() {
        return photoItemMedium;
    }

    /**
     * @return if isPhotoMessage == true then PhotoItem Y type or null
     */
    public PhotoItem getPhotoItemLarge() {
        return photoItemLarge;
    }

}
