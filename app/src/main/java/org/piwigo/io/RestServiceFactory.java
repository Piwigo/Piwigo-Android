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

package org.piwigo.io;

import android.accounts.Account;

import com.google.gson.Gson;

import org.piwigo.PiwigoApplication;
import org.piwigo.accounts.UserManager;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestServiceFactory {

    private final HttpLoggingInterceptor loggingInterceptor;
    private final Gson gson;
    private final UserManager userManager;

    public RestServiceFactory(HttpLoggingInterceptor loggingInterceptor, Gson gson, UserManager userManager) {
        this.loggingInterceptor = loggingInterceptor;
        this.gson = gson;
        this.userManager = userManager;
    }

    public RestService createForUrl(String url) {
        OkHttpClient client = buildOkHttpClient(null);
        Retrofit retrofit = buildRetrofit(client, url);
        return retrofit.create(RestService.class);
    }

    public RestService createForAccount(Account account) {
        OkHttpClient client = buildOkHttpClient(userManager.getCookie(account));
        Retrofit retrofit = buildRetrofit(client, userManager.getSiteUrl(account));
        return retrofit.create(RestService.class);
    }

    private OkHttpClient buildOkHttpClient(String cookie) {
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder();

                    HttpUrl.Builder urlBuilder = chain.request().url().newBuilder();
                    urlBuilder.addQueryParameter("format", "json");
                    builder.url(urlBuilder.build());

                    if (cookie != null) {
                        builder.addHeader("Cookie", "pwg_id=" + cookie);
                    }

                    /* TODO: adjust hardcoded string by resource app name and version */
                    builder.header("User-Agent", "Piwigo-Android");
                    return chain.proceed(builder.build());
                })
                .build();
    }

    private Retrofit buildRetrofit(OkHttpClient client, String baseUrl) {
        return new Retrofit.Builder()
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();
    }
}
