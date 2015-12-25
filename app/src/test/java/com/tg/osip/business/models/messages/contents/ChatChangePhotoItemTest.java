package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author e.matsyuk
 */
public class ChatChangePhotoItemTest {

    @Test
    public void constructor_nullMessageContent() {
        ChatChangePhotoItem messageContentPhotoItem = new ChatChangePhotoItem(null);
        assertThat(messageContentPhotoItem.getPhotoItemMedium()).isNull();
        assertThat(messageContentPhotoItem.getPhotoItemLarge()).isNull();
    }

}