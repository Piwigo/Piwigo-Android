package org.piwigo.internal.di.module;

import android.accounts.AccountManager;
import android.content.Context;

import org.piwigo.PiwigoApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private final PiwigoApplication application;

    public ApplicationModule(PiwigoApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return application;
    }

    @Provides
    @Singleton
    AccountManager provideAccountManager() {
        return AccountManager.get(application);
    }

}
