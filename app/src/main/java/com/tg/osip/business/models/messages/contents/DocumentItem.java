package com.tg.osip.business.models.messages.contents;

import android.text.format.Formatter;

import com.tg.osip.ApplicationSIP;
import com.tg.osip.tdclient.update_managers.FileDownloaderI;
import com.tg.osip.utils.CommonStaticFields;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * DocumentItem without PhotoSize processing
 *
 * @author e.matsyuk
 */
public class DocumentItem extends MessageContentItem implements FileDownloaderI {

    private String fileName;
    private String mimeType;
    private int documentFileId;
    private String documentFilePath;
    private String documentTGFilePath;
    private int documentFileSize;
    private String documentFileSizeString;

    public DocumentItem(TdApi.MessageDocument messageDocument) {
        if (messageDocument == null || messageDocument.document == null) {
            return;
        }
        initFields(messageDocument.document);
    }

    private void initFields(TdApi.Document document) {
        fileName = document.fileName;
        mimeType = document.mimeType;
        if (document.document != null) {
            documentFileId = document.document.id;
            if (document.document.path != null && !document.document.path.equals(CommonStaticFields.EMPTY_STRING)) {
                documentFilePath = document.document.path;
                documentTGFilePath = CommonStaticFields.ADD_TO_PATH + documentFilePath;
            }
            documentFileSize = document.document.size;
            documentFileSizeString = Formatter.formatFileSize(ApplicationSIP.applicationContext, documentFileSize);
        }
    }

    @Override
    public String getTGFilePath() {
        if (documentTGFilePath == null) {
            return CommonStaticFields.EMPTY_STRING;
        }
        return documentTGFilePath;
    }

    @Override
    public String getFilePath() {
        if (documentFilePath == null) {
            return CommonStaticFields.EMPTY_STRING;
        }
        return documentFilePath;
    }

    @Override
    public int getFileId() {
        return documentFileId;
    }

    public String getFileName() {
        if (fileName == null) {
            return CommonStaticFields.EMPTY_STRING;
        }
        return fileName;
    }

    public String getMimeType() {
        if (mimeType == null) {
            return CommonStaticFields.EMPTY_STRING;
        }
        return mimeType;
    }

    public int getDocumentFileSize() {
        return documentFileSize;
    }

    public String getDocumentFileSizeString() {
        if (documentFileSizeString == null) {
            return CommonStaticFields.EMPTY_STRING;
        }
        return documentFileSizeString;
    }

}
