package com.tg.osip.business.models;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.Serializable;

/**
 * Photo model for messages and photo activities
 *
 * @author e.matsyul
 */
public class PhotoItem implements ImageLoaderI, Serializable {

    private final static int EMPTY_FILE_ID = 0;
    private final static String ADD_TO_PATH = "file://";
    private final static String EMPTY_STRING = "";

    private int photoFileId;
    private String photoFilePath;
    private int width;
    private int height;
    transient private Drawable plug;

    public PhotoItem (TdApi.PhotoSize photoSize) {
        initFileId(photoSize);
        initFilePath(photoSize);
        initPlug();
        width = photoSize.width;
        height = photoSize.height;
    }

    private void initFileId(TdApi.PhotoSize photoSize) {
        photoFileId = photoSize.photo.id;
    }

    private void initFilePath(TdApi.PhotoSize photoSize) {
        String filePath = photoSize.photo.path;
        if (!TextUtils.isEmpty(filePath)) {
            photoFilePath = ADD_TO_PATH + filePath;
            return;
        }
        photoFilePath = EMPTY_STRING;
    }

    private void initPlug() {
        // temp null plug
        plug = null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public int getPhotoFileId() {
        return photoFileId;
    }

    @Override
    public boolean isPhotoFileIdValid() {
        return photoFileId != EMPTY_FILE_ID;
    }

    @Override
    public String getPhotoFilePath() {
        return photoFilePath;
    }

    @Override
    public boolean isPhotoFilePathValid() {
        return !photoFilePath.equals(EMPTY_STRING);
    }

    @Override
    public Drawable getPlug() {
        return plug;
    }

}
