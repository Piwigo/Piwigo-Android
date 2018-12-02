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

package org.piwigo;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.databinding.DataBindingUtil;

import org.piwigo.accounts.PiwigoAccountAuthenticator;
import org.piwigo.internal.di.component.ApplicationComponent;
import org.piwigo.internal.di.component.BindingComponent;
import org.piwigo.internal.di.component.DaggerApplicationComponent;
import org.piwigo.internal.di.component.DaggerBindingComponent;
import org.piwigo.internal.di.module.ApplicationModule;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;

public class PiwigoApplication extends Application implements HasActivityInjector, HasServiceInjector {

    @Inject DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;
    @Inject DispatchingAndroidInjector<Service> dispatchingAndroidServiceInjector;

    private ApplicationComponent applicationComponent;

    @Override public void onCreate() {
        super.onCreate();

        initializeDependancyInjection();
    }

    @Override public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }

    private void initializeDependancyInjection() {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        applicationComponent.inject(this);

        BindingComponent bindingComponent = DaggerBindingComponent.builder()
                .applicationComponent(applicationComponent)
                .build();
        DataBindingUtil.setDefaultComponent(bindingComponent);
    }

    /**
     * Returns an {@link AndroidInjector} of {@link Service}s.
     */
    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingAndroidServiceInjector;
    }
/*
    public void inject(PiwigoAccountAuthenticator piwigoAccountAuthenticator) {
        applicationComponent.inject(piwigoAccountAuthenticator);
    }
    */
}