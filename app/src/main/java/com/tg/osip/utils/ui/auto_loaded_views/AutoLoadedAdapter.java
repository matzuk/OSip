package com.tg.osip.utils.ui.auto_loaded_views;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter for {@link AutoLoadedRecyclerView AutoLoadedRecyclerView}
 *
 * @author e.matsyuk
 */
public abstract class AutoLoadedAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<T> listElements = new ArrayList<>();

    public void addNewItems(List<T> items) {
        listElements.addAll(items);
    }

    public List<T> getItems() {
        return listElements;
    }

    public T getItem(int position) {
        return listElements.get(position);
    }

    @Override
    public int getItemCount() {
        return listElements.size();
    }
}
