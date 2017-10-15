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
package org.piwigo.ui.accounts;

import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.graphics.Typeface;
import android.os.Bundle;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
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

import org.piwigo.R;
import org.piwigo.databinding.ActivityManageAccountsBinding;
import org.piwigo.ui.login.LoginActivity;
import org.piwigo.ui.model.User;
import org.piwigo.ui.shared.BaseActivity;
import org.piwigo.ui.view.AccountView;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class ManageAccountsActivity extends BaseActivity implements AccountView {
    @Inject ManageAccountsViewModelFactory viewModelFactory;

    /* TODO: cleanup activity:
     *   selection by tap is crazy, tap shall directly open the LoginView
     *   LongClick could delete after confirmation
     *   currently active account should be highlighted
     *   */
    ManageAccountsViewModel viewModel;
    ActivityManageAccountsBinding binding;

    private ListView userListView;

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
        AndroidInjection.inject(this);
        setTheme(R.style.Theme_Piwigo);
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ManageAccountsViewModel.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_accounts);
        binding.setViewModel(viewModel);

        /* TODO: isn't it bulls*** to set the title of the viewmodel from the activity and wouldn't this be better handled in the ViewModel directly? */
        viewModel.title.set(getString(R.string.nav_manage_accounts));
        viewModel.users.set(accountHelper.getUsers());
        viewModel.setView(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.account_toolbar);
        setSupportActionBar(toolbar);

        userListView = (ListView) findViewById(R.id.user_list);
        mUserAdapter = new UserAdapter(viewModel.users.get());
        userListView.setAdapter(mUserAdapter);

        viewModel.users.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                ManageAccountsActivity.this.mUserAdapter.notifyDataSetChanged();
            }
        });
        viewModel.users.set(accountHelper.getUsers());

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            Resources res = ManageAccountsActivity.this.getResources();

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                Intent intent = new Intent(getApplicationContext(),
                        LoginActivity.class);
                intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUserAdapter.getItem(position).account.name);
                startActivity(intent);
            }
        });

        userListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long arg) {
                new AlertDialog.Builder(ManageAccountsActivity.this)
                        .setTitle(ManageAccountsActivity.this.getResources().getString(R.string.account_delete_title))
                        .setMessage(ManageAccountsActivity.this.getResources().getString(R.string.account_delete_confirmation, mUserAdapter.getItem(position).account.name))
                        .setIcon(android.R.drawable.ic_delete)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                if(whichButton == DialogInterface.BUTTON_POSITIVE) {
                                    accountHelper.removeAccount(mUserAdapter.getItem(position));
                                }
                            }})
                        .setNegativeButton(android.R.string.no, null).show();

                return true;
            }
        });
        registerForContextMenu(userListView);

        int idx = 0;
        for(User u : accountHelper.getUsers()){
            if(accountHelper.getUser() == u) {
                break;
            }else {
                idx++;
            }
        }
        userListView.setItemChecked(idx, true);
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

    @Override
    public void select(User user){
        int idx = 0;
        View selected = userListView.getSelectedView();
        if(selected != null || userListView.getCheckedItemPosition() > 0) {
            selected.setSelected(false);
//            userListView.setItemChecked(idx, false);
        }
        for(User u : accountHelper.getUsers()){
            if(user == u) {
                break;
            }else {
                idx++;
            }
        }
        /* TODO: fix selection - it does not yet work */
        userListView.setItemChecked(idx, true);
        userListView.setSelected(true);
    }
/* TODO cleanup observer in ViewModel */

}
