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

package org.piwigo.ui.shared;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.piwigo.helper.AccountHelper;
import org.piwigo.io.repository.PreferencesRepository;

import javax.inject.Inject;

public abstract class BaseActivity extends AppCompatActivity {

    private ViewModel viewModel;

    @Inject protected AccountHelper accountHelper;
    @Inject protected PreferencesRepository preferencesRepository;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        if (hasViewModel()) {
            viewModel.onSaveState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (hasViewModel()) {
            viewModel.onRestoreState(savedInstanceState);
        }
    }

    @Override protected void onDestroy() {
        if (hasViewModel()) {
            viewModel.onDestroy();
            viewModel = null;
        }
        super.onDestroy();
    }

    protected void bindLifecycleEvents(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    private boolean hasViewModel() {
        return viewModel != null;
    }

}
