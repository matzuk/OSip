package com.tg.osip.tdclient.update_managers;

import com.tg.osip.business.models.messages.contents.ChatAddParticipantItem;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author e.matsyuk
 */
public class FileDownloaderUtilsTest {

    @Test
    public void isFileIdValid_zeroInt() {
        assertThat(FileDownloaderUtils.isFileIdValid(0)).isEqualTo(false);
    }

    @Test
    public void isFileIdValid_nonZeroInt() {
        assertThat(FileDownloaderUtils.isFileIdValid(5)).isEqualTo(true);
    }

    @Test
    public void isFilePathValid_null() {
        assertThat(FileDownloaderUtils.isFilePathValid(null)).isEqualTo(false);
    }

    @Test
    public void isFilePathValid_empty() {
        assertThat(FileDownloaderUtils.isFilePathValid("")).isEqualTo(false);
    }

    @Test
    public void isFilePathValid_nonEmpty() {
        assertThat(FileDownloaderUtils.isFilePathValid("sfg")).isEqualTo(true);
    }

}