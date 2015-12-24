package com.tg.osip.business.models.messages.contents;

import android.support.annotation.Nullable;

import com.tg.osip.business.models.PhotoItem;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class MessageContentPhotoItem extends MessageContentItem {

    private static final String PHOTO_TYPE_S = "s";
    private static final String PHOTO_TYPE_M = "m";
    private static final String PHOTO_TYPE_X = "x";
    private static final String PHOTO_TYPE_Y = "y";

    private PhotoItem photoItemMedium;
    private PhotoItem photoItemLarge;

    public MessageContentPhotoItem(TdApi.MessageContent messageContent) {
        init(messageContent);
    }

    private void init(TdApi.MessageContent messageContent) {
        if (messageContent == null || messageContent.getClass() != TdApi.MessagePhoto.class) {
            return;
        }
        TdApi.MessagePhoto messagePhoto = (TdApi.MessagePhoto)messageContent;
        if (messagePhoto.photo == null || messagePhoto.photo.photos == null) {
            return;
        }
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

    @Nullable
    public PhotoItem getPhotoItemMedium() {
        return photoItemMedium;
    }

    @Nullable
    public PhotoItem getPhotoItemLarge() {
        return photoItemLarge;
    }

}
