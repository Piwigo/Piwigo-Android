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
import org.piwigo.data.model.Category;
import org.piwigo.data.model.Image;
import org.piwigo.data.model.PositionedItem;
import org.piwigo.data.repository.CategoriesRepository;
import org.piwigo.data.repository.ImageRepository;
import org.piwigo.ui.shared.BindingRecyclerViewAdapter;
import org.reactivestreams.Subscription;

import java.io.IOException;

import io.reactivex.observers.DisposableObserver;

public class AlbumsViewModel extends ViewModel {

    private static final String TAG = AlbumsViewModel.class.getName();

    public ObservableBoolean isLoading = new ObservableBoolean();

    public ObservableArrayList<Image> images = new ObservableArrayList<>();
    public ObservableArrayList<Category> albums = new ObservableArrayList<>();
    public BindingRecyclerViewAdapter.ViewBinder<Category> albumsViewBinder = new CategoryViewBinder();
    public BindingRecyclerViewAdapter.ViewBinder<Image> photoViewBinder = new ImagesViewBinder();

    private final UserManager userManager;
    private final CategoriesRepository categoriesRepository;
    private final ImageRepository imageRepository;

    private final Resources resources;

    private Subscription albumsSubscription;
    private Subscription photosSubscription;

    private Integer category = null;

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
                    .subscribe(new CategoriesSubscriber());
            images.clear();
            imageRepository.getImages(category)
                    .subscribe(new ImageSubscriber());
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

    private class CategoriesSubscriber extends DisposableObserver<PositionedItem<Category>> {

        @Override
        public void onComplete() {

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
        public void onNext(PositionedItem<Category> category) {
            albums.ensureCapacity(Math.max(category.getPosition() + 1, images.size() * 2));
            albums.add(category.getPosition(), category.getItem());
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

    private class ImageSubscriber extends DisposableObserver<PositionedItem<Image>>{
        // TODO: get rid of flickering, as currently the images.add will cause the RececlerView to redraw completely which is a nightmare
        // this is to be done in the BindingRecyclerViewAdapter
        @Override
        public void onNext(PositionedItem<Image> item) {
            images.ensureCapacity(Math.max(item.getPosition() + 1, images.size() * 2));
            images.add(item.getPosition(), item.getItem());
        }

        @Override
        public void onComplete() {

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
    }

    private class ImagesViewBinder implements BindingRecyclerViewAdapter.ViewBinder<Image> {

        @Override
        public int getViewType(Image image) {
            return 0;
        }

        @Override
        public int getLayout(int viewType) {
            return R.layout.item_images;
        }

        @Override
        public void bind(BindingRecyclerViewAdapter.ViewHolder viewHolder, Image image) {
            // TODO: make configurable to also show the photo name here
            ImagesItemViewModel viewModel = new ImagesItemViewModel(image, images.indexOf(image), image.name, images);
            viewHolder.getBinding().setVariable(BR.viewModel, viewModel);
        }

    }
}
