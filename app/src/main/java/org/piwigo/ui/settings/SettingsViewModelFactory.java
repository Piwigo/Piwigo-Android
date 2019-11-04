/*
 * Piwigo for Android
 * Copyright (C) 2019-2019 Piwigo Team http://piwigo.org
 * Copyright (C) 2019-2019 Radko Varchola
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

package org.piwigo.ui.settings;
import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

@Singleton
public class SettingsViewModelFactory implements ViewModelProvider.Factory {

    @Inject
    public SettingsViewModelFactory() {
    }

    @Override public <T extends ViewModel> T create(Class<T> viewModelClass) {
        if (viewModelClass.isAssignableFrom(SettingsViewModel.class)) {
            //noinspection unchecked
            return (T) new SettingsViewModel();
        }
        throw new IllegalStateException("Unable to create " + viewModelClass.getName());
    }
}
