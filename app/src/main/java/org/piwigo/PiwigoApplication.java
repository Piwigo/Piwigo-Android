package org.piwigo;

import android.app.Application;

import org.piwigo.internal.di.component.ApplicationComponent;
import org.piwigo.internal.di.component.DaggerApplicationComponent;
import org.piwigo.internal.di.module.ApplicationModule;

public class PiwigoApplication extends Application {

    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeInjector();
    }

    private void initializeInjector() {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

}
