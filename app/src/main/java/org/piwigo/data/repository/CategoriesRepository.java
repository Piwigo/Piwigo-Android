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
import org.piwigo.data.model.Category;
import org.piwigo.helper.NaturalOrderComparator;
import org.piwigo.io.repository.PreferencesRepository;
import org.piwigo.io.repository.RESTCategoriesRepository;

import javax.inject.Inject;

import androidx.annotation.Nullable;

import io.reactivex.Observable;

public class CategoriesRepository {

    private final RESTCategoriesRepository mRestCategoryRepo;
    private final UserManager mUserManager;
    private final PreferencesRepository mPreferences;


    @Inject public CategoriesRepository(RESTCategoriesRepository restCategoryRepo, UserManager userManager, PreferencesRepository preferences) {
        mRestCategoryRepo = restCategoryRepo;
        mUserManager = userManager;
        mPreferences = preferences;
    }

    public Observable<Category> getCategories(@Nullable Integer categoryId) {
        return mRestCategoryRepo.getCategories(mUserManager.getActiveAccount().getValue(), categoryId,
                mPreferences.getString(PreferencesRepository.KEY_PREF_DOWNLOAD_SIZE))
                .map(restCat -> {
                    Category c = new Category();
                    c.name = restCat.name;
                    c.id = restCat.id;
                    c.nbImages = restCat.nbImages;
                    c.thumbnailUrl = restCat.thumbnailUrl;
                    c.globalRank = restCat.globalRank;
                    c.comment = restCat.comment;
                    c.nbCategories = restCat.nbCategories;
                    c.representativePictureId = restCat.representativePictureId;
                    c.totalNbImages = restCat.totalNbImages;
                    return c;
                })
// TODO: #90 generalize sorting
                .sorted((category1, category2) -> NaturalOrderComparator.compare(category1.globalRank, category2.globalRank))
                ;
    }
}
