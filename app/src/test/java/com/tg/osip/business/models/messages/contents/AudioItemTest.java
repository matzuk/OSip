package com.tg.osip.business.models.messages.contents;

import android.text.format.Formatter;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author e.matsyuk
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Formatter.class)
public class AudioItemTest {

    @Before
    public void beforeEachTest() {
        PowerMockito.mockStatic(Formatter.class);
        Mockito.when(Formatter.formatFileSize(anyObject(), anyInt())).thenReturn("");
    }

    @Test
    public void constructor_nullMessageContent() {
        AudioItem audioItem = new AudioItem(null);
        assertThat(audioItem.getFileName()).isNotNull();
        assertThat(audioItem.getMimeType()).isNotNull();
        assertThat(audioItem.getPerformer()).isNotNull();
        assertThat(audioItem.getTitle()).isNotNull();
        assertThat(audioItem.getTGFilePath()).isNotNull();
        assertThat(audioItem.getAudioFileSize()).isNotNull();
        assertThat(audioItem.getAudioFileSizeString()).isNotNull();
    }

    @Test
    public void constructor_nullMessageAudio() {
        TdApi.MessageAudio messageAudio = new TdApi.MessageAudio();
        AudioItem audioItem = new AudioItem(messageAudio);
        assertThat(audioItem.getFileName()).isNotNull();
        assertThat(audioItem.getMimeType()).isNotNull();
        assertThat(audioItem.getPerformer()).isNotNull();
        assertThat(audioItem.getTitle()).isNotNull();
        assertThat(audioItem.getTGFilePath()).isNotNull();
        assertThat(audioItem.getAudioFileSize()).isNotNull();
        assertThat(audioItem.getAudioFileSizeString()).isNotNull();
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
        assertThat(audioItem.getTGFilePath()).isEqualTo("");
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
        assertThat(audioItem.getTGFilePath()).isEqualTo("");
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
        assertThat(audioItem.getTGFilePath()).isEqualTo("file://qqq");
    }

}