/*
 * Copyright 2016 Phil Bayfield https://philio.me
 * Copyright 2016 Piwigo Team http://piwigo.org
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

package org.piwigo.ui.activity;

import android.os.Bundle;
import android.os.Handler;

import org.piwigo.R;
import org.piwigo.ui.Navigator;

import javax.inject.Inject;

public class LauncherActivity extends BaseActivity {

    @Inject Navigator navigator;

    private final Handler handler = new Handler();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationComponent().inject(this);
        setContentView(R.layout.activity_launcher);
        handler.postDelayed(this::startLogin, 1000);
    }

    private void startLogin() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            navigator.startLogin(this, findViewById(R.id.logo), getString(R.string.logo_transition_name));
        } else {
            navigator.startLogin(this);
        }
    }

}
