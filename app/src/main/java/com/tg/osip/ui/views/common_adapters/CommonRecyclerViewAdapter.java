package com.tg.osip.ui.views.common_adapters;

import com.tg.osip.ui.views.auto_loading.AutoLoadingRecyclerViewAdapter;

import java.util.List;

/**
 * Common adapter for RecyclerView in Main and Chat Screens
 *
 * @author e.matsyuk
 */
public abstract class CommonRecyclerViewAdapter<T> extends AutoLoadingRecyclerViewAdapter<T> {

    // this variable for correct loader ProgressBar replacement to List items
    private boolean firstPortionLoaded;

    @Override
    public void addNewItems(List<T> items) {
        firstPortionLoaded = true;
        super.addNewItems(items);
    }

    protected boolean isFirstPortionLoaded() {
        return firstPortionLoaded;
    }

}
