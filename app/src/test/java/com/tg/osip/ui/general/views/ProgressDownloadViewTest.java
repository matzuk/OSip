package com.tg.osip.ui.general.views;

import com.tg.osip.tdclient.update_managers.FileDownloaderI;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.ui.general.views.progress_download.ProgressDownloadView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Unit tests
 *
 * @author e.matsyuk
 */
public class ProgressDownloadViewTest {

    @Mock
    FileDownloaderManager fileDownloaderManager;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void setFileDownloaderI_emptyIdFileDownloaderI() throws Exception {
        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getFilePath() {
                return "";
            }

            @Override
            public int getFileId() {
                return 0;
            }
        };
        when(fileDownloaderManager.isFileInCache(anyInt())).thenReturn(false);
        when(fileDownloaderManager.isFileInProgress(anyInt())).thenReturn(false);
        ProgressDownloadView progressDownloadView = new ProgressDownloadView();
        progressDownloadView.setFileDownloaderManager(fileDownloaderManager);

        assertThat(progressDownloadView.getDownloadingState(fileDownloaderI)).isEqualTo(ProgressDownloadView.DownloadingState.START);
    }

    @Test
    public void getDownloadingState_emptyPathFileDownloaderI() throws Exception {
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
        when(fileDownloaderManager.isFileInProgress(anyInt())).thenReturn(false);
        ProgressDownloadView progressDownloadView = new ProgressDownloadView();
        progressDownloadView.setFileDownloaderManager(fileDownloaderManager);

        assertThat(progressDownloadView.getDownloadingState(fileDownloaderI)).isEqualTo(ProgressDownloadView.DownloadingState.START);
    }

    @Test
    public void getDownloadingState_readyFileDownloaderI() throws Exception {
        FileDownloaderI fileDownloaderI = new FileDownloaderI() {
            @Override
            public String getFilePath() {
                return "123";
            }

            @Override
            public int getFileId() {
                return 10;
            }
        };
        when(fileDownloaderManager.isFileInCache(anyInt())).thenReturn(false);
        when(fileDownloaderManager.isFileInProgress(anyInt())).thenReturn(false);
        ProgressDownloadView progressDownloadView = new ProgressDownloadView();
        progressDownloadView.setFileDownloaderManager(fileDownloaderManager);

        assertThat(progressDownloadView.getDownloadingState(fileDownloaderI)).isEqualTo(ProgressDownloadView.DownloadingState.PLAY);
    }

    @Test
    public void getDownloadingState_inCacheFileDownloaderI() throws Exception {
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
        when(fileDownloaderManager.isFileInCache(10)).thenReturn(true);
        when(fileDownloaderManager.isFileInProgress(anyInt())).thenReturn(false);
        ProgressDownloadView progressDownloadView = new ProgressDownloadView();
        progressDownloadView.setFileDownloaderManager(fileDownloaderManager);

        assertThat(progressDownloadView.getDownloadingState(fileDownloaderI)).isEqualTo(ProgressDownloadView.DownloadingState.PLAY);
    }

    @Test
    public void getDownloadingState_inProgressFileDownloaderI() throws Exception {
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
        ProgressDownloadView progressDownloadView = new ProgressDownloadView();
        progressDownloadView.setFileDownloaderManager(fileDownloaderManager);

        assertThat(progressDownloadView.getDownloadingState(fileDownloaderI)).isEqualTo(ProgressDownloadView.DownloadingState.DOWNLOADING);
    }

    @Test
    public void progressUpdatingTest() {

    }

}