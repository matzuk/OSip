package com.tg.osip.business.models.messages.contents;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class ChatAddParticipantItem extends ChatParticipantItem<TdApi.MessageChatAddParticipant> {

    public ChatAddParticipantItem(TdApi.MessageChatAddParticipant messageContent) {
        super(messageContent);
    }

    @Override
    TdApi.User getUser(TdApi.MessageChatAddParticipant messageContent) {
        if (messageContent == null ) {
             return null;
        }
        return messageContent.user;
    }

}
