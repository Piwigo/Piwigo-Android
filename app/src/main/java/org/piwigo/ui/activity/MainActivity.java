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
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.piwigo.R;
import org.piwigo.databinding.ActivityMainBinding;
import org.piwigo.databinding.DrawerHeaderBinding;
import org.piwigo.helper.AccountHelper;
import org.piwigo.ui.adapter.AccountSelectionSpinnerAdapter;
import org.piwigo.ui.fragment.AlbumsFragment;
import org.piwigo.ui.model.User;
import org.piwigo.ui.view.MainView;
import org.piwigo.ui.viewmodel.MainViewModel;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.inject.Inject;

public class MainActivity extends BaseActivity implements MainView,Observer{

    @Inject MainViewModel viewModel;

    /* TODO check what account is used for */
    private Account account;

    private Spinner accountSpinner;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityComponent().inject(this);

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
                Toast.makeText(getApplicationContext(), users.get(position).username + "@" + users.get(position).url + " was selected (" + position + ")", Toast.LENGTH_LONG).show();
                accountHelper.setAccount(users.get(position).account);
                ((AlbumsFragment)(getSupportFragmentManager()
                    .findFragmentById(R.id.content))).refresh();
                viewModel.drawerState.set(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

                Toast.makeText(getApplicationContext(), "<nothing> was selected", Toast.LENGTH_LONG).show();
            }

        });

        viewModel.setView(this);
        bindLifecycleEvents(viewModel);
        binding.setViewModel(viewModel);
        headerBinding.setViewModel(viewModel);
        binding.navigationView.addHeaderView(headerBinding.getRoot());
        setSupportActionBar(binding.toolbar);

        accountHelper.addObserver(this);

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

    /* handle menu entries */
    @Override public void onItemSelected(MenuItem item) {
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
        if (!account.name.equals(name)) {
            preferencesRepository.setAccountName(account.name);
        }

        viewModel.setUser(user);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof AccountHelper) {
            AccountSelectionSpinnerAdapter adapter = new AccountSelectionSpinnerAdapter(this,
                    R.layout.account_selection_spinner_item, R.id.aspin_username, accountHelper.getUsers());

            accountSpinner.setAdapter(adapter);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        accountHelper.deleteObserver(this);
    }
}
