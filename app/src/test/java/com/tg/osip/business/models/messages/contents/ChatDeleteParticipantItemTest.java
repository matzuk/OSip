package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author e.matsyuk
 */
public class ChatDeleteParticipantItemTest {

    @Test
    public void constructor_nullMessageContent() {
        TdApi.MessageChatDeleteParticipant messageContent = null;
        ChatDeleteParticipantItem chatDeleteParticipantItem = new ChatDeleteParticipantItem(messageContent);
        assertThat(chatDeleteParticipantItem.getName()).isEqualTo("");
    }

    @Test
    public void constructor_nullUserMessageContent() {
        TdApi.MessageChatDeleteParticipant messageChatDeleteParticipant = new TdApi.MessageChatDeleteParticipant();
        TdApi.User user = new TdApi.User();
        messageChatDeleteParticipant.user = user;

        ChatDeleteParticipantItem chatAddParticipantItem = new ChatDeleteParticipantItem(messageChatDeleteParticipant);
        assertThat(chatAddParticipantItem.getName()).isEqualTo("");
    }

}