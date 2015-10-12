package com.tg.osip.utils.ui.auto_loading;

/**
 *
 */
public class AutoLoadingRecyclerViewExceptions extends RuntimeException {

    public AutoLoadingRecyclerViewExceptions() {
        super("Exception in AutoLoadedRecyclerView");
    }

    public AutoLoadingRecyclerViewExceptions(String detailMessage) {
        super(detailMessage);
    }
}
