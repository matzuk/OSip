/**
 * Copyright 2015 Eugene Matsyuk (matzuk2@mail.ru)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package com.tg.osip.ui.general.views.auto_loading;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for {@link AutoLoadingRecyclerView AutoLoadingRecyclerView}
 *
 * @author e.matsyuk
 */
public abstract class AutoLoadingRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected List<T> listElements = new ArrayList<>();

    public void addNewItems(List<T> items) {
        listElements.addAll(items);
    }

    public void addNewItem(T item) {
        listElements.add(item);
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
