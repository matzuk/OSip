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
 * Unit tests with Robolectric
 *
 * @author e.matsyuk
 */
@RunWith(RobolectricUnitTestRunner.class)
public class ProgressDownloadViewTest2 {

    @Mock
    FileDownloaderManager fileDownloaderManager;

    ProgressDownloadView progressDownloadView;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
        // Robolectic
        Activity activity = Robolectric.setupActivity(Activity.class);

        progressDownloadView = new ProgressDownloadView(activity);
    }

    @Test
    public void progressUpdatingTest_downloadingState() {
        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 10;
            }
        };
        when(fileDownloaderManager.isFileInCache(anyInt())).thenReturn(false);
        when(fileDownloaderManager.isFileInProgress(10)).thenReturn(true);
        when(fileDownloaderManager.getProgressValue(10)).thenReturn(20);

        PublishSubject<Pair<Integer, Integer>> downloadProgressChannel = PublishSubject.create();
        when(fileDownloaderManager.getDownloadProgressChannel()).thenReturn(downloadProgressChannel);

        progressDownloadView.setFileDownloaderManager(fileDownloaderManager);
        progressDownloadView.setFileDownloaderI(fileDownloaderI);

        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(20);

        downloadProgressChannel.onNext(new Pair<>(10, 40));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);

        downloadProgressChannel.onNext(new Pair<>(20, 60));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);

        downloadProgressChannel.onNext(new Pair<>(10, 85));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(85);

        downloadProgressChannel.onNext(new Pair<>(10, 100));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(100);
        assertThat(progressDownloadView.downloadingState).isEqualTo(ProgressDownloadView.DownloadingState.READY);
    }

    @Test
    public void progressUpdatingTest_startState() {
        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 10;
            }
        };
        when(fileDownloaderManager.isFileInCache(anyInt())).thenReturn(false);
        when(fileDownloaderManager.isFileInProgress(10)).thenReturn(false);

        PublishSubject<Pair<Integer, Integer>> downloadProgressChannel = PublishSubject.create();
        when(fileDownloaderManager.getDownloadProgressChannel()).thenReturn(downloadProgressChannel);

        progressDownloadView.setFileDownloaderManager(fileDownloaderManager);
        progressDownloadView.setFileDownloaderI(fileDownloaderI);
        progressDownloadView.callOnClick();

        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(0);

        downloadProgressChannel.onNext(new Pair<>(10, 40));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);

        downloadProgressChannel.onNext(new Pair<>(20, 60));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);

        downloadProgressChannel.onNext(new Pair<>(10, 85));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(85);

        downloadProgressChannel.onNext(new Pair<>(10, 100));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(100);
        assertThat(progressDownloadView.downloadingState).isEqualTo(ProgressDownloadView.DownloadingState.READY);
    }

    @Test
    public void progressUpdatingTest_someStates() {
        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 10;
            }
        };
        when(fileDownloaderManager.isFileInCache(anyInt())).thenReturn(false);
        when(fileDownloaderManager.isFileInProgress(10)).thenReturn(true);
        when(fileDownloaderManager.getProgressValue(10)).thenReturn(20);

        PublishSubject<Pair<Integer, Integer>> downloadProgressChannel = PublishSubject.create();
        when(fileDownloaderManager.getDownloadProgressChannel()).thenReturn(downloadProgressChannel);

        progressDownloadView.setFileDownloaderManager(fileDownloaderManager);
        progressDownloadView.setFileDownloaderI(fileDownloaderI);
        progressDownloadView.callOnClick();

        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(20);

        downloadProgressChannel.onNext(new Pair<>(10, 40));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(40);

        FileDownloaderI fileDownloaderI2 = new FileDownloaderI() {
            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 20;
            }
        };
        progressDownloadView.setFileDownloaderI(fileDownloaderI2);
        progressDownloadView.callOnClick();

        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(0);

        downloadProgressChannel.onNext(new Pair<>(20, 100));
        assertThat(progressDownloadView.progressBar.getProgress()).isEqualTo(100);
        assertThat(progressDownloadView.downloadingState).isEqualTo(ProgressDownloadView.DownloadingState.READY);
    }

}