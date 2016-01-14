package com.tg.osip.business.models;

import android.content.Context;
import android.content.res.Resources;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for
 *
 * @author e.matsyuk
 */
public class ChatItemTest {

    @Mock
    private TdApi.Chat chat;
    @Mock
    private TdApi.Message message;
    @Mock
    private Context context;

    private ChatItem chatItem;

    @Before
    public void setupAddNotePresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        chatItem = new ChatItem();
    }

    @Test
    public void initChatLastMessage_null() {
        TdApi.MessageContent messageContent = null;
        chatItem.initChatLastMessage(context, messageContent);
        assertThat(chatItem.getLastMessageText()).isNotNull();
    }

    @Test
    public void initChatLastMessage_textType() {
        TdApi.MessageContent messageContent = new TdApi.MessageText("message");
        chatItem.initChatLastMessage(context, messageContent);
        assertThat(chatItem.getLastMessageText()).isEqualTo("message");
    }

    @Test
    public void initChatLastMessage_otherType() {
        Resources resources = mock(Resources.class);
        when(resources.getString(anyInt())).thenReturn("audio");
        Context context2 = mock(Context.class);
        when(context2.getResources()).thenReturn(resources);

        TdApi.MessageContent messageContent = new TdApi.MessageAudio();
        chatItem.initChatLastMessage(context2, messageContent);
        assertThat(chatItem.getLastMessageText()).isEqualTo("audio");
    }

}
