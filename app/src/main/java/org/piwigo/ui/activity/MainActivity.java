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

import android.accounts.Account;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.MenuItem;

import org.piwigo.R;
import org.piwigo.databinding.ActivityMainBinding;
import org.piwigo.databinding.DrawerHeaderBinding;
import org.piwigo.ui.fragment.AlbumsFragment;
import org.piwigo.ui.model.User;
import org.piwigo.ui.view.MainView;
import org.piwigo.ui.viewmodel.MainViewModel;

import javax.inject.Inject;

public class MainActivity extends BaseActivity implements MainView {

    @Inject MainViewModel viewModel;

    private Account account;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityComponent().inject(this);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        DrawerHeaderBinding headerBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.drawer_header, binding.navigationView, false);

        viewModel.setView(this);
        bindLifecycleEvents(viewModel);
        binding.setViewModel(viewModel);
        headerBinding.setViewModel(viewModel);
        binding.navigationView.addHeaderView(headerBinding.getRoot());
        setSupportActionBar(binding.toolbar);

        if (savedInstanceState == null) {
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

    @Override public void onItemSelected(MenuItem item) {
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
