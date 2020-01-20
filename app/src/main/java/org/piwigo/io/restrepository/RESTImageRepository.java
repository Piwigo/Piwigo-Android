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
import org.piwigo.io.restmodel.ImageInfo;
import org.piwigo.io.restmodel.ImageListResponse;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

public class RESTImageRepository extends RESTBaseRepository {

    private static final int PAGE_SIZE = 16;

    @Inject public RESTImageRepository(WebServiceFactory webServiceFactory, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler, UserManager userManager) {
        super(webServiceFactory, ioScheduler, uiScheduler, userManager);
    }

    public Observable<ImageInfo> getImages(@Nullable Integer categoryId) {
        return getPagesStartingAt(categoryId, webServiceFactory.create(), 0)
                .flatMap( imageListResponse -> Observable.fromIterable(imageListResponse.result.images));
    }

    private Observable<ImageListResponse> getPagesStartingAt(@Nullable Integer categoryId, RestService restService, int page) {
        Observable<ImageListResponse> a = restService
                .getImages(categoryId, page, PAGE_SIZE);

        return a.concatMap(response -> {
            int received = response.result.paging.count
                    + response.result.paging.page * PAGE_SIZE;

            if (response.result == null || response.result.paging.totalCount <= received) {
                return Observable.just(response);
            } else {
                return Observable.just(response)
                        .concatWith(getPagesStartingAt(categoryId, restService, page + 1));
            }

        });
   }
}


