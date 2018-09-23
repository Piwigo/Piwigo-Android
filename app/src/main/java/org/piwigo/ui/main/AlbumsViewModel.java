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

import android.accounts.Account;
import android.arch.lifecycle.ViewModel;
import android.content.res.Resources;
import android.databinding.ObservableArrayList;
import android.util.Log;

import com.google.common.base.Optional;

import org.piwigo.BR;
import org.piwigo.R;
import org.piwigo.accounts.UserManager;
import org.piwigo.io.model.Category;
import org.piwigo.io.repository.CategoriesRepository;
import org.piwigo.ui.shared.BindingRecyclerViewAdapter;

import java.util.List;

import rx.Subscriber;
import rx.Subscription;

public class AlbumsViewModel extends ViewModel {

    private static final String TAG = AlbumsViewModel.class.getName();

    public ObservableArrayList<Category> items = new ObservableArrayList<>();
    public BindingRecyclerViewAdapter.ViewBinder<Category> viewBinder = new CategoryViewBinder();

    private final UserManager userManager;
    private final CategoriesRepository categoriesRepository;
    private final Resources resources;

    private Subscription subscription;

    AlbumsViewModel(UserManager userManager, CategoriesRepository categoriesRepository, Resources resources) {
        this.userManager = userManager;
        this.categoriesRepository = categoriesRepository;
        this.resources = resources;
    }

    @Override protected void onCleared() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    void loadAlbums(Integer categoryId) {
        Optional<Account> account = userManager.getActiveAccount();
        if (account.isPresent()) {
            subscription = categoriesRepository.getCategories(account.get(), categoryId)
                    .subscribe(new CategoriesSubscriber());
        }
    }

    private class CategoriesSubscriber extends Subscriber<List<Category>> {

        @Override public void onCompleted() {}

        @Override public void onError(Throwable e) {
            Log.e(TAG, e.getMessage());
        }

        @Override public void onNext(List<Category> categories) {
            items.clear();
            items.addAll(categories);
        }
    }

    private class CategoryViewBinder implements BindingRecyclerViewAdapter.ViewBinder<Category> {

        @Override public int getViewType(Category category) {
            return 0;
        }

        @Override public int getLayout(int viewType) {
            return R.layout.item_album;
        }

        @Override public void bind(BindingRecyclerViewAdapter.ViewHolder viewHolder, Category category) {
            String photos = resources.getQuantityString(R.plurals.album_photos, category.nbImages, category.nbImages);
            if (category.totalNbImages > category.nbImages) {
                int subPhotos = category.totalNbImages - category.nbImages;
                photos += resources.getQuantityString(R.plurals.album_photos_subs, subPhotos, subPhotos);
            }
            AlbumItemViewModel viewModel = new AlbumItemViewModel(category.thumbnailUrl, category.name, photos, category.id);
            viewHolder.getBinding().setVariable(BR.viewModel, viewModel);
        }
    }


}
