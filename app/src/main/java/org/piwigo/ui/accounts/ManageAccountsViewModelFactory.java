/*
 * Piwigo for Android
 * Copyright (C) 2016-2017 Piwigo Team http://piwigo.org
 * Copyright (C) 2017-2017 Raphael Mack http://www.raphael-mack.de
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

package org.piwigo.ui.accounts;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;

import org.piwigo.helper.AccountHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ManageAccountsViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;
    @Inject AccountHelper accountHelper;

    @Inject public ManageAccountsViewModelFactory(Context context) {
        this.context = context;
    }

    @Override public <T extends ViewModel> T create(Class<T> viewModelClass) {
        if (viewModelClass.isAssignableFrom(ManageAccountsViewModel.class)) {
            //noinspection unchecked
            return (T) new ManageAccountsViewModel(context.getResources(), accountHelper);
        }
        throw new IllegalStateException("Unable to create " + viewModelClass.getName());
    }
}
