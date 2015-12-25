package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author e.matsyuk
 */
public class GroupChatCreateTest {

    @Test
    public void constructor_nullMessageContent() {
        GroupChatCreate groupChatCreate = new GroupChatCreate(null);
        assertThat(groupChatCreate.getTitle()).isEqualTo("");
    }

    @Test
    public void constructor_nullMessageContentTitle() {
        TdApi.MessageGroupChatCreate messageContent = new TdApi.MessageGroupChatCreate();
        GroupChatCreate groupChatCreate = new GroupChatCreate(messageContent);
        assertThat(groupChatCreate.getTitle()).isEqualTo("");
    }

}