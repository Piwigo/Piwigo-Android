/*
 * Copyright 2015 Phil Bayfield https://philio.me
 * Copyright 2015 Piwigo Team http://piwigo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.piwigo;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import org.piwigo.internal.di.component.ApplicationComponent;
import org.piwigo.internal.di.component.DaggerApplicationComponent;
import org.piwigo.internal.di.module.ApplicationModule;

import io.fabric.sdk.android.Fabric;

public class PiwigoApplication extends Application {

    private ApplicationComponent applicationComponent;

    @Override public void onCreate() {
        super.onCreate();
        initializeCrashlytics();
        initializeInjector();
    }

    protected void initializeCrashlytics() {
        Fabric.with(this, new Crashlytics());
    }

    private void initializeInjector() {
        ApplicationComponent applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        setApplicationComponent(applicationComponent);
    }

    public void setApplicationComponent(ApplicationComponent applicationComponent) {
        this.applicationComponent = applicationComponent;
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

}
