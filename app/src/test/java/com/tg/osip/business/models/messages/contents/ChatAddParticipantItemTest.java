package com.tg.osip.business.models.messages.contents;


import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author e.matsyuk
 */
public class ChatAddParticipantItemTest {

    @Test
    public void constructor_nullMessageContent() {
        TdApi.MessageChatAddParticipant messageContent = null;
        ChatAddParticipantItem chatAddParticipantItem = new ChatAddParticipantItem(messageContent);
        assertThat(chatAddParticipantItem.getName()).isEqualTo("");
    }

    @Test
    public void constructor_nullUserMessageContent() {
        TdApi.MessageChatAddParticipant messageChatAddParticipant = new TdApi.MessageChatAddParticipant();
        TdApi.User user = new TdApi.User();
        messageChatAddParticipant.user = user;

        ChatAddParticipantItem chatAddParticipantItem = new ChatAddParticipantItem(messageChatAddParticipant);
        assertThat(chatAddParticipantItem.getName()).isEqualTo("");
    }

}