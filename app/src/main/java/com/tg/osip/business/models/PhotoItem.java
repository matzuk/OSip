package com.tg.osip.business.models;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.tg.osip.ui.general.views.images.ImageLoaderI;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.Serializable;

/**
 * Photo model for messages and photo activities
 *
 * @author e.matsyul
 */
public class PhotoItem implements ImageLoaderI, Serializable {

    private final static String ADD_TO_PATH = "file://";
    private final static String EMPTY_STRING = "";

    private int photoFileId;
    private String photoFilePath;
    private int width;
    private int height;
    private PhotoItem plugFile;
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

    public void setPlugFile(PhotoItem plugFile) {
        this.plugFile = plugFile;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public int getFileId() {
        return photoFileId;
    }

    @Override
    public String getFilePath() {
        return photoFilePath;
    }

    @Override
    public Drawable getPlug() {
        return plug;
    }

    @Override
    public ImageLoaderI getPlugFile() {
        return plugFile;
    }

}
