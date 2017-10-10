/*
 * Copyright 2017 Phil Bayfield https://philio.me
 * Copyright 2017 Piwigo Team http://piwigo.org
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
            if (item.first.totalNbImages > item.first.nbImages) {
                int subPhotos = item.first.totalNbImages - item.first.nbImages;
                photos += resources.getQuantityString(R.plurals.album_photos_subs, subPhotos, subPhotos);
            }
            AlbumItemViewModel viewModel = new AlbumItemViewModel(item.second.derivatives.large.url, item.first.name, photos);
            viewHolder.getBinding().setVariable(BR.viewModel, viewModel);
        }
    }
}
