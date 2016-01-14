package com.tg.osip.tdclient.update_managers;

import com.tg.osip.utils.CommonStaticFields;

/**
 * Utils for ImageLoaderI inheritors
 *
 * @author e.matsyuk
 */
public class FileDownloaderUtils {

    public static boolean isFileIdValid(int photoFileId) {
        return photoFileId != CommonStaticFields.EMPTY_FILE_ID;
    }

    public static boolean isFilePathValid(String photoFilePath) {
        return photoFilePath != null && !photoFilePath.equals(CommonStaticFields.EMPTY_STRING);
    }

}
