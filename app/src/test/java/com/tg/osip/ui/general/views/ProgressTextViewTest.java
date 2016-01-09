package com.tg.osip.ui.general.views;

import android.app.Activity;
import android.support.v4.util.Pair;

import com.tg.osip.RobolectricUnitTestRunner;
import com.tg.osip.tdclient.update_managers.FileDownloaderI;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;

import rx.subjects.PublishSubject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * @author e.matsyuk
 */
@RunWith(RobolectricUnitTestRunner.class)
public class ProgressTextViewTest {

    @Mock
    FileDownloaderManager fileDownloaderManager;

    PublishSubject<Pair<Integer, Integer>> downloadProgressChannel;
    PublishSubject<Integer> downloadCancelChannel;
    Activity activity;

    @Before
    public void beforeTest() {
        // Mockito
        MockitoAnnotations.initMocks(this);
        // Robolectic
        activity = Robolectric.setupActivity(Activity.class);
        downloadProgressChannel = PublishSubject.create();
        downloadCancelChannel = PublishSubject.create();
        when(fileDownloaderManager.getDownloadProgressChannel()).thenReturn(downloadProgressChannel);
        when(fileDownloaderManager.getDownloadCancelChannel()).thenReturn(downloadCancelChannel);
    }

    @Test
    public void init_FileDownloaded() {
        ProgressTextView progressTextView = new ProgressTextView(activity);

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "123";
            }

            @Override
            public String getFilePath() {
                return "123";
            }

