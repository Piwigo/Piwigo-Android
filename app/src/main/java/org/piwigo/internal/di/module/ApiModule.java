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

import org.piwigo.helper.AccountHelper;
import org.piwigo.internal.di.qualifier.ForRetrofit;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.io.Session;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public class ApiModule {

    @Provides @Singleton Session provideSession() {
        return new Session();
    }

    @Provides @Singleton Gson provideGson() {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }

    @Provides @Singleton Retrofit.Builder provideRetrofitBuilder(@ForRetrofit OkHttpClient client, Gson gson) {
        return new Retrofit.Builder()
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create());
    }

    @Provides @Singleton @Named("IoScheduler") Scheduler provideIoScheduler() {
        return Schedulers.io();
    }

    @Provides @Singleton @Named("UiScheduler") Scheduler provideUiScheduler() {
        return AndroidSchedulers.mainThread();
    }

    @Provides @Singleton RestServiceFactory provideRestServiceFactory(Retrofit.Builder builder, AccountHelper accountHelper) {
        return new RestServiceFactory(builder, accountHelper);
    }
}