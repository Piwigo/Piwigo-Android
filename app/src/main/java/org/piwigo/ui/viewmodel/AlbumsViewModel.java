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

package org.piwigo.ui.viewmodel;

import org.piwigo.io.model.response.CategoryListResponse;
import org.piwigo.io.repository.CategoriesRepository;

import java.util.List;

import javax.inject.Inject;

import rx.Observer;

public class AlbumsViewModel extends BaseViewModel {

    @Inject CategoriesRepository categoriesRepository;

    @Inject public AlbumsViewModel() {}

    public void loadAlbums() {
        categoriesRepository.getCategories()
                .subscribe(new Observer<List<CategoryListResponse.Result.Category>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<CategoryListResponse.Result.Category> categories) {

                    }
                });
    }

}
