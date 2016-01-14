package com.tg.osip.business.models.messages.contents;

import android.text.TextUtils;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;

/**
 * @author e.matsyuk
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class MessageContentPhotoItemTest {

    @Before
    public void setup() {
        PowerMockito.mockStatic(TextUtils.class);
        Mockito.when(TextUtils.isEmpty(any())).thenReturn(true);
    }

    @Test
    public void constructor_nullMessageContent() {
        MessageContentPhotoItem messageContentPhotoItem = new MessageContentPhotoItem(null);
        assertThat(messageContentPhotoItem.getPhotoItemMedium()).isNull();
        assertThat(messageContentPhotoItem.getPhotoItemLarge()).isNull();
    }

    @Test
    public void constructor_photoMY_correctExtending() {
        TdApi.MessagePhoto messageContent = new TdApi.MessagePhoto();
        TdApi.Photo photo = new TdApi.Photo();
        TdApi.PhotoSize photoSizeY = new TdApi.PhotoSize();
        photoSizeY.height = 700;
        photoSizeY.width = 700;
        photoSizeY.photo = new TdApi.File();
        photoSizeY.type = "y";
        TdApi.PhotoSize photoSizeX = new TdApi.PhotoSize();
        photoSizeX.height = 500;
        photoSizeX.width = 500;
        photoSizeX.photo = new TdApi.File();
        photoSizeX.type = "x";
        TdApi.PhotoSize photoSizeM = new TdApi.PhotoSize();
        photoSizeM.height = 300;
        photoSizeM.width = 300;
        photoSizeM.photo = new TdApi.File();
        photoSizeM.type = "m";
        TdApi.PhotoSize photoSizeS = new TdApi.PhotoSize();
        photoSizeS.height = 100;
        photoSizeS.width = 100;
        photoSizeS.photo = new TdApi.File();
        photoSizeS.type = "s";
        TdApi.PhotoSize[] photoSizes = new TdApi.PhotoSize[]{photoSizeY, photoSizeX, photoSizeM, photoSizeS};
        photo.photos = photoSizes;
        messageContent.photo = photo;

        MessageContentPhotoItem messageContentPhotoItem = new MessageContentPhotoItem(messageContent);

        assertThat(messageContentPhotoItem.getPhotoItemMedium().getHeight()).isEqualTo(300);
        assertThat(messageContentPhotoItem.getPhotoItemLarge().getHeight()).isEqualTo(700);
    }

}