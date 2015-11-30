package com.tg.osip.business.models;

import com.tg.osip.ui.messages.MessagesRecyclerAdapter;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Comfortable model for {@link MessagesRecyclerAdapter MessagesRecyclerAdapter}
 *
 * @author e.matsyuk
 */
public class MessageItem {

    private static final String PHOTO_TYPE_S = "s";
    private static final String PHOTO_TYPE_M = "m";
    private static final String PHOTO_TYPE_X = "x";
    private static final String PHOTO_TYPE_Y = "y";

    private TdApi.Message message;

    private PhotoItem photoItemMedium;
    private PhotoItem photoItemLarge;

    public MessageItem(TdApi.Message message) {
        this.message = message;
        init();
    }

    private void init() {
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
