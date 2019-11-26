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
import org.piwigo.data.model.PositionedItem;
import org.piwigo.io.repository.PreferencesRepository;
import org.piwigo.io.repository.RESTImageRepository;
import org.piwigo.io.restmodel.Derivative;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;

public class ImageRepository {

    private final RESTImageRepository mRestImageRepo;
    private final UserManager mUserManager;
    private final Scheduler ioScheduler;
    private final Scheduler uiScheduler;
    private final PreferencesRepository mPreferences;

    @Inject public ImageRepository(RESTImageRepository restImageRepo, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler, UserManager userManager, PreferencesRepository preferences) {
        this.ioScheduler = ioScheduler;
        this.uiScheduler = uiScheduler;
        mRestImageRepo = restImageRepo;
        mUserManager = userManager;
        mPreferences = preferences;
    }

    /**
     * fetch all images in given category
     *
     * @param categoryId
     * @return items with their position
     */
    public Observable<PositionedItem<Image>> getImages(@Nullable Integer categoryId) {
// TODO: #90 implement sorting

// TODO: add fetching the images from local database/cache
        return mRestImageRepo.getImages(mUserManager.getActiveAccount().getValue(), categoryId)
                .toFlowable(BackpressureStrategy.BUFFER)

                .zipWith(Flowable.range(0, Integer.MAX_VALUE), (info, counter) -> {
                    Derivative d;
                    switch(mPreferences.getString(PreferencesRepository.KEY_PREF_DOWNLOAD_SIZE)){
                        case "thumb":
                            d = info.derivatives.thumb;
                            break;
                        case "small":
                            d = info.derivatives.small;
                            break;
                        case "xsmall":
                            d = info.derivatives.xsmall;
                            break;
                        case "medium":
                            d = info.derivatives.medium;
                            break;
                        case "large":
                            d = info.derivatives.large;
                            break;
                        case "xlarge":
                            d = info.derivatives.xlarge;
                            break;
                        case "xxlarge":
                            d = info.derivatives.xxlarge;
                            break;
                        case "square":
                        default:
                            d = info.derivatives.square;
                    }

                    Image i = new Image(d.url, d.width, d.height);
                    i.name = info.name;
                    i.id = info.id;
                    i.author = info.author;
                    i.comment = info.height;
                    i.width = info.width;
                    return new PositionedItem<>(counter, i);
                }).toObservable();
    }

    /**
     * store the img
     *
     * new images will be stored locally and add to the upload queue
     * for existing images the meta data will be updated
     *
     * exchanging the bitmap (locally or remotely) is not yet supported
     *
     * @param img
     */
    public void saveImage(Image img){
        // TODO: implement
        // if img has no id, add a new one, locally in the DB and add it to the upload queue
        // if img has an id, update the exisitng one
    }
}


