package com.tg.osip.utils.ui.auto_loaded_views;

/**
 *
 */
public class AutoLoadedRecyclerViewExceptions extends RuntimeException {

    public AutoLoadedRecyclerViewExceptions() {
        super("Exception in AutoLoadedRecyclerView");
    }

    public AutoLoadedRecyclerViewExceptions(String detailMessage) {
        super(detailMessage);
    }
}
