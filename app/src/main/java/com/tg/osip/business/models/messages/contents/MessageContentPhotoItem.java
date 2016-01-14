package com.tg.osip.business.models.messages.contents;

import android.support.annotation.Nullable;

import com.tg.osip.business.models.PhotoItem;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class MessageContentPhotoItem extends MessageContentPhoto<TdApi.MessagePhoto> {

    public MessageContentPhotoItem(TdApi.MessagePhoto messageContent) {
        super(messageContent);
    }

    @Override
    TdApi.Photo getPhoto(TdApi.MessagePhoto messageContent) {
        if (messageContent == null) {
            return null;
        }
        return messageContent.photo;
    }

}
