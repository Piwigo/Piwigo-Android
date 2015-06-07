package org.piwigo.internal.di.component;

import android.app.Activity;

import org.piwigo.internal.di.PerActivity;
import org.piwigo.internal.di.module.ActivityModule;

import dagger.Component;

@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    Activity activity();

}