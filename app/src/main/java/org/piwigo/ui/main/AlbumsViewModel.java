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

package org.piwigo.ui.main;

import android.arch.lifecycle.ViewModel;
import android.content.res.Resources;
import android.databinding.ObservableArrayList;
import android.util.Log;
import android.util.Pair;

import org.piwigo.BR;
import org.piwigo.R;
import org.piwigo.io.model.Category;
import org.piwigo.io.model.ImageInfo;
import org.piwigo.io.repository.CategoriesRepository;
import org.piwigo.ui.shared.BindingRecyclerViewAdapter;

import java.util.List;

import rx.Subscriber;
import rx.Subscription;

public class AlbumsViewModel extends ViewModel {

    private static final String TAG = AlbumsViewModel.class.getName();

    public ObservableArrayList<Pair<Category, ImageInfo>> items = new ObservableArrayList<>();
    public BindingRecyclerViewAdapter.ViewBinder<Pair<Category, ImageInfo>> viewBinder = new CategoryViewBinder();

    private final CategoriesRepository categoriesRepository;
    private final Resources resources;

    private Subscription subscription;

    AlbumsViewModel(CategoriesRepository categoriesRepository, Resources resources) {
        this.categoriesRepository = categoriesRepository;
        this.resources = resources;
    }

    @Override protected void onCleared() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    void loadAlbums(Integer categoryId) {
        subscription = categoriesRepository.getCategories(categoryId)
                .subscribe(new CategoriesSubscriber());
    }

    private class CategoriesSubscriber extends Subscriber<List<Pair<Category, ImageInfo>>> {

        @Override public void onCompleted() {}

        @Override public void onError(Throwable e) {
            Log.e(TAG, e.getMessage());
        }

        @Override public void onNext(List<Pair<org.piwigo.io.model.Category, ImageInfo>> pairs) {
            items.clear();
            items.addAll(pairs);
        }
    }

    private class CategoryViewBinder implements BindingRecyclerViewAdapter.ViewBinder<Pair<Category, ImageInfo>> {

        @Override public int getViewType(Pair<Category, ImageInfo> item) {
            return 0;
        }

        @Override public int getLayout(int viewType) {
            return R.layout.item_album;
        }

        @Override public void bind(BindingRecyclerViewAdapter.ViewHolder viewHolder, Pair<Category, ImageInfo> item) {
            String photos = resources.getQuantityString(R.plurals.album_photos, item.first.nbImages, item.first.nbImages);
            String url = null;
            if (item.first.totalNbImages > item.first.nbImages) {
                int subPhotos = item.first.totalNbImages - item.first.nbImages;
                photos += resources.getQuantityString(R.plurals.album_photos_subs, subPhotos, subPhotos);
            }
            if(item.second != null) {
                url = item.second.derivatives.large.url;
            }
            AlbumItemViewModel viewModel = new AlbumItemViewModel(url, item.first.name, photos);
            viewHolder.getBinding().setVariable(BR.viewModel, viewModel);
        }
    }
}
