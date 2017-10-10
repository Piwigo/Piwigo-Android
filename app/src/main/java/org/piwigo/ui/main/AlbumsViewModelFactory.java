/*
 * Copyright 2017 Phil Bayfield https://philio.me
 * Copyright 2017 Piwigo Team http://piwigo.org
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

package org.piwigo.ui.main;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;

import org.piwigo.io.repository.CategoriesRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AlbumsViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;
    private final CategoriesRepository categoriesRepository;

    @Inject public AlbumsViewModelFactory(Context context, CategoriesRepository categoriesRepository) {
        this.context = context;
        this.categoriesRepository = categoriesRepository;
    }

    @Override public <T extends ViewModel> T create(Class<T> viewModelClass) {
        if (viewModelClass.isAssignableFrom(AlbumsViewModel.class)) {
            //noinspection unchecked
            return (T) new AlbumsViewModel(categoriesRepository, context.getResources());
        }
        throw new IllegalStateException("Unable to create " + viewModelClass.getName());
    }
}
