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

    void setUsername(String username) {
        this.username.set(username);
    }

    void setUrl(String url) {
        this.url.set(url);
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
