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

import android.accounts.Account;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import org.piwigo.R;
import org.piwigo.databinding.ActivityMainBinding;
import org.piwigo.databinding.DrawerHeaderBinding;
import org.piwigo.ui.model.User;
import org.piwigo.ui.shared.BaseActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class MainActivity extends BaseActivity implements HasSupportFragmentInjector {

    @Inject DispatchingAndroidInjector<Fragment> fragmentInjector;

    private MainViewModel viewModel;
    private Account account;

    @Override protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        DrawerHeaderBinding headerBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.drawer_header, binding.navigationView, false);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getSelectedMenuItem().observe(this, this::itemSelected);

        binding.setViewModel(viewModel);
        headerBinding.setViewModel(viewModel);
        binding.navigationView.addHeaderView(headerBinding.getRoot());
        setSupportActionBar(binding.toolbar);

        loadAccount();

        if (savedInstanceState == null) {
            viewModel.setTitle(getString(R.string.nav_albums));
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content, new AlbumsFragment())
                    .commit();
        }
    }

    @Override protected void onResume() {
        super.onResume();
        checkAccount();
        if (account == null) {
            loadAccount();
        }
    }

    @Override public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentInjector;
    }

    private void itemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_albums:
                break;
        }
    }

    private void checkAccount() {
        if (account != null) {
            account = accountHelper.getAccount(account.name, false);
        }
    }

    private void loadAccount() {
        String name = preferencesRepository.getAccountName();
        account = accountHelper.getAccount(name, true);
        if (account == null) {
            finish();
            return;
        }
        if (!account.name.equals(name)) {
            preferencesRepository.setAccountName(account.name);
        }
        User user = accountHelper.createUser(account);
        viewModel.setUser(user);
    }
}
