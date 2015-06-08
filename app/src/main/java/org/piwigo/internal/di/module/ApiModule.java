package org.piwigo.internal.di.module;

import org.piwigo.io.RestService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;

@Module
public class ApiModule {

    @Provides
    @Singleton
    RestAdapter provideRestAdapter() {
        return new RestAdapter.Builder()
                .build();
    }

    @Provides
    @Singleton
    RestService provideRestService(RestAdapter restAdapter) {
        return restAdapter.create(RestService.class);
    }

}