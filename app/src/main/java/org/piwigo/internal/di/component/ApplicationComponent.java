package org.piwigo.internal.di.component;


import android.content.Context;

import org.piwigo.internal.di.module.ApplicationModule;
import org.piwigo.ui.activity.BaseActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(BaseActivity baseActivity);

    Context applicationContext();

}
