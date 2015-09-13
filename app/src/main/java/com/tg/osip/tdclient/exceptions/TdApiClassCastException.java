package com.tg.osip.tdclient.exceptions;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Exception when response {@link org.drinkless.td.libcore.telegram.TdApi.TLObject TdApi.TLObject} class is not correct
 *
 * @author e.matsyuk
 */
public class TdApiClassCastException extends RuntimeException {

    public TdApiClassCastException(ClassCastException classCastException) {
        super("TdApiClassCastException{" + "message = " + classCastException.getMessage() + '}');
    }

}
