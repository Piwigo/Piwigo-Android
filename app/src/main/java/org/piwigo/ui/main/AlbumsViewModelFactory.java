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

package org.piwigo.ui.main;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;

import org.piwigo.accounts.UserManager;
import org.piwigo.io.repository.CategoriesRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AlbumsViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;
    private final UserManager userManager;
    private final CategoriesRepository categoriesRepository;

    @Inject public AlbumsViewModelFactory(Context context, UserManager userManager, CategoriesRepository categoriesRepository) {
        this.context = context;
        this.userManager = userManager;
        this.categoriesRepository = categoriesRepository;
    }

    @Override public <T extends ViewModel> T create(Class<T> viewModelClass) {
        if (viewModelClass.isAssignableFrom(AlbumsViewModel.class)) {
            //noinspection unchecked
            return (T) new AlbumsViewModel(userManager, categoriesRepository, context.getResources());
        }
        throw new IllegalStateException("Unable to create " + viewModelClass.getName());
    }
}
