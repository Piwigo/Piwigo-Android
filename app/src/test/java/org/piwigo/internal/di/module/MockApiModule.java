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

import android.accounts.AccountManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import org.piwigo.BuildConfig;
import org.piwigo.io.MockRestService;
import org.piwigo.io.RestService;
import org.piwigo.io.provider.LoginProvider;
import org.piwigo.manager.SessionManager;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.MockRestAdapter;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import static retrofit.RestAdapter.LogLevel.FULL;
import static retrofit.RestAdapter.LogLevel.NONE;

@Module
public class MockApiModule {

    @Provides @Singleton SessionManager provideSessionManager(AccountManager accountManager) {
        return new SessionManager(accountManager);
    }

    @Provides @Singleton RequestInterceptor provideRequestInterceptor(final SessionManager sessionManager) {
        return new RequestInterceptor() {
            @Override public void intercept(RequestFacade request) {
                request.addQueryParam("format", "json");
                if (sessionManager.getCookie() != null) {
                    request.addHeader("Cookie", "pwg_id=" + sessionManager.getCookie());
                }
            }
        };
    }

    @Provides @Singleton Gson provideGson() {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }

    @Provides @Singleton RestAdapter provideRestAdapter(OkHttpClient client, SessionManager sessionManager, RequestInterceptor interceptor, Gson gson) {
        return new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setLogLevel(BuildConfig.DEBUG ? FULL : NONE)
                .setEndpoint(sessionManager.getEndpoint())
                .setRequestInterceptor(interceptor)
                .setConverter(new GsonConverter(gson))
                .build();
    }

    @Provides @Singleton MockRestAdapter provideMockRestAdapter(RestAdapter restAdapter) {
        return MockRestAdapter.from(restAdapter);
    }

    @Provides @Singleton MockRestService provideMockRestService() {
        return new MockRestService();
    }

    @Provides @Singleton RestService provideRestService(MockRestAdapter mockRestAdapter, MockRestService mockRestService) {
        return mockRestAdapter.create(RestService.class, mockRestService);
    }

    @Provides @Singleton @Named("IoScheduler") Scheduler provideIoScheduler() {
        return Schedulers.immediate();
    }

    @Provides @Singleton @Named("UiScheduler") Scheduler provideUiScheduler() {
        return Schedulers.immediate();
    }

    @Provides @Singleton LoginProvider provideLoginProvider(SessionManager sessionManager, RestService restService, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler, Gson gson) {
        return new LoginProvider(sessionManager, restService, ioScheduler, uiScheduler, gson);
    }

}