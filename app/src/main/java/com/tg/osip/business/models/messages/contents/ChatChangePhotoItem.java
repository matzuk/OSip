package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class ChatChangePhotoItem extends MessageContentPhoto<TdApi.MessageChatChangePhoto> {

    public ChatChangePhotoItem(TdApi.MessageChatChangePhoto messageContent) {
        super(messageContent);
    }

    @Override
    TdApi.Photo getPhoto(TdApi.MessageChatChangePhoto messageContent) {
        if (messageContent == null) {
            return null;
        }
        return messageContent.photo;
    }
}
