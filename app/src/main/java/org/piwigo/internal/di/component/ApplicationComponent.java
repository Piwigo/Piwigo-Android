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

package org.piwigo.internal.di.component;

import com.squareup.picasso.Picasso;

import org.piwigo.PiwigoApplication;
import org.piwigo.bg.UploadService;
import org.piwigo.internal.di.module.ApiModule;
import org.piwigo.internal.di.module.ApplicationModule;
import org.piwigo.internal.di.module.AndroidInjectorModule;
import org.piwigo.internal.di.module.NetworkModule;
import org.piwigo.io.repository.PreferencesRepository;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class, NetworkModule.class, ApiModule.class, AndroidInjectorModule.class})
public interface ApplicationComponent {

    void inject(PiwigoApplication application);

    void inject(UploadService service);

    Picasso picasso();

    PreferencesRepository preferencesRepository();
}
