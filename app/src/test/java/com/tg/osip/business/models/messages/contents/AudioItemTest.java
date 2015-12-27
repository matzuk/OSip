package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author e.matsyuk
 */
public class AudioItemTest {

    @Test
    public void constructor_nullMessageContent() {
        AudioItem audioItem = new AudioItem(null);
        assertThat(audioItem.getFileName()).isNotNull();
        assertThat(audioItem.getMimeType()).isNotNull();
        assertThat(audioItem.getPerformer()).isNotNull();
        assertThat(audioItem.getTitle()).isNotNull();
        assertThat(audioItem.getFilePath()).isNotNull();
    }

    @Test
    public void constructor_nullMessageAudio() {
        TdApi.MessageAudio messageAudio = new TdApi.MessageAudio();
        AudioItem audioItem = new AudioItem(messageAudio);
        assertThat(audioItem.getFileName()).isNotNull();
        assertThat(audioItem.getMimeType()).isNotNull();
        assertThat(audioItem.getPerformer()).isNotNull();
        assertThat(audioItem.getTitle()).isNotNull();
        assertThat(audioItem.getFilePath()).isNotNull();
    }

    @Test
    public void constructor_messageAudio() {
        TdApi.Audio audio = new TdApi.Audio();
        audio.fileName = "fileName";
        audio.mimeType = "mimeType";
        audio.performer = "performer";
        audio.title = "title";
        audio.duration = 10;

        TdApi.MessageAudio messageAudio = new TdApi.MessageAudio();
        messageAudio.audio = audio;
        AudioItem audioItem = new AudioItem(messageAudio);
        assertThat(audioItem.getFileName()).isEqualTo("fileName");
        assertThat(audioItem.getMimeType()).isEqualTo("mimeType");
        assertThat(audioItem.getPerformer()).isEqualTo("performer");
        assertThat(audioItem.getTitle()).isEqualTo("title");
        assertThat(audioItem.getDuration()).isEqualTo(10);
    }

    @Test
    public void constructor_messageAudioNullFileAudio() {
        TdApi.Audio audio = new TdApi.Audio();
        audio.audio = null;

        TdApi.MessageAudio messageAudio = new TdApi.MessageAudio();
        messageAudio.audio = audio;
        AudioItem audioItem = new AudioItem(messageAudio);
        assertThat(audioItem.getFilePath()).isEqualTo("");
    }

    @Test
    public void constructor_messageAudioEmptyFileAudio() {
        TdApi.File file = new TdApi.File();
        file.path = "";

        TdApi.Audio audio = new TdApi.Audio();
        audio.audio = file;

        TdApi.MessageAudio messageAudio = new TdApi.MessageAudio();
        messageAudio.audio = audio;
        AudioItem audioItem = new AudioItem(messageAudio);
        assertThat(audioItem.getFilePath()).isEqualTo("");
    }

    @Test
    public void constructor_messageAudioFileAudio() {
        TdApi.File file = new TdApi.File();
        file.path = "qqq";

        TdApi.Audio audio = new TdApi.Audio();
        audio.audio = file;

        TdApi.MessageAudio messageAudio = new TdApi.MessageAudio();
        messageAudio.audio = audio;
        AudioItem audioItem = new AudioItem(messageAudio);
        assertThat(audioItem.getFilePath()).isEqualTo("file://qqq");
    }

}