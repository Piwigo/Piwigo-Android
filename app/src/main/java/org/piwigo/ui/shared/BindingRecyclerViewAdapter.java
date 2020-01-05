/*
 * Piwigo for Android
 * Copyright (C) 2016-2018 Piwigo Team http://piwigo.org
 * Copyright (C) 2018-2018 Raphael Mack http://www.raphael-mack.de
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

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class BindingRecyclerViewAdapter<T> extends RecyclerView.Adapter<BindingRecyclerViewAdapter.ViewHolder> {

    private final ViewBinder<T> viewBinder;
    private List<T> items = new ArrayList<T>();

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
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ItemDiffCB(this.items, items));
        diffResult.dispatchUpdatesTo(this);

        // remember as old list...
        this.items.clear();
        this.items.addAll(items);
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

    public class ItemDiffCB extends DiffUtil.Callback{
        private final List<T> newItems;
        private final List<T> oldItems;

        public ItemDiffCB(List<T>newItems, List<T> oldItems){
            this.newItems = newItems;
            this.oldItems = oldItems;
        }

        @Override
        public int getOldListSize() {
            return oldItems.size();
        }

        @Override
        public int getNewListSize() {
            return newItems.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldItems.get(oldItemPosition) == newItems.get(newItemPosition);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            T o = oldItems.get(oldItemPosition);
            T n = newItems.get(newItemPosition);
            if(o == n){
                return true;
            }
            if (o == null || n == null){
                return false;
            }
            return oldItems.get(oldItemPosition).equals(newItems.get(newItemPosition));
        }
    }

}
