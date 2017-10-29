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

package org.piwigo.internal.di.module;

import com.squareup.picasso.Picasso;

import org.piwigo.internal.binding.adapter.ImageViewBindingAdapter;
import org.piwigo.internal.di.scope.DataBinding;

import dagger.Module;
import dagger.Provides;

@Module
public class BindingModule {

    @Provides @DataBinding ImageViewBindingAdapter provideImageBindingAdapter(Picasso picasso) {
        return new ImageViewBindingAdapter(picasso);
    }
}
