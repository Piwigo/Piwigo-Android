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

import androidx.annotation.Nullable;

import org.piwigo.accounts.UserManager;
import org.piwigo.data.model.Image;
import org.piwigo.io.repository.RESTImageRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ImageRepository {

    private final RESTImageRepository mRestImageRepo;
    private final UserManager mUserManager;

    @Inject public ImageRepository(RESTImageRepository restImageRepo, UserManager userManager) {
        mRestImageRepo = restImageRepo;
        mUserManager = userManager;
    }

    public Observable<Image> getImages(@Nullable Integer categoryId) {

        return mRestImageRepo.getImages(mUserManager.getActiveAccount().getValue(), categoryId)
//                .flatMapIterable(list -> list)
                .map(info -> {
                    Image i = new Image();
                    i.name = info.name;
                    i.id = info.id;
                    i.elementUrl = info.elementUrl;
                    i.author = info.author;
                    i.comment = info.height;
                    i.width = info.width;
                    return i;
                })
                ;
   }
}


