/*
 * Piwigo for Android
 * Copyright (C) 2017 Raphael Mack http://www.raphael-mack.de
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
package org.piwigo.ui.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Bundle;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.piwigo.BuildConfig;
import org.piwigo.R;
import org.piwigo.databinding.ActivityMainBinding;
import org.piwigo.databinding.ActivityManageAccountsBinding;
import org.piwigo.helper.AccountHelper;
import org.piwigo.ui.model.User;
import org.piwigo.ui.view.AccountView;
import org.piwigo.ui.viewmodel.ManageAccountsViewModel;

import java.util.List;

import javax.inject.Inject;

public class ManageAccountsActivity extends BaseActivity implements AccountView {

    @Inject ManageAccountsViewModel viewModel;
    ActivityManageAccountsBinding binding;

    private ListView userListView;

    private int currentSelectedPosition = -1;

    private UserAdapter mUserAdapter;


    private class UserAdapter extends ArrayAdapter<User>{
        public UserAdapter(List<User> objects) {
            super(ManageAccountsActivity.this, 0, objects);
        }
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            final User user = getItem(position);
            if (view == null) {
                LayoutInflater inflater = ManageAccountsActivity.this.getLayoutInflater();
                view = inflater.inflate(R.layout.account_row, parent, false);
            }
            TextView username = (TextView) view.findViewById(R.id.username);
            username.setText(user.username);
            if(user.guest) {
                username.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
            }else{
                username.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            }

            TextView galleryUrlView = (TextView) view.findViewById(R.id.gallery_url);
            galleryUrlView.setText(user.url);

            ImageView imageView = (ImageView) view.findViewById(R.id.account_image);
/* TODO fill image here (from favicon of gallery / User) */

            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityComponent().inject(this);

        DataBindingUtil.setContentView(this, R.layout.activity_manage_accounts);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_accounts);

        bindLifecycleEvents(viewModel);
        binding.setViewModel(viewModel);
        viewModel.title.set(getString(R.string.nav_manage_accounts));
        viewModel.setView(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.account_toolbar);
        setSupportActionBar(toolbar);

        userListView = (ListView) findViewById(R.id.user_list);
        this.mUserAdapter = new UserAdapter(viewModel.users);
        userListView.setAdapter(this.mUserAdapter);

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            Resources res = ManageAccountsActivity.this.getResources();

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                if(currentSelectedPosition == position) {
                    currentSelectedPosition = -1;
                    view.setSelected(false);
                }else {
                    currentSelectedPosition = position;
                    view.setSelected(true);
                }
            }
        });

        userListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long arg) {
                Intent intent = new Intent(getApplicationContext(),
                        LoginActivity.class);
                intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUserAdapter.getItem(position).account.name);
                startActivity(intent);
                return true;
            }
        });
        registerForContextMenu(userListView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage_accounts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_account:
                startActivity(new Intent(getApplicationContext(),
                        LoginActivity.class));
                break;
            case R.id.action_del_account:
                if(currentSelectedPosition == -1)
                {
                    /* nothing selected */
                    Snackbar.make(binding.getRoot(), R.string.account_not_selected, Snackbar.LENGTH_LONG)
                            .show();
                }else{
                    /* TODO delete account */
                    accountHelper.removeAccount(mUserAdapter.getItem(currentSelectedPosition).account);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void close() {
        setResult(Activity.RESULT_OK);
        finish();
    }
}
