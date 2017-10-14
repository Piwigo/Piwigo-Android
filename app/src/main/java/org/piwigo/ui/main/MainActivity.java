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
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import org.piwigo.R;
import org.piwigo.databinding.ActivityMainBinding;
import org.piwigo.databinding.DrawerHeaderBinding;
import org.piwigo.helper.AccountHelper;
import org.piwigo.ui.about.AboutActivity;
import org.piwigo.ui.accounts.ManageAccountsActivity;
import org.piwigo.ui.model.User;
import org.piwigo.ui.shared.BaseActivity;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class MainActivity extends BaseActivity implements HasSupportFragmentInjector, Observer{

    @Inject DispatchingAndroidInjector<Fragment> fragmentInjector;

    private MainViewModel viewModel;

    private Account account;

    private Spinner accountSpinner;

    @Override protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        DrawerHeaderBinding headerBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.drawer_header, binding.navigationView, false);

        accountSpinner = (Spinner) headerBinding.accountSpinner;
        List<User> users = accountHelper.getUsers();
        AccountSelectionSpinnerAdapter adapter = new AccountSelectionSpinnerAdapter(this,
                R.layout.account_selection_spinner_item, R.id.aspin_username, users);

        accountSpinner.setAdapter(adapter);

        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(account != users.get(position).account) {
                    accountHelper.setAccount(users.get(position).account);
                }
                viewModel.drawerState.set(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                /* Nothing todo if nothing changes */
            }

        });

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getSelectedMenuItem().observe(this, this::itemSelected);

        binding.setViewModel(viewModel);
        headerBinding.setViewModel(viewModel);
        binding.navigationView.addHeaderView(headerBinding.getRoot());
        setSupportActionBar(binding.toolbar);

        accountHelper.addObserver(this);
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
            case R.id.nav_manage_accounts:
                Intent intent = new Intent(this, ManageAccountsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_about:
                Intent intentabout = new Intent(this, AboutActivity.class);
                startActivity(intentabout);
                break;
            case R.id.nav_albums:
                /* TODO: implement Albums */
            case R.id.nav_settings:
                /* TODO: implement settings */
            case R.id.nav_upload:
                /* TODO: implement upload */
                Context context = getApplicationContext();
                CharSequence text = item.getTitle() + " not yet implemented.";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
        }

    }

    private void checkAccount() {
        if (account != null) {
            account = accountHelper.getAccount(account.name, false);
        }
    }

    private void loadAccount() {
        String name = preferencesRepository.getAccountName();
        User user = accountHelper.getUser(name, true);
        if (user == null) {
            finish();
            return;
        }
        account = user.account;
        accountHelper.setAccount(account);
        if (!account.name.equals(name)) {
            preferencesRepository.setAccountName(account.name);
        }

        viewModel.setUser(user);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof AccountHelper) {
            if(data instanceof User) {
                /* now also update the content */
                if (account != accountHelper.getUser().account) {
                    AlbumsFragment frag = (AlbumsFragment) getSupportFragmentManager().findFragmentById(R.id.content);
                    account = accountHelper.getUser().account;
                    if(frag != null) {
                        frag.refresh();
                    }
                }
            }else{
                AccountSelectionSpinnerAdapter adapter = new AccountSelectionSpinnerAdapter(this,
                        R.layout.account_selection_spinner_item, R.id.aspin_username, accountHelper.getUsers());

                accountSpinner.setAdapter(adapter);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        accountHelper.deleteObserver(this);
    }
}
