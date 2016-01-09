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

/**
 * @author e.matsyuk
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Formatter.class)
public class VideoItemTest {

    @Before
    public void beforeEachTest() {
        PowerMockito.mockStatic(Formatter.class);
        Mockito.when(Formatter.formatFileSize(anyObject(), anyInt())).thenReturn("");
    }

    @Test
    public void constructor_nullMessageContent() {
        VideoItem videoItem = new VideoItem(null);
        assertThat(videoItem.getCaption()).isNotNull();
        assertThat(videoItem.getFilePath()).isNotNull();
        assertThat(videoItem.getFileSize()).isNotNull();
        assertThat(videoItem.getTGFilePath()).isNotNull();
        assertThat(videoItem.getThumb()).isNull();
    }

    @Test
    public void constructor_nullMessageVideo() {
        TdApi.MessageVideo messageVideo = new TdApi.MessageVideo();
        VideoItem videoItem = new VideoItem(messageVideo);
        assertThat(videoItem.getCaption()).isNotNull();
        assertThat(videoItem.getFilePath()).isNotNull();
        assertThat(videoItem.getFileSize()).isNotNull();
        assertThat(videoItem.getTGFilePath()).isNotNull();
        assertThat(videoItem.getThumb()).isNull();
    }

}