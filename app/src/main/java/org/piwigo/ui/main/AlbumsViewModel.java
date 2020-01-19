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
import android.database.sqlite.SQLiteException;
import android.util.Log;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModel;

import org.piwigo.BR;
import org.piwigo.R;
import org.piwigo.accounts.UserManager;
import org.piwigo.data.model.Category;
import org.piwigo.data.model.PositionedItem;
import org.piwigo.data.model.VariantWithImage;
import org.piwigo.data.repository.CategoriesRepository;
import org.piwigo.data.repository.ImageRepository;
import org.piwigo.ui.shared.BindingRecyclerViewAdapter;
import org.reactivestreams.Subscription;

import java.io.IOException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;

public class AlbumsViewModel extends ViewModel {

    private static final String TAG = AlbumsViewModel.class.getName();

    private boolean isLoadingCategories = false;
    private boolean isLoadingImages = false;
    public ObservableBoolean isLoading = new ObservableBoolean();

    public ObservableArrayList<VariantWithImage> images = new ObservableArrayList<>();
    public ObservableArrayList<Category> albums = new ObservableArrayList<>();
    public BindingRecyclerViewAdapter.ViewBinder<Category> albumsViewBinder = new CategoryViewBinder();
    public BindingRecyclerViewAdapter.ViewBinder<VariantWithImage> photoViewBinder = new ImagesViewBinder();

    private final UserManager userManager;
    private final CategoriesRepository categoriesRepository;
    private final ImageRepository imageRepository;

    private final Resources resources;

    private Subscription albumsSubscription;
    private Subscription photosSubscription;

    private Integer category = null;

    private MainViewModel mMainViewModel;

    AlbumsViewModel(UserManager userManager, CategoriesRepository categoriesRepository,
                    ImageRepository imageRepository, Resources resources) {
        this.userManager = userManager;
        this.categoriesRepository = categoriesRepository;
        this.imageRepository = imageRepository;
        this.resources = resources;
    }

    @Override
    protected void onCleared() {
        if (albumsSubscription != null) {
            albumsSubscription.cancel();
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
            albumsSubscription.cancel();
            albumsSubscription = null;
        }
        if (photosSubscription != null) {
            // cleanup, just in case
            photosSubscription.cancel();
            photosSubscription = null;
        }
        if (account != null) {
            categoriesRepository.getCategories(category)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CategoriesSubscriber());

            imageRepository.getImages(category)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new ImageSubscriber());
        }
    }

    void loadAlbums(Integer categoryId) {
        if(category == null || category != category) {
            category = categoryId;
            forcedLoadAlbums();
        }
    }

    public void setMainViewModel(MainViewModel vm){
        mMainViewModel = vm;
    }

    public void onRefresh() {
        forcedLoadAlbums();
    }

    private void updateLoading() {
        isLoading.set(isLoadingCategories || isLoadingImages);
    }

    private class CategoriesSubscriber extends DisposableObserver<PositionedItem<Category>> {
        public CategoriesSubscriber(){
            super();
            isLoadingCategories = true;
            updateLoading();
        }

        @Override
        public void onComplete() {
            isLoadingCategories = false;
            updateLoading();
        }

        @Override
        public void onError(Throwable e) {
            isLoadingCategories = false;
            updateLoading();

            if (e instanceof SQLiteException){
                // TODO: check whether this is really what we want here
                Log.e(TAG, "CategoriesSubscriber.onError(): " + e.getMessage());
                throw new RuntimeException(e);
            } else if (e instanceof IOException) {
                Log.e(TAG, "CategoriesSubscriber.onError(): " + e.getMessage());
                // TODO: #91 tell the user about the network problem
            } else {
                // NO: NEVER throw an exception here
                // throw new RuntimeException(e);
                Log.e(TAG, "CategoriesSubscriber.onError(): " + e.getMessage());
                // TODO: #161 highlight problem to the user
            }
        }

        @Override
        public void onNext(PositionedItem<Category> category) {
            while(albums.size() <= category.getPosition()) {
                albums.add(null);
            }
            albums.set(category.getPosition(), category.getItem());
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

    private class ImageSubscriber extends DisposableObserver<PositionedItem<VariantWithImage>>{
        public ImageSubscriber(){
            super();
            isLoadingImages = true;
            updateLoading();
        }
        @Override
        public void onNext(PositionedItem<VariantWithImage> item) {
            if(images.size() == item.getPosition()){
                images.add(item.getItem());
            }else {
                while (images.size() <= item.getPosition()) {
                    images.add(null);
                }
                images.set(item.getPosition(), item.getItem());
            }
        }

        @Override
        public void onComplete() {
            isLoadingImages = false;
            updateLoading();
        }

        @Override
        public void onError(Throwable e) {
            isLoadingImages = false;
            updateLoading();
            if (e instanceof IOException) {
                Log.e(TAG, "ImagesSubscriber: " + e.getMessage());
// TODO: #91 tell the user about the network problem
            } else {
                Log.e(TAG, "ImagesSubscriber: " + e.getMessage());
                // TODO: #161 highlight problem to the user (this is alreay here...)
                if(mMainViewModel != null) {
                    mMainViewModel.setError(e);
                }
            }
            e.printStackTrace();
        }
    }

    private class ImagesViewBinder implements BindingRecyclerViewAdapter.ViewBinder<VariantWithImage> {

        @Override
        public int getViewType(VariantWithImage image) {
            return 0;
        }

        @Override
        public int getLayout(int viewType) {
            return R.layout.item_images;
        }

        @Override
        public void bind(BindingRecyclerViewAdapter.ViewHolder viewHolder, VariantWithImage image) {
            // TODO: make configurable to also show the photo name here
            ImagesItemViewModel viewModel = new ImagesItemViewModel(image, images.indexOf(image), images);
            viewHolder.getBinding().setVariable(BR.viewModel, viewModel);
        }
    }
}
