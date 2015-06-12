package org.piwigo.internal.di.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import org.piwigo.BuildConfig;
import org.piwigo.io.RestService;
import org.piwigo.manager.SessionManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

import static retrofit.RestAdapter.LogLevel.FULL;
import static retrofit.RestAdapter.LogLevel.NONE;

@Module
public class ApiModule {

    private String url;

    public ApiModule(String url) {
        this.url = url;
    }

    @Provides
    @Singleton
    SessionManager provideSessionManager() {
        return new SessionManager();
    }

    @Provides
    @Singleton
    RequestInterceptor provideRequestInterceptor(final SessionManager sessionManager) {
        return new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addQueryParam("format", "json");
                if (sessionManager.getSessionCookie() != null) {
                    request.addHeader("Cookie", "pwg_id=" + sessionManager.getSessionCookie());
                }
            }
        };
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new GsonBuilder().create();
    }

    @Provides
    @Singleton
    RestAdapter provideRestAdapter(OkHttpClient client, RequestInterceptor interceptor, Gson gson) {
        return new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setLogLevel(BuildConfig.DEBUG ? FULL : NONE)
                .setEndpoint(url)
                .setRequestInterceptor(interceptor)
                .setConverter(new GsonConverter(gson))
                .build();
    }

    @Provides
    @Singleton
    RestService provideRestService(RestAdapter restAdapter) {
        return restAdapter.create(RestService.class);
    }

}