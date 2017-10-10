/*
 * Copyright 2017 Phil Bayfield https://philio.me
 * Copyright 2017 Piwigo Team http://piwigo.org
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

package org.piwigo.ui.launcher;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;

import org.piwigo.R;
import org.piwigo.databinding.ActivityLauncherBinding;
import org.piwigo.ui.shared.Navigator;
import org.piwigo.ui.shared.BaseActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class LauncherActivity extends BaseActivity {

    @Inject Navigator navigator;

    private final Handler handler = new Handler();
    private ActivityLauncherBinding binding;

    @Override protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_launcher);

        if (accountHelper.hasAccount()) {
            handler.postDelayed(this::startMain, 1000);
        } else {
            handler.postDelayed(this::startLogin, 1000);
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Navigator.REQUEST_CODE_LOGIN && resultCode == RESULT_OK) {
            handler.postDelayed(this::startMain, 1000);
        } else {
            finish();
        }
    }

    private void startMain() {
        navigator.startMain(this);
        finish();
    }

    private void startLogin() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            navigator.startLogin(this, binding.logo, getString(R.string.logo_transition_name));
        } else {
            navigator.startLogin(this);
        }
    }
}
