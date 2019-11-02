/*
 * Piwigo for Android
 * Copyright (C) 2016-2018 Piwigo Team http://piwigo.org
 * Copyright (C) 2018-2018 Raphael Mack http://www.raphael-mack.de
 *
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
package org.piwigo.ui.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.piwigo.R;
import org.piwigo.databinding.ActivityManageAccountsBinding;
import org.piwigo.ui.login.LoginActivity;
import org.piwigo.ui.shared.BaseActivity;
import org.piwigo.ui.shared.BindingRecyclerViewAdapter;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.DispatchingAndroidInjector;

public class ManageAccountsActivity extends BaseActivity implements OnAccountsUpdateListener {

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentInjector;

    @Inject
    ManageAccountsViewModelFactory viewModelFactory;

    @Inject AccountManager accountManager;

    private ManageAccountsViewModel viewModel;

    /**
     * This invoked when the AccountManager starts up and whenever the account
     * set changes.
     *
     * @param accounts the current accounts
     */
    @Override
    public void onAccountsUpdated(Account[] accounts) {
        userManager.refreshAccounts();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        ActivityManageAccountsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_accounts);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ManageAccountsViewModel.class);

        binding.setViewModel(viewModel);
        Toolbar toolbar = findViewById(R.id.account_toolbar);
        setSupportActionBar(toolbar);
        viewModel.title.set(getString(R.string.title_activity_accounts));

        userManager.getAccounts().observe(this, newItems -> {
            BindingRecyclerViewAdapter<Account> a = (BindingRecyclerViewAdapter<Account>) binding.accountRecycler.getAdapter();

            if (a != null) {
                a.update(newItems);
            }
        });
        userManager.getActiveAccount().observe(this, account -> {
            BindingRecyclerViewAdapter<Account> a = (BindingRecyclerViewAdapter<Account>) binding.accountRecycler.getAdapter();

            if (a != null) {
                a.notifyDataSetChanged();
            }
        });

        viewModel.selectedAccount.observe(this, account -> {
            BindingRecyclerViewAdapter<Account> a = (BindingRecyclerViewAdapter<Account>) binding.accountRecycler.getAdapter();

            if (a != null) {
                finish();
            }
        });

        binding.accountRecycler.setLayoutManager(new LinearLayoutManager(this));

        accountManager.addOnAccountsUpdatedListener(this, null, true);
    }

    public void onDestroy() {
        /* cleanup the account update listener */
        accountManager.removeOnAccountsUpdatedListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage_accounts, menu);
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_account:
                startActivity(new Intent(getApplicationContext(),
                        LoginActivity.class));
                break;
            case R.id.action_edit_account:
                Intent editIntent = new Intent(getApplicationContext(),
                        LoginActivity.class);
                editIntent.setAction(LoginActivity.EDIT_ACCOUNT_ACTION);
                editIntent.putExtra("account", viewModel.getSelectedAccount());
                startActivity(editIntent);
                break;
            case R.id.action_del_account:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        accountManager.removeAccount(userManager.getActiveAccount().getValue(), this, future -> userManager.refreshAccounts(), null);
                    } else {
                        accountManager.removeAccount(userManager.getActiveAccount().getValue(), future -> userManager.refreshAccounts(), null);
                    }
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
