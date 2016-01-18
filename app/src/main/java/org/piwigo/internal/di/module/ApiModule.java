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
import com.squareup.okhttp.OkHttpClient;

import org.piwigo.BuildConfig;
import org.piwigo.io.DynamicEndpoint;
import org.piwigo.io.RestService;
import org.piwigo.io.Session;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static retrofit.RestAdapter.LogLevel.FULL;
import static retrofit.RestAdapter.LogLevel.NONE;

@Module
public class ApiModule {

    @Provides @Singleton Session provideSession() {
        return new Session();
    }

    @Provides @Singleton DynamicEndpoint provideDynamicEndpoint() {
        return new DynamicEndpoint();
    }

    @Provides @Singleton RequestInterceptor provideRequestInterceptor(Session session) {
        return request -> {
            request.addQueryParam("format", "json");
            if (session.getCookie() != null) {
                request.addHeader("Cookie", "pwg_id=" + session.getCookie());
            }
        };
    }

    @Provides @Singleton Gson provideGson() {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }

    @Provides @Singleton RestAdapter provideRestAdapter(OkHttpClient client, DynamicEndpoint endpoint, RequestInterceptor interceptor, Gson gson) {
        return new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setLogLevel(BuildConfig.DEBUG ? FULL : NONE)
                .setEndpoint(endpoint)
                .setRequestInterceptor(interceptor)
                .setConverter(new GsonConverter(gson))
                .build();
    }

    @Provides @Singleton RestService provideRestService(RestAdapter restAdapter) {
        return restAdapter.create(RestService.class);
    }

    @Provides @Singleton @Named("IoScheduler") Scheduler provideIoScheduler() {
        return Schedulers.io();
    }

    @Provides @Singleton @Named("UiScheduler") Scheduler provideUiScheduler() {
        return AndroidSchedulers.mainThread();
    }

}