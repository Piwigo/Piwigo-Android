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
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.piwigo.PiwigoApplication;
import org.piwigo.R;
import org.piwigo.internal.di.component.ApplicationComponent;
import org.piwigo.internal.di.module.ActivityModule;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.support.v4.view.GravityCompat.START;

public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Inject optional navigation widgets
     */
    @Bind(R.id.toolbar) @Nullable Toolbar toolbar;
    @Bind(R.id.drawer_layout) @Nullable DrawerLayout drawerLayout;
    @Bind(R.id.navigation_view) @Nullable NavigationView navigationView;
    @Bind(R.id.content) @Nullable FrameLayout contentView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationComponent().inject(this);
    }

    @Override public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        onContentViewSet();
    }

    @Override public void setContentView(View view) {
        super.setContentView(view);
        onContentViewSet();
    }

    @Override public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        onContentViewSet();
    }

    @Override public void onBackPressed() {
        if (hasDrawer() && getDrawerLayout().isDrawerOpen(START)) {
            getDrawerLayout().closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * After child has called setContentView, inject the navigation widgets and configure them
     */
    private void onContentViewSet() {
        ButterKnife.bind(this);
        setUpToolbar();
        setUpNavigationDrawer();
    }

    private void setUpToolbar() {
        if (hasToolbar()) {
            setSupportActionBar(toolbar);
        }
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

    protected ApplicationComponent getApplicationComponent() {
        return ((PiwigoApplication) getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }

    protected boolean hasActionBar() {
        return getSupportActionBar() != null;
    }

    protected boolean hasToolbar() {
        return toolbar != null;
    }

    protected Toolbar getToolbar() {
        return toolbar;
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

}
