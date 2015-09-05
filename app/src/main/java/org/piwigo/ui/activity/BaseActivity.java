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
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.piwigo.PiwigoApplication;
import org.piwigo.R;
import org.piwigo.internal.di.component.ApplicationComponent;
import org.piwigo.internal.di.module.ActivityModule;

import static android.support.v4.view.GravityCompat.START;

public abstract class BaseActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationComponent().inject(this);
    }

    @Override public void onBackPressed() {
        if (hasDrawer() && getDrawerLayout().isDrawerOpen(START)) {
            getDrawerLayout().closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    protected ApplicationComponent getApplicationComponent() {
        return ((PiwigoApplication) getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }

    protected void setToolbar(Toolbar toolbar) {
        this.toolbar = toolbar;
        setSupportActionBar(toolbar);
    }

    protected boolean hasToolbar() {
        return toolbar != null;
    }

    protected Toolbar getToolbar() {
        return toolbar;
    }

    protected boolean hasActionBar() {
        return getSupportActionBar() != null;
    }

    protected void setDrawerComponents(DrawerLayout drawerLayout, NavigationView navigationView) {
        this.drawerLayout = drawerLayout;
        this.navigationView = navigationView;
        setUpNavigationDrawer();
    }

    protected boolean hasDrawer() {
        return drawerLayout != null && navigationView != null;
    }

    protected DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    protected NavigationView getNavigationView() {
        return navigationView;
    }

    private void setUpNavigationDrawer() {
        if (hasDrawer()) {
            if (hasActionBar()) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            if (hasToolbar()) {
                getToolbar().setNavigationIcon(R.drawable.ic_action_menu);
                getToolbar().setNavigationOnClickListener(v -> drawerLayout.openDrawer(START));
            }
        }
    }

}
