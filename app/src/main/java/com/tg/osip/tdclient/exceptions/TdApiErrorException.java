package com.tg.osip.tdclient.exceptions;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Exception from {@link org.drinkless.td.libcore.telegram.TdApi.Error TdApi.Error}
 *
 * @author e.matsyuk
 */
public class TdApiErrorException extends RuntimeException {

    private int code;
    private String text;

    public TdApiErrorException(TdApi.Error error) {
        super("TdApiErrorException{" + "code = " + String.valueOf(error.code) + ", text = " + error.text + '}');
        this.code = error.code;
        this.text = error.text;
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }
}
