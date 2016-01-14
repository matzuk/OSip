package com.tg.osip.business.models.messages.contents;

import android.support.annotation.Nullable;
import android.text.format.Formatter;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.business.models.PhotoItem;
import com.tg.osip.tdclient.update_managers.FileDownloaderI;
import com.tg.osip.utils.CommonStaticFields;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class VideoItem extends MessageContentItem implements FileDownloaderI {

    private String caption;
    private int durationInSeconds;
    private PhotoItem thumb;
    private String videoFilePath;
    private String videoTGFilePath;
    private int videoFileId;
    private String fileSize;

    public VideoItem(TdApi.MessageVideo messageVideo) {
        if (messageVideo == null || messageVideo.video == null) {
            return;
        }
        caption = messageVideo.caption;
        initFields(messageVideo.video);
    }

    private void initFields(TdApi.Video video) {
        durationInSeconds = video.duration;
        if (video.thumb != null) {
            thumb = new PhotoItem(video.thumb);
        }
        if (video.video != null) {
            videoFileId = video.video.id;
            if (video.video.path != null && !video.video.path.equals(CommonStaticFields.EMPTY_STRING)) {
                videoFilePath = video.video.path;
                videoTGFilePath = CommonStaticFields.ADD_TO_PATH + videoFilePath;
            }
            fileSize = Formatter.formatFileSize(ApplicationSIP.applicationContext, video.video.size);
        }
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    @Nullable
    public PhotoItem getThumb() {
        return thumb;
    }

    @Override
    public String getFilePath() {
        if (videoFilePath == null) {
            return CommonStaticFields.EMPTY_STRING;
        }
        return videoFilePath;
    }

    @Override
    public String getTGFilePath() {
        if (videoTGFilePath == null) {
            return CommonStaticFields.EMPTY_STRING;
        }
        return videoTGFilePath;
    }

    @Override
    public int getFileId() {
        return videoFileId;
    }

    public String getFileSize() {
        if (fileSize == null) {
            return CommonStaticFields.EMPTY_STRING;
        }
        return fileSize;
    }

    public String getCaption() {
        if (caption == null) {
            return CommonStaticFields.EMPTY_STRING;
        }
        return caption;
    }
}
