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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.piwigo.accounts.UserManager;
import org.piwigo.io.WebServiceFactory;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
public class ApiModule {

    @Provides @Singleton Gson provideGson() {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }

    @Provides @Singleton @Named("IoScheduler")
    Scheduler provideIoScheduler() {
        return Schedulers.io();
    }

    @Provides @Singleton @Named("UiScheduler")
    Scheduler provideUiScheduler() {
        return AndroidSchedulers.mainThread();
    }

    @Provides @Singleton
    WebServiceFactory provideRestServiceFactory(HttpLoggingInterceptor loggingInterceptor, Gson gson, UserManager userManager) {
        return new WebServiceFactory(loggingInterceptor, gson, userManager);
    }
}