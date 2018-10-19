/*
 * Piwigo for Android
 * Copyright (C) 2016-2018 Piwigo Team http://piwigo.org
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
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.view.View;

import org.piwigo.BR;
import org.piwigo.R;
import org.piwigo.accounts.UserManager;
import org.piwigo.ui.login.LoginActivity;
import org.piwigo.ui.shared.BindingRecyclerViewAdapter;

import java.util.List;


public class ManageAccountsViewModel extends ViewModel {
    public final ObservableField<String> title = new ObservableField<>();

    public final LiveData<List<Account>> accounts;
    Account selectedAccount = null;

    private final ObservableArrayList<Account> items = new ObservableArrayList<>();
    public final BindingRecyclerViewAdapter.ViewBinder<Account> viewBinder = new AccountViewBinder();

    private final UserManager userManager;

    public ManageAccountsViewModel(UserManager userManager) {
        this.userManager = userManager;
        accounts = userManager.getAccounts();
        items.addAll(accounts.getValue());
        selectedAccount = userManager.getActiveAccount().getValue();
    }

    public void refresh(){
        //todo: remove
        userManager.refreshAccounts();
// TODO: this is horrible
        items.clear();
        items.addAll(accounts.getValue());

    }

    public Account getSelectedAccount(){
        return selectedAccount;
    }

    public ObservableArrayList<Account> getItems() {
        return items;
    }

    private class AccountViewBinder implements BindingRecyclerViewAdapter.ViewBinder<Account> {

        @Override public int getViewType(Account account) {
            return 0;
        }

        @Override public int getLayout(int viewType) {
            return R.layout.account_row;
        }

        @Override public void bind(BindingRecyclerViewAdapter.ViewHolder viewHolder, Account account) {
            AccountViewModel vm = new AccountViewModel(userManager, account);
            viewHolder.getBinding().setVariable(BR.viewModel, vm);
            boolean x = vm.isActive();
            viewHolder.itemView.setSelected(x);
            viewHolder.itemView.setOnClickListener(v -> {
                selectedAccount = account;

                userManager.setActiveAccount(account);

//                v.setSelected(true);
                refresh(); // dirty way to redraw the recycler... -> TODO: needs rework
            });
            viewHolder.itemView.setOnLongClickListener(v -> {
                Intent intent = new Intent(v.getContext(), LoginActivity.class);

                intent.setAction(LoginActivity.EDIT_ACCOUNT_ACTION);
                intent.putExtra("account", account);
                v.getContext().startActivity(intent);
                return true;
            });
        }
    }

}
