/*
 * Copyright 2016 Phil Bayfield https://philio.me
 * Copyright 2016 Piwigo Team http://piwigo.org
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

package org.piwigo.internal.binding.adapter;

import android.databinding.BindingAdapter;
import android.support.design.widget.NavigationView;

import org.piwigo.internal.binding.observable.NavigationItemObservable;

public class NavigationAdapter {

    @BindingAdapter({"bind:item", "bind:itemSelectedListener"}) public static void bindItem(NavigationView navigationView, NavigationItemObservable observable, NavigationView.OnNavigationItemSelectedListener listener) {
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
