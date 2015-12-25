package com.tg.osip.business.models.messages;

import com.tg.osip.business.models.messages.contents.ChatAddParticipantItem;
import com.tg.osip.business.models.messages.contents.ChatChangePhotoItem;
import com.tg.osip.business.models.messages.contents.ChatChangeTitleItem;
import com.tg.osip.business.models.messages.contents.ChatDeleteParticipantItem;
import com.tg.osip.business.models.messages.contents.ChatDeletePhoto;
import com.tg.osip.business.models.messages.contents.ChatJoinByLink;
import com.tg.osip.business.models.messages.contents.GroupChatCreate;
import com.tg.osip.business.models.messages.contents.MessageContentPhotoItem;
import com.tg.osip.business.models.messages.contents.MessageContentTextItem;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author e.matsyuk
 */
public class MessageItemTest {

    @Before
    public void setupAddNotePresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void initMessageFields_nullMessage() {
        TdApi.Message message = null;
        MessageItem messageItem = new MessageItem();
        messageItem.initMessageFields(message);
        assertThat(messageItem.getId()).isEqualTo(0);
        assertThat(messageItem.getFromId()).isEqualTo(0);
        assertThat(messageItem.getChatId()).isEqualTo(0);
        assertThat(messageItem.getDate()).isEqualTo(0);
        assertThat(messageItem.getForwardFromId()).isEqualTo(0);
        assertThat(messageItem.getForwardDate()).isEqualTo(0);
    }
    @Test
    public void initMessageFields_message() {
        TdApi.Message message = new TdApi.Message();
        message.id = 5;
        message.fromId = 2;
        message.chatId = 1;
        message.date = 10000;
        message.forwardFromId = 8;
        message.forwardDate = 12000;
        MessageItem messageItem = new MessageItem();
        messageItem.initMessageFields(message);
        assertThat(messageItem.getId()).isEqualTo(5);
        assertThat(messageItem.getFromId()).isEqualTo(2);
        assertThat(messageItem.getChatId()).isEqualTo(1);
        assertThat(messageItem.getDate()).isEqualTo(10000);
        assertThat(messageItem.getForwardFromId()).isEqualTo(8);
        assertThat(messageItem.getForwardDate()).isEqualTo(12000);
    }

    @Test
    public void constructor_messageNull() {
        MessageItem messageItem = new MessageItem(null);
        assertThat(messageItem.getMessageContentItem()).isNull();
    }

    @Test
    public void constructor_messageContentNull() {
        TdApi.Message message = new TdApi.Message();
        message.message = null;
        MessageItem messageItem = new MessageItem(message);
        assertThat(messageItem.getMessageContentItem()).isNull();
    }

    @Test
    public void constructor_messageContentText() {
        TdApi.Message message = new TdApi.Message();
        message.message = new TdApi.MessageText("123");
        MessageItem messageItem = new MessageItem(message);
        assertThat(messageItem.getMessageContentItem().getClass()).isEqualTo(MessageContentTextItem.class);
        assertThat(((MessageContentTextItem)messageItem.getMessageContentItem()).getText()).isEqualTo("123");
    }

    @Test
    public void constructor_messageContentPhoto() {
        TdApi.Message message = new TdApi.Message();
        message.message = new TdApi.MessagePhoto();
        MessageItem messageItem = new MessageItem(message);
        assertThat(messageItem.getMessageContentItem().getClass()).isEqualTo(MessageContentPhotoItem.class);
    }

    @Test
    public void constructor_messageContentChatChangePhoto() {
        TdApi.Message message = new TdApi.Message();
        message.message = new TdApi.MessageChatChangePhoto();
        MessageItem messageItem = new MessageItem(message);
        assertThat(messageItem.getMessageContentItem().getClass()).isEqualTo(ChatChangePhotoItem.class);
    }

    @Test
    public void constructor_messageContentChatAddParticipiant() {
        TdApi.Message message = new TdApi.Message();
        message.message = new TdApi.MessageChatAddParticipant();
        MessageItem messageItem = new MessageItem(message);
        assertThat(messageItem.getMessageContentItem().getClass()).isEqualTo(ChatAddParticipantItem.class);
    }

    @Test
    public void constructor_messageContentChatChangeTitle() {
        TdApi.Message message = new TdApi.Message();
        message.message = new TdApi.MessageChatChangeTitle();
        MessageItem messageItem = new MessageItem(message);
        assertThat(messageItem.getMessageContentItem().getClass()).isEqualTo(ChatChangeTitleItem.class);
    }

    @Test
    public void constructor_messageContentChatDeleteParticipant() {
        TdApi.Message message = new TdApi.Message();
        message.message = new TdApi.MessageChatDeleteParticipant();
        MessageItem messageItem = new MessageItem(message);
        assertThat(messageItem.getMessageContentItem().getClass()).isEqualTo(ChatDeleteParticipantItem.class);
    }

    @Test
    public void constructor_messageContentChatDeletePhoto() {
        TdApi.Message message = new TdApi.Message();
        message.message = new TdApi.MessageChatDeletePhoto();
        MessageItem messageItem = new MessageItem(message);
        assertThat(messageItem.getMessageContentItem().getClass()).isEqualTo(ChatDeletePhoto.class);
    }

    @Test
    public void constructor_messageContentChatJoinByLink() {
        TdApi.Message message = new TdApi.Message();
        message.message = new TdApi.MessageChatJoinByLink();
        MessageItem messageItem = new MessageItem(message);
        assertThat(messageItem.getMessageContentItem().getClass()).isEqualTo(ChatJoinByLink.class);
    }

    @Test
    public void constructor_messageContentGroupChatCreate() {
        TdApi.Message message = new TdApi.Message();
        message.message = new TdApi.MessageGroupChatCreate();
        MessageItem messageItem = new MessageItem(message);
        assertThat(messageItem.getMessageContentItem().getClass()).isEqualTo(GroupChatCreate.class);
    }

    @Test
    public void constructor_messageContentUnsupported() {
        TdApi.Message message = new TdApi.Message();
        message.message = new TdApi.MessageAudio();
        MessageItem messageItem = new MessageItem(message);
        assertThat(messageItem.getMessageContentItem()).isNull();
    }

}