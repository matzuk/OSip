package com.tg.osip.business.models.messages.contents;

import com.tg.osip.tdclient.update_managers.FileDownloaderI;
import com.tg.osip.utils.CommonStaticFields;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class AudioItem extends MessageContentItem implements FileDownloaderI {

    private String fileName;
    private String mimeType;
    private String performer;
    private String title;
    private int duration;
    private int audioFileId;
    private String audioFilePath;
    private String audioTGFilePath;

    public AudioItem(TdApi.MessageAudio messageAudio) {
        if (messageAudio == null || messageAudio.audio == null) {
            return;
        }
        initSimpleFields(messageAudio);
        initFileFields(messageAudio);
    }

    private void initSimpleFields(TdApi.MessageAudio messageAudio) {
        fileName = messageAudio.audio.fileName;
        mimeType = messageAudio.audio.mimeType;
        performer = messageAudio.audio.performer;
        title = messageAudio.audio.title;
        duration = messageAudio.audio.duration;
    }

    private void initFileFields(TdApi.MessageAudio messageAudio) {
        if (messageAudio.audio.audio == null) {
            return;
        }
        audioFileId = messageAudio.audio.audio.id;
        String filePath = messageAudio.audio.audio.path;
        if (filePath != null && !filePath.equals(CommonStaticFields.EMPTY_STRING)) {
            audioTGFilePath = CommonStaticFields.ADD_TO_PATH + filePath;
            audioFilePath = filePath;
        }
    }

    @Override
    public String getTGFilePath() {
        if (audioTGFilePath == null) {
            return CommonStaticFields.EMPTY_STRING;
        }
        return audioTGFilePath;
    }

    @Override
    public String getFilePath() {
        if (audioFilePath == null) {
            return CommonStaticFields.EMPTY_STRING;
        }
        return audioFilePath;
    }

    @Override
    public int getFileId() {
        return audioFileId;
    }


    public String getFileName() {
        if (fileName == null) {
            return CommonStaticFields.EMPTY_STRING;
        }
        return fileName;
    }

    public String getMimeType() {
        if (mimeType == null) {
            return CommonStaticFields.EMPTY_STRING;
        }
        return mimeType;
    }

    public String getPerformer() {
        if (performer == null) {
            return CommonStaticFields.EMPTY_STRING;
        }
        return performer;
    }

    public String getTitle() {
        if (title == null) {
            return CommonStaticFields.EMPTY_STRING;
        }
        return title;
    }

    public int getDuration() {
        return duration;
    }
}
