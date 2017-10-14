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
import android.support.design.widget.NavigationView;

import org.piwigo.internal.binding.observable.NavigationItemObservable;

public class NavigationAdapter {

    @BindingAdapter({"item", "itemSelectedListener"}) public static void bindItem(NavigationView navigationView, NavigationItemObservable observable, NavigationView.OnNavigationItemSelectedListener listener) {
        boolean bound = navigationView.getTag() != null && (boolean) navigationView.getTag();
        if (!bound) {
            navigationView.setNavigationItemSelectedListener(item -> {
                listener.onNavigationItemSelected(item);
                if (item.isCheckable()) {
                    observable.set(item.getItemId());
                }
                return true;
            });
            navigationView.setTag(true);
        }

        if (observable.get() == 0) {
            observable.set(navigationView.getMenu().getItem(0).getItemId());
        } else {
            navigationView.getMenu().findItem(observable.get()).setChecked(true);
        }
    }

}
