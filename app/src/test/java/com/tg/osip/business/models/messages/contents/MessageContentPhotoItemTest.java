package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * @author e.matsyuk
 */
public class MessageContentPhotoItemTest {

    @Test
    public void constructor_nullMessageContent() {
        TdApi.MessageContent messageContent = null;
        MessageContentPhotoItem messageContentPhotoItem = new MessageContentPhotoItem(messageContent);
        assertThat(messageContentPhotoItem.getPhotoItemMedium()).isNull();
        assertThat(messageContentPhotoItem.getPhotoItemLarge()).isNull();
    }

    @Test
    public void constructor_otherMessageContent() {
        TdApi.MessageContent messageContent = new TdApi.MessageAudio();
        MessageContentPhotoItem messageContentPhotoItem = new MessageContentPhotoItem(messageContent);
        assertThat(messageContentPhotoItem.getPhotoItemMedium()).isNull();
        assertThat(messageContentPhotoItem.getPhotoItemLarge()).isNull();
    }

}