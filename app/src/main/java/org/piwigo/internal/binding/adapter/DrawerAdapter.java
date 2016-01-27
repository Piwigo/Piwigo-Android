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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import org.piwigo.internal.binding.observable.DrawerStateObservable;

public class DrawerAdapter {

    @BindingAdapter("bind:state") public static void bindState(DrawerLayout drawerLayout, DrawerStateObservable observable) {
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
