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

import org.piwigo.accounts.UserManager;
import org.piwigo.data.db.CacheDatabase;
import org.piwigo.data.model.Category;
import org.piwigo.data.model.PositionedItem;
import org.piwigo.helper.NaturalOrderComparator;
import org.piwigo.io.PreferencesRepository;
import org.piwigo.io.restrepository.RESTCategoriesRepository;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.annotation.Nullable;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;

public class CategoriesRepository {

    private final RESTCategoriesRepository mRestCategoryRepo;
    private final UserManager mUserManager;
    private final PreferencesRepository mPreferences;
    private final CacheDatabase mCache;
    private final Scheduler ioScheduler;
    private final Scheduler uiScheduler;


    @Inject public CategoriesRepository(RESTCategoriesRepository restCategoryRepo, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler, UserManager userManager, PreferencesRepository preferences, CacheDatabase cache) {
        mRestCategoryRepo = restCategoryRepo;
        mUserManager = userManager;
        mPreferences = preferences;
        mCache = cache;
        this.ioScheduler = ioScheduler;
        this.uiScheduler = uiScheduler;
    }

    public Observable<PositionedItem<Category>> getCategories(@Nullable Integer categoryId) {
        return mCache.categoryDao().getCategoriesIn(categoryId)
                .subscribeOn(ioScheduler)
                .observeOn(uiScheduler)
                .flattenAsFlowable(s -> s)
                .zipWith(Flowable.range(0, Integer.MAX_VALUE),
                        (item, counter) -> new PositionedItem<Category>(counter, item))
                .concatWith(
                        mRestCategoryRepo.getCategories(mUserManager.getActiveAccount().getValue(),
                    categoryId,
                    mPreferences.getString(PreferencesRepository.KEY_PREF_DOWNLOAD_SIZE))
                    .toFlowable(BackpressureStrategy.BUFFER)
                    .subscribeOn(ioScheduler)
                    .observeOn(ioScheduler)

                    .zipWith(Flowable.range(0, Integer.MAX_VALUE), (restCat, counter) -> {
                        Category c = new Category();
                        c.name = restCat.name;
                        c.id = restCat.id;
                        if(restCat.idUppercat != 0) {
                            c.parentCatId = restCat.idUppercat;
                        }
                        c.nbImages = restCat.nbImages;
                        c.thumbnailUrl = restCat.thumbnailUrl;
                        c.globalRank = restCat.globalRank;
                        c.comment = restCat.comment;
                        c.nbCategories = restCat.nbCategories;
                        c.representativePictureId = restCat.representativePictureId;
                        c.totalNbImages = restCat.totalNbImages;
                        mCache.categoryDao().upsert(c);
                        return new PositionedItem<Category>(counter, c);
                    })
                                // TODO: delete categories in database after they have been deleted on the server
    // TODO: #90 generalize sorting
                    .sorted((categoryItem1, categoryItem2) -> NaturalOrderComparator.compare(categoryItem1.getItem().globalRank, categoryItem2.getItem().globalRank))
                    .subscribeOn(ioScheduler)
                    .observeOn(uiScheduler)

                )
                .toObservable();
    }
}
