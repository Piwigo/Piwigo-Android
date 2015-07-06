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

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.piwigo.R;
import org.piwigo.databinding.DrawerHeaderBinding;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getName();

    private static final String STATE_VISIBLE_GROUP = "visible_group";
    private static final String STATE_SELECTED_ITEM = "selected_item";

    private int visibleGroup;
    private int selectedItem;

    DrawerHeaderBinding headerBinding;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpDrawer(savedInstanceState);
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_VISIBLE_GROUP, visibleGroup);
        outState.putInt(STATE_SELECTED_ITEM, selectedItem);
    }

    private void setUpDrawer(Bundle savedInstanceState) {
        // Inflate the drawer header and add click listener
        headerBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.drawer_header, getNavigationView(), false);
        headerBinding.drawerHeader.setOnClickListener(v -> swapDrawerMenu());
        getNavigationView().addHeaderView(headerBinding.getRoot());

        // Handle nav selections
        addNavListener();

        // Set initial state
        if (savedInstanceState != null) {
            visibleGroup = savedInstanceState.getInt(STATE_VISIBLE_GROUP);
            selectedItem = savedInstanceState.getInt(STATE_SELECTED_ITEM);
        } else {
            visibleGroup = R.id.nav_group_features;
            selectedItem = getNavigationView().getMenu().getItem(0).getItemId();
        }
        findMenuItem(selectedItem).setChecked(true);
        if (visibleGroup != R.id.nav_group_features) {
            swapDrawerMenu();
        }

        // Make sure that menu is reset when drawer is closed
        getDrawerLayout().setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                if (!findMenuItem(selectedItem).isVisible()) {
                    swapDrawerMenu();
                }
            }
        });
    }

    private void addNavListener() {
        getNavigationView().setNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getGroupId() == R.id.nav_group_features) {
                selectedItem = menuItem.getItemId();
                menuItem.setChecked(true);
            }

            switch (menuItem.getItemId()) {
                case R.id.nav_albums:
                    break;
                case R.id.nav_upload:
                    break;
                case R.id.nav_settings:
                    break;
                case R.id.nav_add_account:
                    break;
                case R.id.nav_manage_accounts:
                    break;
            }

            getDrawerLayout().closeDrawers();
            return true;
        });
    }

    private void swapDrawerMenu() {
        Menu menu = getNavigationView().getMenu();
        if (findMenuItem(selectedItem).isVisible()) {
            menu.setGroupVisible(R.id.nav_group_features, false);
            menu.setGroupVisible(R.id.nav_group_settings, false);
            menu.setGroupVisible(R.id.nav_group_accounts, true);
            headerBinding.arrow.setImageResource(R.drawable.ic_action_arrow_drop_up);
            visibleGroup = R.id.nav_group_accounts;
        } else {
            menu.setGroupVisible(R.id.nav_group_features, true);
            menu.setGroupVisible(R.id.nav_group_settings, true);
            menu.setGroupVisible(R.id.nav_group_accounts, false);
            headerBinding.arrow.setImageResource(R.drawable.ic_action_arrow_drop_down);
            visibleGroup = R.id.nav_group_features;
        }
    }

    private MenuItem findMenuItem(int item) {
        return getNavigationView().getMenu().findItem(item);
    }

}
