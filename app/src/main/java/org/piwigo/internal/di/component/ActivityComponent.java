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

import android.app.Activity;

import org.piwigo.internal.di.module.ActivityModule;
import org.piwigo.internal.di.scope.PerActivity;
import org.piwigo.ui.activity.LoginActivity;
import org.piwigo.ui.activity.MainActivity;
import org.piwigo.ui.activity.ManageAccountsActivity;
import org.piwigo.ui.fragment.AlbumsFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(LoginActivity activity);

    void inject(MainActivity activity);

    void inject(ManageAccountsActivity activity);

    void inject(AlbumsFragment fragment);

    Activity activity();

}