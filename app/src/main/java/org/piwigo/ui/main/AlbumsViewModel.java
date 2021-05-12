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
import androidx.recyclerview.widget.RecyclerView;

import org.piwigo.BR;
import org.piwigo.EspressoIdlingResource;
import org.piwigo.R;
import org.piwigo.accounts.UserManager;
import org.piwigo.data.model.Category;
import org.piwigo.data.model.Image;
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

    public ObservableArrayList<ViewElement> data = new ObservableArrayList<>();
    /* only modify while holding lock on data */
    private int nrOfAlbums = 0;

    public BindingRecyclerViewAdapter.ViewBinder<Category> albumsViewBinder = new CategoryViewBinder();
    public BindingRecyclerViewAdapter.ViewBinder<VariantWithImage> photoViewBinder = new ImagesViewBinder();
    public BindingRecyclerViewAdapter.ViewBinder<ViewElement> dataViewBinder = new DataViewBinder();

    private final UserManager userManager;
    private final CategoriesRepository categoriesRepository;
    private final ImageRepository imageRepository;

    private final Resources resources;

    private Subscription albumsSubscription;
    private Subscription photosSubscription;

    private Integer category = null;

    private MainViewModel mMainViewModel;

    private RecyclerView.Adapter albumsAdapter = null;
    private RecyclerView.Adapter imagesAdapter = null;
    private RecyclerView.Adapter dataAdapter = null;

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

    public void forcedLoadAlbums() {

        EspressoIdlingResource.moreBusy("load albums");
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
        Account account = userManager.getActiveAccount().getValue();
        if (account == null) {
            EspressoIdlingResource.lessBusy("load albums", "account null");
            return;
        }
        if (userManager.isGuest(account) || userManager.sessionCookie() != null) {
            synchronized (data) {
                nrOfAlbums = 0;
                data.clear();
            }
            EspressoIdlingResource.moreBusy("load categories");
            categoriesRepository.getCategories(category)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CategoriesSubscriber());

            EspressoIdlingResource.moreBusy("load album images");
            imageRepository.getImages(category)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new ImageSubscriber());
        }
        EspressoIdlingResource.lessBusy("load albums", "forceLoadAlbums done");
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

    public void onRefresh(RecyclerView.Adapter dataAdapter/*, RecyclerView.Adapter imagesAdapter*/) {
        this.dataAdapter = dataAdapter;
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
            synchronized (data) {
                /* TODO: remove deleted items
                int i = albums.size() - nbCat - 1;
                if (albumsAdapter != null) {
                    while (i > 0) {
                        Log.d("m_cache_sync", "remove album");
                        data.remove(albums.size() - 1);
                        albums.remove(albums.size() - 1);
                        i--;
                    }
                    albumsAdapter.notifyDataSetChanged();
                }
                */
                isLoadingCategories = false;
                updateLoading();
            }
            EspressoIdlingResource.lessBusy("load categories", "onComplete");
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
            synchronized (data) {
                while (nrOfAlbums <= category.getPosition()) {
                    data.add(nrOfAlbums, null);
                    nrOfAlbums++;
                }
                if (data.get(category.getPosition()) == null){
                    data.set(category.getPosition(), new ViewElement(category.getItem()));
                }else{
                    ViewElement ve = data.get(category.getPosition());
                    if(ve == null || ve.getCategory() == null){
                        Log.d("AlbumsViewModel", "data element at " + category.getPosition() + " has wrong type");
                    }else
                    if(category.getItem().id != ve.getCategory().id) {
                        data.set(category.getPosition(), new ViewElement(category.getItem()));
                    }
                }
            }
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
// TODO: Get Image URL from local stored image instead ofr the thumbnailUrl
            AlbumItemViewModel viewModel = new AlbumItemViewModel(category.thumbnailUrl, category.name, category.comment, photos, category.id);
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
            synchronized (data) {
                // add empty spaces until the item position is available
                while (data.size() <= nrOfAlbums + item.getPosition()) {
                    data.add(null);
                }
                if (data.get(nrOfAlbums + item.getPosition()) != null
                   && data.get(nrOfAlbums + item.getPosition()).getType() == ViewElement.CATEGORY){
                    Log.d("AlbumsViewModel", "wrong type at index " + nrOfAlbums + item.getPosition() + " expected IMAGE found CATEGORY");
                }else if (data.get(nrOfAlbums + item.getPosition()) == null
                        || item.getItem().image.id != data.get(nrOfAlbums + item.getPosition()).getImage().image.id) {
                    data.set(nrOfAlbums + item.getPosition(), new ViewElement(item.getItem()));
                }
            }
        }

        @Override
        public void onComplete() {
            synchronized (data) {
                // TODO: remove data not available anymore
/*                int i = images.size() - nbImage - 1;
                if (albumsAdapter != null) {
                    while (i > 0) {
                        Log.d("m_cache_sync", "removed item");
                        images.remove(images.size() - 1);
                        data.remove(nrOfAlbums + images.size() - 1);
                        i--;
                    }
                    imagesAdapter.notifyDataSetChanged();
                }*/
                isLoadingImages = false;
                updateLoading();
            }
            EspressoIdlingResource.lessBusy("load album images", "ImageSubscriber.onComplete");
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
            Log.d("XXX", "ImagesViewBinder.bind() " + image.image.name);
            // TODO: make configurable to also show the photo name here
            ImagesItemViewModel viewModel;

            synchronized (data) {
                int idx = 0;
                while(data.get(idx).getImage() != image) idx++;

                viewModel = new ImagesItemViewModel(image, idx - nrOfAlbums, category);
            }

            viewHolder.getBinding().setVariable(BR.viewModel, viewModel);
        }
    }

    private class DataViewBinder implements BindingRecyclerViewAdapter.ViewBinder<ViewElement> {
        @Override
        public int getViewType(ViewElement element) {
            return element.type;
        }

        @Override
        public int getLayout(int viewType) {
            if(viewType == ViewElement.IMAGE) {
                return R.layout.item_images;
            }else if(viewType == ViewElement.CATEGORY) {
                return R.layout.item_album;
            }else{
                return -1;
            }
        }

        @Override
        public void bind(BindingRecyclerViewAdapter.ViewHolder viewHolder, ViewElement element) {

            if (element.type == ViewElement.IMAGE) {
                VariantWithImage image = element.getImage();
                Log.d("XXX", "ImagesViewBinder.bind() " + image.image.name);
                // TODO: make configurable to also show the photo name here
                ImagesItemViewModel viewModel;
                synchronized (data) {
                    int idx = data.indexOf(element);
                    viewModel = new ImagesItemViewModel(image, idx - nrOfAlbums, category);
                }
                viewHolder.getBinding().setVariable(BR.viewModel, viewModel);
            }
            if (element.type == ViewElement.CATEGORY) {
                Category category = element.getCategory();
                String photos = resources.getQuantityString(R.plurals.album_photos, category.nbImages, category.nbImages);
                if (category.totalNbImages > category.nbImages) {
                    int subPhotos = category.totalNbImages - category.nbImages;
                    photos += resources.getQuantityString(R.plurals.album_photos_subs, subPhotos, subPhotos);
                }
// TODO: Get Image URL from local stored image instead ofr the thumbnailUrl
                AlbumItemViewModel viewModel = new AlbumItemViewModel(category.thumbnailUrl, category.name, category.comment, photos, category.id);
                viewHolder.getBinding().setVariable(BR.viewModel, viewModel);
            }
        }
    }

    public class ViewElement{
        public static final int IMAGE = 0;
        public static final int CATEGORY = 1;

        private int type;
        private VariantWithImage image;
        private Category category;

        public ViewElement(VariantWithImage image){
            this.type = IMAGE;
            this.image = image;
            this.category = null;
        }

        public ViewElement(Category category){
            this.type = CATEGORY;
            this.image = null;
            this.category = category;
        }

        public int getType() {
            return type;
        }

        public VariantWithImage getImage() {
            return image;
        }

        public Category getCategory() {
            return category;
        }
    }
}
