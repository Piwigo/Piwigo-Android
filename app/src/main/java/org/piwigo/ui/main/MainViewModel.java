/*
 * Copyright 2017 Phil Bayfield https://philio.me
 * Copyright 2017 Piwigo Team http://piwigo.org
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

package org.piwigo.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableField;
import android.support.design.widget.NavigationView;
import android.view.MenuItem;
import android.view.View;

import org.piwigo.R;
import org.piwigo.internal.binding.observable.DrawerStateObservable;
import org.piwigo.internal.binding.observable.NavigationItemObservable;
import org.piwigo.ui.model.User;

public class MainViewModel extends ViewModel {

    public ObservableField<String> title = new ObservableField<>();
    public ObservableField<String> username = new ObservableField<>();
    public ObservableField<String> url = new ObservableField<>();
    public DrawerStateObservable drawerState = new DrawerStateObservable(false);
    public NavigationItemObservable navigationItem = new NavigationItemObservable(R.id.nav_albums);
    public NavigationItemSelectedListener navigationListener = new NavigationItemSelectedListener();

    private MutableLiveData<MenuItem> selectedMenuItem = new MutableLiveData<>();

    public LiveData<MenuItem> getSelectedMenuItem() {
        return selectedMenuItem;
    }

    void setTitle(String title) {
        this.title.set(title);
    }

    void setUser(User user) {
        username.set(user.username);
        url.set(user.url);
    }

    public void navigationIconClick(View view) {
        drawerState.set(true);
    }

    public class NavigationItemSelectedListener implements NavigationView.OnNavigationItemSelectedListener {

        @Override public boolean onNavigationItemSelected(MenuItem item) {
            selectedMenuItem.setValue(item);
            drawerState.set(false);
            return true;
        }
    }
}
