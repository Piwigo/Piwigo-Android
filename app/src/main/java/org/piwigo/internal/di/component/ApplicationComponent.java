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

package org.piwigo.internal.di.component;

import android.content.Context;

import com.google.gson.Gson;

import org.piwigo.internal.di.module.ApiModule;
import org.piwigo.internal.di.module.ApplicationModule;
import org.piwigo.internal.di.module.NetworkModule;
import org.piwigo.io.DynamicEndpoint;
import org.piwigo.io.RestService;
import org.piwigo.io.Session;
import org.piwigo.ui.activity.BaseActivity;
import org.piwigo.ui.activity.LauncherActivity;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import rx.Scheduler;

@Singleton
@Component(modules = {ApplicationModule.class, NetworkModule.class, ApiModule.class})
public interface ApplicationComponent {

    void inject(BaseActivity activity);

    void inject(LauncherActivity activity);

    Context context();

    Session session();

    DynamicEndpoint endpoint();

    Gson gson();

    RestService restService();

    @Named("IoScheduler") Scheduler ioScheduler();

    @Named("UiScheduler") Scheduler uiScheduler();

}
