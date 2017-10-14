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

import android.databinding.BindingAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import org.piwigo.internal.binding.observable.DrawerStateObservable;

public class DrawerAdapter {

    @BindingAdapter("state") public static void bindState(DrawerLayout drawerLayout, DrawerStateObservable observable) {
        boolean bound = drawerLayout.getTag() != null && (boolean) drawerLayout.getTag();
        if (!bound) {
            drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {

                @Override
                public void onDrawerOpened(View drawerView) {
                    observable.set(true);
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    observable.set(false);
                }

            });
            drawerLayout.setTag(true);
        }

        if (observable.get()) {
            drawerLayout.openDrawer(GravityCompat.START);
        } else {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

}
