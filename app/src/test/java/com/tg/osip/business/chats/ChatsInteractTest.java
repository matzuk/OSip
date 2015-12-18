package com.tg.osip.business.chats;

import com.tg.osip.tdclient.TGProxyI;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author e.matsyuk
 */
public class ChatsInteractTest {

    @Mock
    private TGProxyI tgProxy;
    @Mock
    private FileDownloaderManager fileDownloaderManager;

    private ChatsInteract chatsInteract;

    @Before
    public void setupChatsInteract() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
        // Get a reference to the class under test
        chatsInteract = new ChatsInteract(tgProxy, fileDownloaderManager);
    }


    @Test
    public void getNextDataPortionInList_emptyChats() throws Exception {
        TdApi.Chat[] chatsMas = new TdApi.Chat[0];
        TdApi.Chats chats = new TdApi.Chats(chatsMas);
        when(tgProxy.sendTD(any(TdApi.TLFunction.class), any())).thenReturn(Observable.just(chats));
        assertThat(chatsInteract.getNextDataPortionInList(0, 0).toBlocking().single()).isNotNull();


    }

    @Test
    public void getNextDataPortionInList_error() {
        Exception error = new RuntimeException();
        when(tgProxy.sendTD(any(TdApi.TLFunction.class), any())).thenReturn(Observable.error(error));
        try {
            chatsInteract.getNextDataPortionInList(0, 0).toBlocking().single();
        } catch (Exception expected) {
            assertThat(expected).isSameAs(error);
        }
    }

}