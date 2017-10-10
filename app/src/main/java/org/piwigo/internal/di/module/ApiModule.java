/*
 * Copyright 2015 Phil Bayfield https://philio.me
 * Copyright 2015 Piwigo Team http://piwigo.org
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