            @Override
            public int getFileId() {
                return 10;
            }
        };
        // setDownloadingInfo
        progressTextView.setDownloadingInfo(fileDownloaderI, "defaultText", 10);
        // assert
        assertThat(progressTextView.getText()).isEqualTo("defaultText");
        assertThat(progressTextView.downloadCancelChannelSubscription).isNull();
        assertThat(progressTextView.downloadProgressChannelSubscription).isNull();
    }

    @Test
    public void init_FileDownloadedInCache() {
        ProgressTextView progressTextView = new ProgressTextView(activity);

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "";
            }

            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 10;
            }
        };
        when(fileDownloaderManager.isFileInCache(10)).thenReturn(true);
        // setFileDownloaderManager
        progressTextView.setFileDownloaderManager(fileDownloaderManager);
        // setDownloadingInfo
        progressTextView.setDownloadingInfo(fileDownloaderI, "defaultText", 10);
        // assert
        assertThat(progressTextView.getText()).isEqualTo("defaultText");
        assertThat(progressTextView.downloadCancelChannelSubscription).isNull();
        assertThat(progressTextView.downloadProgressChannelSubscription).isNull();
    }

    @Test
    public void init_FileInProgress() {
        ProgressTextView progressTextView = new ProgressTextView(activity);

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "";
            }

            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 20;
            }
        };
        when(fileDownloaderManager.isFileInProgress(20)).thenReturn(true);
        when(fileDownloaderManager.getProgressValue(20)).thenReturn(20);
        // setFileDownloaderManager
        progressTextView.setFileDownloaderManager(fileDownloaderManager);
        // setDownloadingInfo
        progressTextView.setDownloadingInfo(fileDownloaderI, "defaultText", 10);
        // assert
        assertThat(progressTextView.getText()).isEqualTo("Downloaded 2 of 10");
        assertThat(progressTextView.downloadCancelChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressTextView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
    }

    @Test
    public void init_FileNotDownloaded() {
        ProgressTextView progressTextView = new ProgressTextView(activity);

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "";
            }

            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 30;
            }
        };
        // setFileDownloaderManager
        progressTextView.setFileDownloaderManager(fileDownloaderManager);
        // setDownloadingInfo
        progressTextView.setDownloadingInfo(fileDownloaderI, "defaultText", 10);
        // assert
        assertThat(progressTextView.getText()).isEqualTo("defaultText");
        assertThat(progressTextView.downloadCancelChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressTextView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
    }

    @Test
    public void cycle_FileNotDownloaded() {
        ProgressTextView progressTextView = new ProgressTextView(activity);

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "";
            }

            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 30;
            }
        };
        // setFileDownloaderManager
        progressTextView.setFileDownloaderManager(fileDownloaderManager);
        // setDownloadingInfo
        progressTextView.setDownloadingInfo(fileDownloaderI, "defaultText", 10);
        // assert
        assertThat(progressTextView.getText()).isEqualTo("defaultText");
        assertThat(progressTextView.downloadCancelChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressTextView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        // progress
        fileDownloaderManager.getDownloadProgressChannel().onNext(new Pair<>(30, 20));
        assertThat(progressTextView.getText()).isEqualTo("Downloaded 2 of 10");
        fileDownloaderManager.getDownloadProgressChannel().onNext(new Pair<>(30, 40));
        assertThat(progressTextView.getText()).isEqualTo("Downloaded 4 of 10");
        fileDownloaderManager.getDownloadProgressChannel().onNext(new Pair<>(30, 60));
        assertThat(progressTextView.getText()).isEqualTo("Downloaded 6 of 10");
        // finish
        fileDownloaderManager.getDownloadProgressChannel().onNext(new Pair<>(30, 100));
        assertThat(progressTextView.getText()).isEqualTo("defaultText");
        assertThat(progressTextView.downloadCancelChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressTextView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
    }

    @Test
    public void cycleWithCancel_FileNotDownloaded() {
        ProgressTextView progressTextView = new ProgressTextView(activity);

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "";
            }

            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 30;
            }
        };
        // setFileDownloaderManager
        progressTextView.setFileDownloaderManager(fileDownloaderManager);
        // setDownloadingInfo
        progressTextView.setDownloadingInfo(fileDownloaderI, "defaultText", 10);
        // assert
        assertThat(progressTextView.getText()).isEqualTo("defaultText");
        assertThat(progressTextView.downloadCancelChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressTextView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        // progress
        fileDownloaderManager.getDownloadProgressChannel().onNext(new Pair<>(30, 20));
        assertThat(progressTextView.getText()).isEqualTo("Downloaded 2 of 10");
        // cancel
        fileDownloaderManager.getDownloadCancelChannel().onNext(30);
        assertThat(progressTextView.getText()).isEqualTo("defaultText");
        assertThat(progressTextView.downloadCancelChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressTextView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        // retain progress
        fileDownloaderManager.getDownloadProgressChannel().onNext(new Pair<>(30, 60));
        assertThat(progressTextView.getText()).isEqualTo("Downloaded 6 of 10");
        // finish
        fileDownloaderManager.getDownloadProgressChannel().onNext(new Pair<>(30, 100));
        assertThat(progressTextView.getText()).isEqualTo("defaultText");
        assertThat(progressTextView.downloadCancelChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressTextView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
    }

    @Test
    public void cycleWithReSetDownloadingInfo_FileNotDownloaded() {
        ProgressTextView progressTextView = new ProgressTextView(activity);

        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "";
            }

            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 30;
            }
        };
        // setFileDownloaderManager
        progressTextView.setFileDownloaderManager(fileDownloaderManager);
        // setDownloadingInfo
        progressTextView.setDownloadingInfo(fileDownloaderI, "defaultText", 10);
        // assert
        assertThat(progressTextView.getText()).isEqualTo("defaultText");
        assertThat(progressTextView.downloadCancelChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressTextView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        // progress
        fileDownloaderManager.getDownloadProgressChannel().onNext(new Pair<>(30, 20));
        assertThat(progressTextView.getText()).isEqualTo("Downloaded 2 of 10");
        // new DownloadingInfo 40
        FileDownloaderI fileDownloaderI2 = new FileDownloaderI() {
            @Override
            public String getTGFilePath() {
                return "";
            }

            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 40;
            }
        };
        progressTextView.setDownloadingInfo(fileDownloaderI2, "defaultText2", 20);
        // assert
        assertThat(progressTextView.getText()).isEqualTo("defaultText2");
        assertThat(progressTextView.downloadCancelChannelSubscription.isUnsubscribed()).isEqualTo(false);
        assertThat(progressTextView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(false);
        // old progress
        fileDownloaderManager.getDownloadProgressChannel().onNext(new Pair<>(30, 20));
        assertThat(progressTextView.getText()).isEqualTo("defaultText2");
        // new progress
        fileDownloaderManager.getDownloadProgressChannel().onNext(new Pair<>(40, 20));
        assertThat(progressTextView.getText()).isEqualTo("Downloaded 4 of 20");
        // finish
        fileDownloaderManager.getDownloadProgressChannel().onNext(new Pair<>(40, 100));
        assertThat(progressTextView.getText()).isEqualTo("defaultText2");
        assertThat(progressTextView.downloadCancelChannelSubscription.isUnsubscribed()).isEqualTo(true);
        assertThat(progressTextView.downloadProgressChannelSubscription.isUnsubscribed()).isEqualTo(true);
    }

}