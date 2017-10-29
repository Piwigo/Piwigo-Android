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

package org.piwigo.io.repository;

import android.accounts.Account;
import android.support.annotation.Nullable;

import org.piwigo.io.RestService;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.io.model.Category;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Scheduler;

public class CategoriesRepository extends BaseRepository {

    @Inject public CategoriesRepository(RestServiceFactory restServiceFactory, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler) {
        super(restServiceFactory, ioScheduler, uiScheduler);
    }

    public Observable<List<Category>> getCategories(Account account, @Nullable Integer categoryId) {
        RestService restService = restServiceFactory.createForAccount(account);

        return restService.getCategories(categoryId, "large")
                .flatMap(response -> Observable.from(response.result.categories))
                .filter(category -> categoryId == null || category.id != categoryId)
                .toSortedList((category1, category2) -> Double.compare(Double.parseDouble(category1.globalRank), Double.parseDouble(category2.globalRank)))
                .compose(applySchedulers());
    }
}
