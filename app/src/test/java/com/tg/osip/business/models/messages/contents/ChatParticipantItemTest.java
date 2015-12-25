package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author e.matsyuk
 */
public class ChatParticipantItemTest {

    @Test
    public void constructor_nullMessageContent() {
        ChatParticipantItem chatParticipantItem = new ChatParticipantItem(null) {
            @Override
            TdApi.User getUser(TdApi.MessageContent messageContent) {
                return null;
            }
        };
        assertThat(chatParticipantItem.getName()).isNotNull();
    }

    @Test
    public void initName_UserNullUserMessageContent() {
        TdApi.User user = new TdApi.User();

        ChatParticipantItem chatParticipantItem = new ChatParticipantItem() {
            @Override
            TdApi.User getUser(TdApi.MessageContent messageContent) {
                return null;
            }
        };
        chatParticipantItem.initName(user);
        assertThat(chatParticipantItem.getName()).isNotNull();
    }

    @Test
    public void initName_UserMessageContent() {
        TdApi.User user = new TdApi.User();
        user.firstName = "first";
        user.lastName = "last";

        ChatParticipantItem chatParticipantItem = new ChatParticipantItem() {
            @Override
            TdApi.User getUser(TdApi.MessageContent messageContent) {
                return null;
            }
        };
        chatParticipantItem.initName(user);
        assertThat(chatParticipantItem.getName()).isEqualTo("first last");
    }

    @Test
    public void initName_UserFirstNameMessageContent() {
        TdApi.User user = new TdApi.User();
        user.firstName = "first";

        ChatParticipantItem chatParticipantItem = new ChatParticipantItem() {
            @Override
            TdApi.User getUser(TdApi.MessageContent messageContent) {
                return null;
            }
        };
        chatParticipantItem.initName(user);
        assertThat(chatParticipantItem.getName()).isEqualTo("first");
    }

    @Test
    public void initName_UserLastNameMessageContent() {
        TdApi.User user = new TdApi.User();
        user.lastName = "last";

        ChatParticipantItem chatParticipantItem = new ChatParticipantItem() {
            @Override
            TdApi.User getUser(TdApi.MessageContent messageContent) {
                return null;
            }
        };
        chatParticipantItem.initName(user);
        assertThat(chatParticipantItem.getName()).isEqualTo("last");
    }

}