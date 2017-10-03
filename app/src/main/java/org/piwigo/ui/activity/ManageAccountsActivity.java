/*
 * Copyright 2017 Raphael Mack http://www.raphael-mack.de
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

package org.piwigo.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Bundle;

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
import org.piwigo.databinding.ActivityMainBinding;
import org.piwigo.databinding.ActivityManageAccountsBinding;
import org.piwigo.ui.model.User;
import org.piwigo.ui.view.AccountView;
import org.piwigo.ui.viewmodel.ManageAccountsViewModel;

import java.util.List;

import javax.inject.Inject;

public class ManageAccountsActivity extends BaseActivity implements AccountView {

    @Inject ManageAccountsViewModel viewModel;

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
/* TODO fill image here */

            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityComponent().inject(this);

        DataBindingUtil.setContentView(this, R.layout.activity_manage_accounts);

        ActivityManageAccountsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_accounts);

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

            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long arg3) {
                Toast.makeText(getApplicationContext(), mUserAdapter.getItem(position).username +
                                "@" + mUserAdapter.getItem(position).url + " selected"
                        , Toast.LENGTH_LONG).show();
                /* TODO remove toast */
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
                /* TODO add account deletion */
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
