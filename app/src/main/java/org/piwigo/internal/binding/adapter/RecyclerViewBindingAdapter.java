/*
 * Piwigo for Android
 * Copyright (C) 2016-2017 Piwigo Team http://piwigo.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.piwigo.internal.binding.adapter;

import android.databinding.BindingAdapter;
import android.databinding.ObservableList;
import android.support.v7.widget.RecyclerView;

import org.piwigo.ui.shared.BindingRecyclerViewAdapter;

public class RecyclerViewBindingAdapter {

    @BindingAdapter(value = {"items", "viewBinder"}, requireAll = false) public static <T> void bindRecyclerView(RecyclerView recyclerView, ObservableList items, BindingRecyclerViewAdapter.ViewBinder<T> viewBinder) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();

        if (viewBinder != null) {
            if (adapter == null) {
                adapter = new BindingRecyclerViewAdapter<>(viewBinder);
                recyclerView.setAdapter(adapter);
            }
        }

        if (items != null && adapter != null) {
            BindingRecyclerViewAdapter<T> bindingRecyclerViewAdapter = (BindingRecyclerViewAdapter<T>) adapter;
            bindingRecyclerViewAdapter.update(items);
        }
    }
}