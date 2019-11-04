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

import android.accounts.AccountManager;
import android.content.Context;

import com.squareup.picasso.Picasso;

import org.piwigo.BuildConfig;
import org.piwigo.PiwigoApplication;
import org.piwigo.accounts.UserManager;
import org.piwigo.internal.cache.PiwigoImageCache;
import org.piwigo.io.repository.PreferencesRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private final PiwigoApplication application;

    public ApplicationModule(PiwigoApplication application) {
        this.application = application;
    }

    @Provides @Singleton Context provideApplicationContext() {
        return application;
    }

    @Provides @Singleton Picasso providePicasso() {
        return new Picasso.Builder(application)
                .indicatorsEnabled(BuildConfig.DEBUG) //We may not want this for production build..
                .memoryCache(new PiwigoImageCache(application)) //What about this ?
                .build();
    }

    @Provides @Singleton AccountManager provideAccountManager() {
        return AccountManager.get(application);
    }

    @Provides @Singleton UserManager provideUserManager(AccountManager accountManager, PreferencesRepository preferencesRepository) {
        return new UserManager(accountManager, application.getResources(), preferencesRepository);
    }

    @Provides @Singleton
    PreferencesRepository providePreferencesRepository() {
        return new PreferencesRepository(application);
    }
}
