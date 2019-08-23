/*
 * Piwigo for Android
 * Copyright (C) 2017-2018 Jeff Ayers
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
import org.piwigo.io.model.ImageInfo;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Scheduler;

public class ImageRepository extends BaseRepository {

    @Inject public ImageRepository(RestServiceFactory restServiceFactory, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler, UserManager userManager) {
        super(restServiceFactory, ioScheduler, uiScheduler, userManager);
    }

    public Observable<List<ImageInfo>> getImages(Account account, @Nullable Integer categoryId) {
        RestService restService = restServiceFactory.createForAccount(account);

        return restService
                .getImages(categoryId)
                .map(imageListResponse -> imageListResponse.result.images)
// TODO: #90 generalize sorting
                .compose(applySchedulers());

   }
}


