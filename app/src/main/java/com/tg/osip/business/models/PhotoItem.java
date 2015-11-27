package com.tg.osip.business.models;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.tg.osip.ui.general.views.images.ImageLoaderI;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Photo model for messages and photo activities
 *
 * @author e.matsyul
 */
public class PhotoItem implements ImageLoaderI {

    private final static int EMPTY_FILE_ID = 0;
    private final static String ADD_TO_PATH = "file://";
    private final static String EMPTY_STRING = "";

    private TdApi.PhotoSize photoSize;
    private int smallPhotoFileId;
    private String smallPhotoFilePath;
    private Drawable plug;

    public PhotoItem (TdApi.PhotoSize photoSize) {
        this.photoSize = photoSize;
        initFileId();
        initFilePath();
        initPlug();
    }

    private void initFileId() {
        smallPhotoFileId = photoSize.photo.id;
    }

    private void initFilePath() {
        String filePath = photoSize.photo.path;
        if (!TextUtils.isEmpty(filePath)) {
            smallPhotoFilePath = ADD_TO_PATH + filePath;
            return;
        }
        smallPhotoFilePath = EMPTY_STRING;
    }

    private void initPlug() {
        // temp null plug
        plug = null;
    }

    @Override
    public int getSmallPhotoFileId() {
        return smallPhotoFileId;
    }

    @Override
    public boolean isSmallPhotoFileIdValid() {
        return smallPhotoFileId != EMPTY_FILE_ID;
    }

    @Override
    public String getSmallPhotoFilePath() {
        return smallPhotoFilePath;
    }

    @Override
    public boolean isSmallPhotoFilePathValid() {
        return !smallPhotoFilePath.equals(EMPTY_STRING);
    }

    @Override
    public Drawable getPlug() {
        return plug;
    }

}
