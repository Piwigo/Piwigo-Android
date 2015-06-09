package org.piwigo.internal.di.module;

import com.squareup.okhttp.OkHttpClient;

import org.piwigo.BuildConfig;
import org.piwigo.io.RestService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

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
    RestAdapter provideRestAdapter(OkHttpClient client) {
        return new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setLogLevel(BuildConfig.DEBUG ? FULL : NONE)
                .setEndpoint(url)
                .build();
    }

    @Provides
    @Singleton
    RestService provideRestService(RestAdapter restAdapter) {
        return restAdapter.create(RestService.class);
    }

}