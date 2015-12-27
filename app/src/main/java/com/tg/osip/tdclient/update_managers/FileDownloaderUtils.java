package com.tg.osip.tdclient.update_managers;

/**
 * Utils for ImageLoaderI inheritors
 *
 * @author e.matsyuk
 */
public class FileDownloaderUtils {

    private final static int EMPTY_FILE_ID = 0;
    private final static String EMPTY_STRING = "";

    public static boolean isFileIdValid(int photoFileId) {
        return photoFileId != EMPTY_FILE_ID;
    }

    public static boolean isFilePathValid(String photoFilePath) {
        return photoFilePath != null && !photoFilePath.equals(EMPTY_STRING);
    }

}
