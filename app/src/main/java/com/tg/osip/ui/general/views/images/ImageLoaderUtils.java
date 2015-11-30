package com.tg.osip.ui.general.views.images;

/**
 * Utils for ImageLoaderI inheritors
 *
 * @author e.matsyuk
 */
public class ImageLoaderUtils {

    private final static int EMPTY_FILE_ID = 0;
    private final static String EMPTY_STRING = "";

    public static boolean isPhotoFileIdValid(int photoFileId) {
        return photoFileId != EMPTY_FILE_ID;
    }

    public static boolean isPhotoFilePathValid(String photoFilePath) {
        return !photoFilePath.equals(EMPTY_STRING);
    }

}
