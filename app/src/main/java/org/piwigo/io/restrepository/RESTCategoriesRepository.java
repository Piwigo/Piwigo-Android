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

package org.piwigo.io.restrepository;

import android.accounts.Account;

import androidx.annotation.Nullable;

import org.piwigo.accounts.UserManager;
import org.piwigo.io.RestService;
import org.piwigo.io.WebServiceFactory;
import org.piwigo.io.restmodel.Category;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

public class RESTCategoriesRepository extends RESTBaseRepository {

    @Inject public RESTCategoriesRepository(WebServiceFactory webServiceFactory, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler, UserManager userManager) {
        super(webServiceFactory, ioScheduler, uiScheduler, userManager);
    }

    public Observable<Category> getCategories(@Nullable Integer categoryId, String thumbnailSize) {
        RestService restService = webServiceFactory.create();
        return restService.getCategories(categoryId, thumbnailSize)
                .subscribeOn(ioScheduler)
                .observeOn(uiScheduler)
                .flatMap(response -> {
                    if(response.result != null) {
                        return Observable.fromIterable(response.result.categories);
                    }else{
                        return null; //Observable.error(new Throwable("Error " + imageListResponse.stat + " " + imageListResponse.err + ": " + imageListResponse.message));
                    }
                })
                // only return the child categories (or the root)
                .filter(category -> categoryId == null || category.id != categoryId)
                ;

    }
}
