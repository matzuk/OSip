package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author e.matsyuk
 */
public class MessageContentTextItemTest {

    @Test
     public void constructor_nullMessageContent() {
        TdApi.MessageContent messageContent = null;
        MessageContentTextItem messageContentTextItem = new MessageContentTextItem(messageContent);
        assertThat(messageContentTextItem.getText()).isEqualTo("");
    }

    @Test
    public void constructor_otherMessageContent() {
        TdApi.MessageAudio messageContent = new TdApi.MessageAudio();
        MessageContentTextItem messageContentTextItem = new MessageContentTextItem(messageContent);
        assertThat(messageContentTextItem.getText()).isEqualTo("");
    }

    @Test
    public void constructor_textMessageContent() {
        TdApi.MessageText messageContent = new TdApi.MessageText("123");
        MessageContentTextItem messageContentTextItem = new MessageContentTextItem(messageContent);
        assertThat(messageContentTextItem.getText()).isEqualTo("123");
    }

}