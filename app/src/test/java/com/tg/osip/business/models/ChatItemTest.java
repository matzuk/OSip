package com.tg.osip.business.models;

import com.tg.osip.business.chats.ChatsInteract;
import com.tg.osip.business.models.ChatItem;
import com.tg.osip.tdclient.TGProxyImpl;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
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

    private ChatItem chatItem;

    @Before
    public void setupAddNotePresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
        // Get a reference to the class under test
        chatItem = new ChatItem();
    }

}
