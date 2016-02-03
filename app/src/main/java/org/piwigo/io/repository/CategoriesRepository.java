/*
 * Copyright 2016 Phil Bayfield https://philio.me
 * Copyright 2016 Piwigo Team http://piwigo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.piwigo.io.repository;

import android.util.Pair;

import org.piwigo.io.model.Category;
import org.piwigo.io.model.ImageInfo;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func2;

public class CategoriesRepository extends BaseRepository {

    @Inject public CategoriesRepository() {}

    public Observable<List<Pair<Category, ImageInfo>>> getCategories(Integer categoryId) {
        return restService
                .getCategories(categoryId)
                .flatMapIterable(categoryListResponse -> categoryListResponse.result.categories)
                .filter(category -> categoryId == null || category.id != categoryId)
                .flatMap(category -> {
                    Observable<ImageInfo> imageInfo = restService.getImageInfo(category.representativePictureId)
                            .map(getImageInfoResponse -> getImageInfoResponse.imageInfo);
                    return Observable.zip(Observable.just(category), imageInfo, (Func2<Category, ImageInfo, Pair<Category, ImageInfo>>) Pair::new);
                })
                .toSortedList((firstPair, secondPair) -> {
                    String firstRank = firstPair.first.globalRank;
                    String secondRank = secondPair.first.globalRank;
                    int firstRankIndex = firstRank.contains(".") ? firstRank.lastIndexOf(".") + 1 : 0;
                    int secondRankIndex = secondRank.contains(".") ? secondRank.lastIndexOf(".") + 1 : 0;
                    return Integer.parseInt(firstRank.substring(firstRankIndex)) - Integer.parseInt(secondRank.substring(secondRankIndex));
                })
                .compose(applySchedulers());
    }

}
