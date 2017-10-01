/*
 * Copyright 2016 Phil Bayfield https://philio.me
 * Copyright 2016 Piwigo Team http://piwigo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.piwigo.internal.binding.adapter;

import android.databinding.BindingAdapter;
import android.databinding.ObservableList;
import android.support.v7.widget.RecyclerView;

import org.piwigo.ui.shared.BindingRecyclerViewAdapter;

public class RecyclerViewAdapter {

    @BindingAdapter({"bind:items", "bind:viewBinder"}) public static <T> void bindRecyclerView(RecyclerView recyclerView, ObservableList items, BindingRecyclerViewAdapter.ViewBinder<T> viewBinder) {
        bindViewBinder(recyclerView, viewBinder);
        bindItems(recyclerView, items);
    }

    @BindingAdapter("bind:items") public static <T> void bindItems(RecyclerView recyclerView, ObservableList<T> items) {
        if (recyclerView.getAdapter() == null) {
            throw new IllegalStateException("RecyclerView doesn't have an adapter, did you bind a ViewBinder?");
        }
        BindingRecyclerViewAdapter<T> adapter = (BindingRecyclerViewAdapter<T>) recyclerView.getAdapter();
        adapter.update(items);
    }

    @BindingAdapter("bind:viewBinder") public static <T> void bindViewBinder(RecyclerView recyclerView, BindingRecyclerViewAdapter.ViewBinder<T> viewBinder) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null) {
            adapter = new BindingRecyclerViewAdapter<>(viewBinder);
            recyclerView.setAdapter(adapter);
        }
    }

}