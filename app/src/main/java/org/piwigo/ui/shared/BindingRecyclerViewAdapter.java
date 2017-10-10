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

package org.piwigo.ui.shared;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class BindingRecyclerViewAdapter<T> extends RecyclerView.Adapter<BindingRecyclerViewAdapter.ViewHolder> {

    private final ViewBinder<T> viewBinder;
    private List<T> items = new ArrayList<>();

    public BindingRecyclerViewAdapter(ViewBinder<T> viewBinder) {
        this.viewBinder = viewBinder;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), viewBinder.getLayout(viewType), parent, false);
        return new ViewHolder(binding);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        viewBinder.bind(holder, items.get(position));
    }

    @Override public int getItemCount() {
        return items.size();
    }

    @Override public int getItemViewType(int position) {
        return viewBinder.getViewType(items.get(position));
    }

    public void update(List<T> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ViewDataBinding binding;

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ViewDataBinding getBinding() {
            return binding;
        }

    }

    public interface ViewBinder<T> {

        int getViewType(T item);

        int getLayout(int viewType);

        void bind(ViewHolder viewHolder, T item);

    }

}
