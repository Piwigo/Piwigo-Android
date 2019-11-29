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

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.piwigo.accounts.UserManager;
import org.piwigo.io.restrepository.RestUserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MainViewModelFactory implements ViewModelProvider.Factory {

    private final UserManager userManager;
    private final RestUserRepository userRepository;

    @Inject public MainViewModelFactory(UserManager userManager, RestUserRepository userRepository) {
        this.userManager = userManager;
        this.userRepository = userRepository;
    }

    @Override public <T extends ViewModel> T create(Class<T> viewModelClass) {
        if (viewModelClass.isAssignableFrom(MainViewModel.class)) {
            //noinspection unchecked
            return (T) new MainViewModel(userManager, userRepository);
        }
        throw new IllegalStateException("Unable to create " + viewModelClass.getName());
    }
}
