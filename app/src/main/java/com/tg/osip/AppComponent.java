package com.tg.osip;

import android.support.annotation.NonNull;

import com.tg.osip.business.AuthManager;
import com.tg.osip.business.chats.ChatsInteract;
import com.tg.osip.business.main.MainInteract;
import com.tg.osip.business.messages.MessagesInteract;
import com.tg.osip.business.update_managers.FileDownloaderManager;
import com.tg.osip.tdclient.TGModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Component for all application
 * @author e.matsyuk
 */
@Component(modules = {AppModule.class, TGModule.class})
@Singleton
public interface AppComponent {
    void inject(@NonNull ApplicationSIP applicationSIP);
    void inject(@NonNull AuthManager authManager);
    void inject(@NonNull ChatsInteract chatsInteract);
    void inject(@NonNull FileDownloaderManager fileDownloaderManager);
    void inject(@NonNull MainInteract mainInteract);
    void inject(@NonNull MessagesInteract messagesInteract);
}
