/*
 * Piwigo for Android
 * Copyright (C) 2016-2017 Piwigo Team http://piwigo.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.piwigo.internal.binding.adapter;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;
import com.google.android.material.navigation.NavigationView;
import android.view.Menu;
import android.view.MenuItem;

public class NavigationViewBindingAdapter {

    @InverseBindingAdapter(attribute = "selectedItemId", event = "onNavigationItemSelected") public static int onNavigationItemSelected(NavigationView navigationView) {
        return getSelectedItemId(navigationView);
    }

    @BindingAdapter(value = {"selectedItemId", "onNavigationItemSelected"}, requireAll = false) public static void setNavigationListener(NavigationView navigationView, int selectedItemId, final InverseBindingListener inverseBindingListener) {
        boolean bound = navigationView.getTag() != null && (boolean) navigationView.getTag();
        if (!bound) {
            navigationView.setTag(true);
            navigationView.setNavigationItemSelectedListener(menuItem -> {
                navigationView.setCheckedItem(menuItem.getItemId());
                if (inverseBindingListener != null) {
                    inverseBindingListener.onChange();
                }
                return false;
            });
        }

        if (selectedItemId != getSelectedItemId(navigationView)) {
            navigationView.setCheckedItem(selectedItemId);
        }
    }

    private static int getSelectedItemId(NavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i ++) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.isChecked()) {
                return menuItem.getItemId();
            }
        }
        return -1;
    }
}
