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

package org.piwigo.ui.main;

import android.accounts.Account;
import android.content.res.Resources;
import android.util.Log;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModel;

import org.piwigo.BR;
import org.piwigo.R;
import org.piwigo.accounts.UserManager;
import org.piwigo.io.model.Category;
import org.piwigo.io.model.ImageInfo;
import org.piwigo.io.repository.CategoriesRepository;
import org.piwigo.io.repository.ImageRepository;
import org.piwigo.io.repository.PreferencesRepository;
import org.piwigo.ui.shared.BindingRecyclerViewAdapter;

import java.io.IOException;
import java.util.List;


import rx.Subscriber;
import rx.Subscription;

public class AlbumsViewModel extends ViewModel {


    private static final String TAG = AlbumsViewModel.class.getName();

    public ObservableBoolean isLoading = new ObservableBoolean();

    public ObservableArrayList<ImageInfo> images = new ObservableArrayList<>();
    public ObservableArrayList<Category> albums = new ObservableArrayList<>();
    public BindingRecyclerViewAdapter.ViewBinder<Category> albumsViewBinder = new CategoryViewBinder();
    public BindingRecyclerViewAdapter.ViewBinder<ImageInfo> photoViewBinder = new ImagesViewBinder();

    private final UserManager userManager;
    private final CategoriesRepository categoriesRepository;
    private final ImageRepository imageRepository;
    private final PreferencesRepository preferences;

    private final Resources resources;

    private Subscription albumsSubscription;
    private Subscription photosSubscription;

    private Integer category = null;

    AlbumsViewModel(UserManager userManager, CategoriesRepository categoriesRepository,
                    ImageRepository imageRepository, Resources resources, PreferencesRepository preferences) {
        this.userManager = userManager;
        this.categoriesRepository = categoriesRepository;
        this.imageRepository = imageRepository;
        this.resources = resources;
        this.preferences = preferences;
    }

    @Override
    protected void onCleared() {
        if (albumsSubscription != null) {
            albumsSubscription.unsubscribe();
        }
    }

    /* which category is shown by this viewmodel */
    public Integer getCategory() {
        return category;
    }

    private void forcedLoadAlbums(){
        Account account = userManager.getActiveAccount().getValue();
        if (albumsSubscription != null) {
            // cleanup, just in case
            albumsSubscription.unsubscribe();
            albumsSubscription = null;
        }
        if (photosSubscription != null) {
            // cleanup, just in case
            photosSubscription.unsubscribe();
            photosSubscription = null;
        }
        if (account != null) {
            albumsSubscription = categoriesRepository.getCategories(account, category,
                    preferences.getString(PreferencesRepository.KEY_PREF_DOWNLOAD_SIZE))
                    .subscribe(new CategoriesSubscriber());
            photosSubscription = imageRepository.getImages(account, category)
                    .subscribe(new ImagesSubscriber());
        }
    }

    void loadAlbums(Integer categoryId) {
        if(category == null || category != category) {
            category = categoryId;
            forcedLoadAlbums();
        }
    }

    public void onRefresh() {
        isLoading.set(true);
        forcedLoadAlbums();
    }

    private class CategoriesSubscriber extends Subscriber<List<Category>> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            if (e instanceof IOException) {
                Log.e(TAG, "CategoriesSubscriber: " + e.getMessage());
                // TODO: #91 tell the user about the network problem
            } else {
                // NO: NEVER throw an exception here
                // throw new RuntimeException(e);
                Log.e(TAG, "CategoriesSubscriber: " + e.getMessage());
                // TODO: #161 highlight problem to the user
            }
        }

        @Override
        public void onNext(List<Category> categories) {
            albums.clear();
            albums.addAll(categories);
        }
    }

    private class CategoryViewBinder implements BindingRecyclerViewAdapter.ViewBinder<Category> {

        @Override
        public int getViewType(Category category) {
            return 0;
        }

        @Override
        public int getLayout(int viewType) {
            return R.layout.item_album;
        }

        @Override
        public void bind(BindingRecyclerViewAdapter.ViewHolder viewHolder, Category category) {
            String photos = resources.getQuantityString(R.plurals.album_photos, category.nbImages, category.nbImages);
            if (category.totalNbImages > category.nbImages) {
                int subPhotos = category.totalNbImages - category.nbImages;
                photos += resources.getQuantityString(R.plurals.album_photos_subs, subPhotos, subPhotos);
            }
            AlbumItemViewModel viewModel = new AlbumItemViewModel(category.thumbnailUrl, category.name, photos, category.id);
            viewHolder.getBinding().setVariable(BR.viewModel, viewModel);
        }
    }

    private class ImagesSubscriber extends Subscriber<List<ImageInfo>> {

        @Override
        public void onCompleted() {
            isLoading.set(false);
        }

        @Override
        public void onError(Throwable e) {
            if (e instanceof IOException) {
                Log.e(TAG, "ImagesSubscriber: " + e.getMessage());
// TODO: #91 tell the user about the network problem
            } else {
                // NO: NEVER throw an exception here
                // throw new RuntimeException(e);
                Log.e(TAG, "ImagesSubscriber: " + e.getMessage());
                // TODO: #161 highlight problem to the user
            }
        }

        @Override
        public void onNext(List<ImageInfo> imageList) {
            images.clear();
            images.addAll(imageList);
        }
    }

    private class ImagesViewBinder implements BindingRecyclerViewAdapter.ViewBinder<ImageInfo> {

        @Override
        public int getViewType(ImageInfo image) {
            return 0;
        }

        @Override
        public int getLayout(int viewType) {
            return R.layout.item_images;
        }

        @Override
        public void bind(BindingRecyclerViewAdapter.ViewHolder viewHolder, ImageInfo image) {
            // TODO: make image size selectable via settings (jca)
            // TODO: make configurable to also show the photo name here
            ImagesItemViewModel viewModel = new ImagesItemViewModel(image.derivatives.medium.url, images.indexOf(image), image.name, images);
            viewHolder.getBinding().setVariable(BR.viewModel, viewModel);
        }

    }

}
