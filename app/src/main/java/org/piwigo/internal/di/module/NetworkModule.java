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

import com.jakewharton.picasso.OkHttp3Downloader;

import org.piwigo.BuildConfig;
import org.piwigo.internal.di.qualifier.ForPicasso;
import org.piwigo.internal.di.qualifier.ForRetrofit;
import org.piwigo.io.Session;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
public class NetworkModule {


    @Provides @Singleton HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.NONE : HttpLoggingInterceptor.Level.NONE);
        return loggingInterceptor;
    }

    @Provides @Singleton @ForRetrofit OkHttpClient provideRetrofitOkHttpClient(HttpLoggingInterceptor loggingInterceptor, Session session) {
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder();

                    HttpUrl.Builder urlBuilder = chain.request().url().newBuilder();
                    urlBuilder.addQueryParameter("format", "json");
                    builder.url(urlBuilder.build());

                    if (session.getCookie() != null) {
                        builder.addHeader("Cookie", "pwg_id=" + session.getCookie());
                    }

                    return chain.proceed(builder.build());
                })
                .build();
    }

    @Provides @Singleton @ForPicasso OkHttpClient providePicassoOkHttpClient(HttpLoggingInterceptor loggingInterceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
    }

    @Provides @Singleton OkHttp3Downloader provideOkHttp3Downloader(@ForPicasso OkHttpClient client) {
        return new OkHttp3Downloader(client);
    }
}
