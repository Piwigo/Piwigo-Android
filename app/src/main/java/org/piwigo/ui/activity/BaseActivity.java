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

package org.piwigo.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.piwigo.PiwigoApplication;
import org.piwigo.helper.AccountHelper;
import org.piwigo.io.repository.PreferencesRepository;
import org.piwigo.internal.di.component.ActivityComponent;
import org.piwigo.internal.di.component.ApplicationComponent;
import org.piwigo.internal.di.component.DaggerActivityComponent;
import org.piwigo.internal.di.module.ActivityModule;
import org.piwigo.ui.viewmodel.ViewModel;

import javax.inject.Inject;

public abstract class BaseActivity extends AppCompatActivity {

    private ActivityComponent activityComponent;
    private ViewModel viewModel;

    @Inject AccountHelper accountHelper;
    @Inject PreferencesRepository preferencesRepository;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationComponent().inject(this);
        initializeInjector();
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

    public void setActivityComponent(ActivityComponent activityComponent) {
        this.activityComponent = activityComponent;
    }

    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    protected ApplicationComponent getApplicationComponent() {
        return ((PiwigoApplication) getApplication()).getApplicationComponent();
    }

    protected void bindLifecycleEvents(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    private void initializeInjector() {
        ActivityComponent activityComponent = DaggerActivityComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(new ActivityModule(this))
                .build();
        setActivityComponent(activityComponent);
    }

    private boolean hasViewModel() {
        return viewModel != null;
    }

}
