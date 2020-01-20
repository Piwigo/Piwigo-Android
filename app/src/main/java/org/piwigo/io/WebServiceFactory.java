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

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.piwigo.BuildConfig;
import org.piwigo.accounts.UserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebServiceFactory {

    public class SessionCookieJar implements CookieJar {

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            for (Cookie cookie : cookies) {
                if (cookie.name().equals("pwg_id"))
                    userManager.setSessionCookie(cookie);
            }
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            ArrayList<Cookie> cookies = new ArrayList<Cookie>();
            Cookie cookie = userManager.sessionCookie();
            if (cookie != null)
                cookies.add(cookie);
            return cookies;
        }
    }

    private final HttpLoggingInterceptor loggingInterceptor;
    private final Gson gson;
    private final UserManager userManager;

    private RestService lastRestService;
    private Account lastAccount;

    public WebServiceFactory(HttpLoggingInterceptor loggingInterceptor, Gson gson, UserManager userManager) {
        this.loggingInterceptor = loggingInterceptor;
        this.gson = gson;
        this.userManager = userManager;
    }

    /* only intended for login, for most use cases consider create */
    public RestService createForUrl(String url) {
        OkHttpClient client = buildOkHttpClient(true, null);
        Retrofit retrofit = buildRetrofit(client, url);
        return retrofit.create(RestService.class);
    }

    public synchronized RestService create() {
        Account account = userManager.getActiveAccount().getValue();
        if (account != null) {
            OkHttpClient client = buildOkHttpClient(true, null);
            Retrofit retrofit = buildRetrofit(client, userManager.getSiteUrl(account));
            lastRestService = retrofit.create(RestService.class);
            lastAccount = account;
        }
        return lastRestService;
    }

    private OkHttpClient buildOkHttpClient(boolean queryJson, @Nullable Map<String, String> addHeaders) {
        return new OkHttpClient.Builder()
                .cookieJar(new SessionCookieJar())
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder();

                    HttpUrl.Builder urlBuilder = chain.request().url().newBuilder();
                    if(queryJson) {
                        urlBuilder.addQueryParameter("format", "json");
                    }
                    builder.url(urlBuilder.build());

                    if(addHeaders != null) {
                        for (String name : addHeaders.keySet()) {
                            builder.addHeader(name, addHeaders.get(name));
                        }
                    }

                    builder.header("User-Agent", ("Piwigo-Android " + BuildConfig.VERSION_NAME));
                    return chain.proceed(builder.build());
                })
                .build();
    }

    public DownloadService downloaderForAccount(Account account, @Nullable Map<String, String> addHeaders) {
        OkHttpClient client = buildOkHttpClient(false, addHeaders);
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(userManager.getSiteUrl(account))
                .build();
        return retrofit.create(DownloadService.class);
    }

    private Retrofit buildRetrofit(OkHttpClient client, String baseUrl) {
        return new Retrofit.Builder()
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();

    }
}
