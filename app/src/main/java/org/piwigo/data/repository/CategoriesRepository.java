/*
 * Piwigo for Android
 * Copyright (C) 2016-2019 Piwigo Team http://piwigo.org
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

package org.piwigo.data.repository;

import android.accounts.Account;
import android.util.Log;

import org.piwigo.accounts.UserManager;
import org.piwigo.data.db.CacheDatabase;
import org.piwigo.data.model.Category;
import org.piwigo.data.model.Image;
import org.piwigo.data.model.PositionedItem;
import org.piwigo.helper.NaturalOrderComparator;
import org.piwigo.io.PreferencesRepository;
import org.piwigo.io.restrepository.RESTCategoriesRepository;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class CategoriesRepository implements Observer<Account> {

    private final RESTCategoriesRepository mRestCategoryRepo;
    private final UserManager mUserManager;
    private final PreferencesRepository mPreferences;
    private final Scheduler ioScheduler;
    private final Scheduler uiScheduler;

    private Object dbAccountLock = new Object();
    private CacheDatabase mCache;

    @Inject public CategoriesRepository(RESTCategoriesRepository restCategoryRepo, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler, UserManager userManager, PreferencesRepository preferences) {
        mRestCategoryRepo = restCategoryRepo;
        mUserManager = userManager;
        mPreferences = preferences;
        this.ioScheduler = ioScheduler;
        this.uiScheduler = uiScheduler;
        mUserManager.getActiveAccount().observeForever(this);
        synchronized (dbAccountLock) {
           mCache = mUserManager.getDatabaseForCurrent();
        }
    }

    public Observable<PositionedItem<Category>> getCategories(@Nullable Integer categoryId) {
        Log.d("CategoriesRepository", "getCategories");
        CacheDatabase db;
        synchronized (dbAccountLock) {
            db = mCache; // this will keep the database if the account is switched. As the old DB will be closed this thread will be reporting an exception but we accept that for now
        }

        Flowable<Integer> remoteIDs = mRestCategoryRepo.getCategories(
                categoryId,
                mPreferences.getString(PreferencesRepository.KEY_PREF_DOWNLOAD_SIZE))
                .subscribeOn(ioScheduler)
                .observeOn(ioScheduler)
                .toFlowable(BackpressureStrategy.BUFFER)
                .zipWith(Flowable.range(0, Integer.MAX_VALUE), (restCat, counter) -> restCat.id);

        Flowable<PositionedItem<Category>> remotes = mRestCategoryRepo.getCategories(
            categoryId,
            mPreferences.getString(PreferencesRepository.KEY_PREF_DOWNLOAD_SIZE))
            .subscribeOn(ioScheduler)
            .observeOn(ioScheduler)
            .toFlowable(BackpressureStrategy.BUFFER)
            .zipWith(Flowable.range(0, Integer.MAX_VALUE), (restCat, counter) -> {

                Category c = new Category();
                c.name = restCat.name;
                c.id = restCat.id;
                if (restCat.idUppercat != 0) {
                    c.parentCatId = restCat.idUppercat;
                }
                c.nbImages = restCat.nbImages;
                c.thumbnailUrl = restCat.thumbnailUrl;
                c.globalRank = restCat.globalRank;
                c.comment = restCat.comment;
                c.nbCategories = restCat.nbCategories;
                c.representativePictureId = restCat.representativePictureId;
                c.totalNbImages = restCat.totalNbImages;
                db.categoryDao().upsert(c);

                return new PositionedItem<Category>(counter, c, true);
            })
            // TODO: #90 generalize sorting
            .sorted((categoryItem1, categoryItem2) -> NaturalOrderComparator.compare(categoryItem1.getItem().globalRank, categoryItem2.getItem().globalRank));

//        remotes.subscribe();
        if(db == null){
            return remotes.toObservable();
        }else {
            return db.categoryDao().getCategoriesIn(categoryId)
                .subscribeOn(ioScheduler)
                .observeOn(ioScheduler)
                .flattenAsFlowable(s -> s)
/*                .filter(category -> {
                    Log.d("m_cache_sync_cat","Read "+category.name);
                    if(!remoteIDs.contains(category.id).blockingGet()) {
                        Log.d("m_cache_sync_cat","Deleted "+category.name);
                        return false;
                    }
                    return true;
                })*/
                .zipWith(Flowable.range(0, Integer.MAX_VALUE), (item, counter) -> {
                    return new PositionedItem<Category>(counter, item, true);
                })
                .concatWith(remotes)
                .toObservable();
        }
    }
    /**
     * Called when the account is changed.
     *
     * @param account The new data
     */
    @Override
    public void onChanged(Account account) {
        synchronized (dbAccountLock) {
            mCache = mUserManager.getDatabaseForCurrent();
        }
    }

    public void updateCategoryCache(@Nullable Integer categoryId) {
        CacheDatabase db;
        synchronized (dbAccountLock) {
            db = mCache; // this will keep the database if the account is switched. As the old DB will be closed this thread will be reporting an exception but we accept that for now
        }

        Flowable<Integer> remoteIDs = mRestCategoryRepo.getCategories(
                categoryId,
                mPreferences.getString(PreferencesRepository.KEY_PREF_DOWNLOAD_SIZE))
                .subscribeOn(ioScheduler)
                .observeOn(ioScheduler)
                .toFlowable(BackpressureStrategy.BUFFER)
                .zipWith(Flowable.range(0, Integer.MAX_VALUE), (restCat, counter) -> restCat.id);

        if(db != null) {
            db.categoryDao().getCategoriesIn(categoryId)
                    .subscribeOn(ioScheduler)
                    .observeOn(ioScheduler)
                    .flattenAsFlowable(s -> s)
                    .zipWith(Flowable.range(0, Integer.MAX_VALUE), (item, counter) -> {
                        if(!remoteIDs.contains(item.id).blockingGet()) {
                            Log.d("m_cache_sync_cat","Deleted "+item.name);
                            db.imageCategoryMapDao().deleteFromCategory(item.id);
                            db.categoryDao().delete(item);
                        }
                        return new PositionedItem<Category>(counter, item, true);
                    })
                    .toObservable()
                    .subscribe();
        }
    }
}
