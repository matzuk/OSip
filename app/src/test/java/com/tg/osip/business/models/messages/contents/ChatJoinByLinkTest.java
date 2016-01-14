package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author e.matsyuk
 */
public class ChatJoinByLinkTest {

    @Test
    public void constructor_nullMessageContent() {
        TdApi.MessageChatJoinByLink messageContent = null;
        ChatJoinByLink chatJoinByLink = new ChatJoinByLink(messageContent);
        assertThat(chatJoinByLink.getInviterId()).isEqualTo(0);
    }

    @Test
    public void constructor_nullMessageContentTitle() {
        TdApi.MessageChatJoinByLink messageContent = new TdApi.MessageChatJoinByLink();
        ChatJoinByLink chatJoinByLink = new ChatJoinByLink(messageContent);
        assertThat(chatJoinByLink.getInviterId()).isEqualTo(0);
    }

}