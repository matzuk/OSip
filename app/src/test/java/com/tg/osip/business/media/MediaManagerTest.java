package com.tg.osip.business.media;

import android.media.MediaPlayer;

import com.tg.osip.utils.CommonStaticFields;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author e.matsyuk
 */
public class MediaManagerTest {

    @Mock
    MediaPlayer mediaPlayer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(mediaPlayer.isPlaying()).thenReturn(true);
    }

    @Test
    public void cycle_Correct() throws IOException {
        MediaManager mediaManager = new MediaManager(mediaPlayer);
        // play
        mediaManager.play("123", 1);
        assertThat(mediaManager.getCurrentIdFile()).isEqualTo(1);
        assertThat(mediaManager.isPaused()).isEqualTo(false);
        verify(mediaPlayer).reset();
        verify(mediaPlayer).setDataSource("123");
        verify(mediaPlayer).prepare();
        // pause
        mediaManager.pause();
        assertThat(mediaManager.getCurrentIdFile()).isEqualTo(1);
        assertThat(mediaManager.isPaused()).isEqualTo(true);
        verify(mediaPlayer).pause();
        // play another file
        mediaManager.play("1234", 2);
        assertThat(mediaManager.getCurrentIdFile()).isEqualTo(2);
        assertThat(mediaManager.isPaused()).isEqualTo(false);
        verify(mediaPlayer).setDataSource("1234");
        // reset
        mediaManager.reset();
        assertThat(mediaManager.getCurrentIdFile()).isEqualTo(CommonStaticFields.EMPTY_FILE_ID);
        assertThat(mediaManager.isPaused()).isEqualTo(false);
        // play another file
        mediaManager.play("12345", 3);
        assertThat(mediaManager.getCurrentIdFile()).isEqualTo(3);
        assertThat(mediaManager.isPaused()).isEqualTo(false);
        verify(mediaPlayer).setDataSource("12345");
    }

    @Test
    public void cycle_Incorrect() throws IOException {
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);
        Mockito.doThrow(new IOException()).when(mediaPlayer).setDataSource(anyString());

        MediaManager mediaManager = new MediaManager(mediaPlayer);
        mediaManager.play("123", 1);
        assertThat(mediaManager.isPaused()).isEqualTo(false);
        assertThat(mediaManager.getCurrentIdFile()).isEqualTo(CommonStaticFields.EMPTY_FILE_ID);
    }

}