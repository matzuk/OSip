package com.tg.osip.business.models.messages.contents;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.tg.osip.business.models.PhotoItem;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Abstract class for photo content items
 *
 * @author e.matsyuk
 */
public abstract class MessageContentPhoto<T extends TdApi.MessageContent> extends MessageContentItem {

    private static final String PHOTO_TYPE_S = "s";
    private static final String PHOTO_TYPE_M = "m";
    private static final String PHOTO_TYPE_X = "x";
    private static final String PHOTO_TYPE_Y = "y";

    private PhotoItem photoItemMedium;
    private PhotoItem photoItemLarge;

    @VisibleForTesting
    public MessageContentPhoto() {}

    public MessageContentPhoto(T messageContent) {
        TdApi.Photo photo = getPhoto(messageContent);
        init(photo);
    }

    @Nullable
    abstract TdApi.Photo getPhoto(T messageContent);

    void init(TdApi.Photo photo) {
        if (photo == null || photo.photos == null) {
            return;
        }
        TdApi.PhotoSize[] photoSizes = photo.photos;

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

    /**
     * @return if isPhotoMessage == true then PhotoItem M type or null
     */
    @Nullable
    public PhotoItem getPhotoItemMedium() {
        return photoItemMedium;
    }

    /**
     * @return if isPhotoMessage == true then PhotoItem Y type or null
     */
    @Nullable
    public PhotoItem getPhotoItemLarge() {
        return photoItemLarge;
    }

}
