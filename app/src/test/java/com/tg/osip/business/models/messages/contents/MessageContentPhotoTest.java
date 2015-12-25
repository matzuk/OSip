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
public class MessageContentPhotoTest {

    @Before
    public void setup() {
        PowerMockito.mockStatic(TextUtils.class);
        Mockito.when(TextUtils.isEmpty(any())).thenReturn(true);
    }

    @Test
    public void init_nullPhoto() {
        MessageContentPhoto messageContentPhoto = new MessageContentPhoto() {
            @Override
            TdApi.Photo getPhoto(TdApi.MessageContent messageContent) {
                return null;
            }
        };
        assertThat(messageContentPhoto.getPhotoItemMedium()).isNull();
        assertThat(messageContentPhoto.getPhotoItemLarge()).isNull();
    }

    @Test
    public void init_nullPhotoSize() {
        MessageContentPhoto messageContentPhoto = new MessageContentPhoto() {
            @Override
            TdApi.Photo getPhoto(TdApi.MessageContent messageContent) {
                return new TdApi.Photo();
            }
        };
        assertThat(messageContentPhoto.getPhotoItemMedium()).isNull();
        assertThat(messageContentPhoto.getPhotoItemLarge()).isNull();
    }

    @Test
    public void init_photoMY() {
        MessageContentPhoto messageContentPhoto = new MessageContentPhoto() {
            @Override
            TdApi.Photo getPhoto(TdApi.MessageContent messageContent) {
                return null;
            }
        };

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

        messageContentPhoto.init(photo);
        assertThat(messageContentPhoto.getPhotoItemMedium().getHeight()).isEqualTo(300);
        assertThat(messageContentPhoto.getPhotoItemLarge().getHeight()).isEqualTo(700);
    }

    @Test
    public void init_photoSX() {
        MessageContentPhoto messageContentPhoto = new MessageContentPhoto() {
            @Override
            TdApi.Photo getPhoto(TdApi.MessageContent messageContent) {
                return null;
            }
        };

        TdApi.Photo photo = new TdApi.Photo();
        TdApi.PhotoSize photoSizeX = new TdApi.PhotoSize();
        photoSizeX.height = 500;
        photoSizeX.width = 500;
        photoSizeX.photo = new TdApi.File();
        photoSizeX.type = "x";
        TdApi.PhotoSize photoSizeS = new TdApi.PhotoSize();
        photoSizeS.height = 100;
        photoSizeS.width = 100;
        photoSizeS.photo = new TdApi.File();
        photoSizeS.type = "s";
        TdApi.PhotoSize[] photoSizes = new TdApi.PhotoSize[]{photoSizeX, photoSizeS};
        photo.photos = photoSizes;

        messageContentPhoto.init(photo);
        assertThat(messageContentPhoto.getPhotoItemMedium().getHeight()).isEqualTo(100);
        assertThat(messageContentPhoto.getPhotoItemLarge().getHeight()).isEqualTo(500);
    }

    @Test
    public void init_photoBX() {
        MessageContentPhoto messageContentPhoto = new MessageContentPhoto() {
            @Override
            TdApi.Photo getPhoto(TdApi.MessageContent messageContent) {
                return null;
            }
        };

        TdApi.Photo photo = new TdApi.Photo();
        TdApi.PhotoSize photoSizeX = new TdApi.PhotoSize();
        photoSizeX.height = 500;
        photoSizeX.width = 500;
        photoSizeX.photo = new TdApi.File();
        photoSizeX.type = "x";
        TdApi.PhotoSize photoSizeS = new TdApi.PhotoSize();
        photoSizeS.height = 100;
        photoSizeS.width = 100;
        photoSizeS.photo = new TdApi.File();
        photoSizeS.type = "b";
        TdApi.PhotoSize[] photoSizes = new TdApi.PhotoSize[]{photoSizeX, photoSizeS};
        photo.photos = photoSizes;

        messageContentPhoto.init(photo);
        assertThat(messageContentPhoto.getPhotoItemMedium().getHeight()).isEqualTo(100);
        assertThat(messageContentPhoto.getPhotoItemLarge().getHeight()).isEqualTo(500);
    }

}