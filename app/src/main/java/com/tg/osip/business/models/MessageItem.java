package com.tg.osip.business.models;

import com.tg.osip.ui.messages.MessagesRecyclerAdapter;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Comfortable model for {@link MessagesRecyclerAdapter MessagesRecyclerAdapter}
 *
 * @author e.matsyuk
 */
public class MessageItem {

    private static final String PHOTO_TYPE_M = "m";
    private static final String PHOTO_TYPE_Y = "y";

    private TdApi.Message message;

    private PhotoItem photoItemM;
    private PhotoItem photoItemY;

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
                photoItemM = new PhotoItem(photoSize);
            } else if (photoSize.type.equals(PHOTO_TYPE_Y)) {
                photoItemY = new PhotoItem(photoSize);
                photoItemY.setPlugFile(photoItemM);
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
    public PhotoItem getPhotoItemM() {
        return photoItemM;
    }

    /**
     * @return if isPhotoMessage == true then PhotoItem Y type or null
     */
    public PhotoItem getPhotoItemY() {
        return photoItemY;
    }

}
