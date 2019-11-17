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

package org.piwigo.io.repository;


import android.accounts.Account;

import androidx.annotation.Nullable;

import org.piwigo.accounts.UserManager;
import org.piwigo.io.RestService;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.io.restmodel.ImageInfo;
import org.piwigo.io.restmodel.ImageListResponse;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;


public class RESTImageRepository extends RESTBaseRepository {

    private static final int PAGE_SIZE = 80;

    @Inject public RESTImageRepository(RestServiceFactory restServiceFactory, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler, UserManager userManager) {
        super(restServiceFactory, ioScheduler, uiScheduler, userManager);
    }

    public Observable<ImageInfo> getImages(Account account, @Nullable Integer categoryId) {
        return getImageStartingAtPage(account, categoryId, restServiceFactory.createForAccount(account), 0);
    }

    private Observable<ImageInfo> getImageStartingAtPage(Account account, @Nullable Integer categoryId, RestService restService, int page) {

        Observable<ImageListResponse> a = restService
        // TODO: #144
            .getImages(categoryId, page, PAGE_SIZE)
            .subscribeOn(ioScheduler)
            .observeOn(uiScheduler);


        return a.flatMap(imageListResponse -> {
            if (imageListResponse.result != null
                    && imageListResponse.result.paging.count < imageListResponse.result.paging.totalCount) {
                return Observable.fromIterable(imageListResponse.result.images).concatWith(getImageStartingAtPage(account, categoryId, restService, page + 1));
            } else {
                return Observable.fromIterable(imageListResponse.result.images);
            }
        });

/*
        a = a.flatMap(response -> {
                    if (response.result != null) {
                        return Observable.just(response).concatWith(restService.getImages(categoryId, page + 1, PAGE_SIZE)
                                .subscribeOn(ioScheduler)
                                .observeOn(uiScheduler)
                        );
                    } else {
                        return Observable.just(response);
                    }
                }
            );

               ;
*/
/*
                  .map(imageListResponse -> {
                      if(imageListResponse.result != null) {
                          if(imageListResponse.result.paging.count < imageListResponse.result.paging.totalCount) {
*
                              Observable<ImageInfo> a = restService.getImages(categoryId, 1, 10)
                                      .subscribeOn(ioScheduler)
                                      .observeOn(uiScheduler)

                                      .map(imageListRespons -> {
                                          if (imageListRespons.result != null) {
                                              return Observable.fromIterable(imageListRespons.result.images);
                                          } else {
                                              return Observable.empty(); //Observable.error(new Throwable("Error " + imageListResponse.stat + " " + imageListResponse.err + ": " + imageListResponse.message));
                                          }
                                      })

                                      ;
                              return Observable.fromIterable(imageListResponse.result.images)
                                      .concatWith(a);
                                      * /
                              return Observable.empty().concatWith(Observable.fromIterable(imageListResponse.result.images));
                          } else {
                              return Observable.fromIterable(imageListResponse.result.images);
                          }
                      }else {
                          return null; //Observable.error(new Throwable("Error " + imageListResponse.stat + " " + imageListResponse.err + ": " + imageListResponse.message));
                      }
                  })
// TODO: #90 generalize sorting
                ;

 */

   }
}


