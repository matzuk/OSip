package com.tg.osip.business.models.messages;

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
    public void initMessageType_nullMessage() {
        TdApi.Message message = null;
        MessageItem messageItem = new MessageItem();
        messageItem.initMessageType(message);
        assertThat(messageItem.getContentType()).isEqualTo(MessageItem.ContentType.NULL_TYPE);
    }

    @Test
    public void initMessageType_nullMessageContent() {
        TdApi.Message message = new TdApi.Message();
        message.message = null;
        MessageItem messageItem = new MessageItem();
        messageItem.initMessageType(message);
        assertThat(messageItem.getContentType()).isEqualTo(MessageItem.ContentType.NULL_TYPE);
    }

    @Test
    public void initMessageType_messageContentText() {
        TdApi.Message message = new TdApi.Message();
        message.message = new TdApi.MessageText();
        MessageItem messageItem = new MessageItem();
        messageItem.initMessageType(message);
        assertThat(messageItem.getContentType()).isEqualTo(MessageItem.ContentType.TEXT_MESSAGE_TYPE);
    }

    @Test
     public void initMessageType_messageContentUnsupported() {
        TdApi.Message message = new TdApi.Message();
        message.message = new TdApi.MessageAudio();
        MessageItem messageItem = new MessageItem();
        messageItem.initMessageType(message);
        assertThat(messageItem.getContentType()).isEqualTo(MessageItem.ContentType.UNSUPPORTED_TYPE);
    }

    @Test
    public void initMessageContent_messageContentNull() {
        TdApi.Message message = new TdApi.Message();
        message.message = null;
        MessageItem messageItem = new MessageItem();
        messageItem.initMessageContent(message);
        assertThat(messageItem.getMessageContentItem()).isNull();
    }

    @Test
    public void initMessageContentInConstructor_messageContentText() {
        TdApi.Message message = new TdApi.Message();
        message.message = new TdApi.MessageText("123");
        MessageItem messageItem = new MessageItem(message);
        assertThat(messageItem.getMessageContentItem().getClass()).isEqualTo(MessageContentTextItem.class);
        assertThat(((MessageContentTextItem)messageItem.getMessageContentItem()).getText()).isEqualTo("123");
    }

    @Test
    public void initMessageContentInConstructor_messageContentUnsupported() {
        TdApi.Message message = new TdApi.Message();
        message.message = new TdApi.MessageAudio();
        MessageItem messageItem = new MessageItem(message);
        assertThat(messageItem.getMessageContentItem()).isNull();
    }

}