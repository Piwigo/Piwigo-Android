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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.View;

public class DrawerLayoutBindingAdapter {

    @InverseBindingAdapter(attribute = "state", event = "onDrawerStateChanged") public static boolean onDrawerStateChanged(DrawerLayout drawerLayout) {
        return drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    @BindingAdapter(value = {"state", "onDrawerStateChanged"}, requireAll = false) public static void setDrawerListener(DrawerLayout drawerLayout, boolean state, final InverseBindingListener inverseBindingListener) {
        boolean bound = drawerLayout.getTag() != null && (boolean) drawerLayout.getTag();
        if (!bound && inverseBindingListener != null) {
            drawerLayout.setTag(true);
            drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {

                @Override public void onDrawerOpened(View drawerView) {
                    inverseBindingListener.onChange();
                }

                @Override public void onDrawerClosed(View drawerView) {
                    inverseBindingListener.onChange();
                }
            });
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START) && !state) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (!drawerLayout.isDrawerOpen(GravityCompat.START) && state) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }
}
