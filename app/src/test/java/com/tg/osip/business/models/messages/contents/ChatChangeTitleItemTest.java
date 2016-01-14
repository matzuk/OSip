package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author e.matsyuk
 */
public class ChatChangeTitleItemTest {

    @Test
    public void constructor_nullMessageContent() {
        TdApi.MessageChatChangeTitle messageContent = null;
        ChatChangeTitleItem chatChangeTitleItem = new ChatChangeTitleItem(messageContent);
        assertThat(chatChangeTitleItem.getTitle()).isEqualTo("");
    }

    @Test
    public void constructor_nullMessageContentTitle() {
        TdApi.MessageChatChangeTitle messageContent = new TdApi.MessageChatChangeTitle();
        ChatChangeTitleItem chatChangeTitleItem = new ChatChangeTitleItem(messageContent);
        assertThat(chatChangeTitleItem.getTitle()).isEqualTo("");
    }

}