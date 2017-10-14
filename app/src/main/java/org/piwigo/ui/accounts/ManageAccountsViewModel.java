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

import android.arch.lifecycle.ViewModel;
import android.content.res.Resources;
import android.databinding.ObservableField;
import android.view.View;
import android.widget.Toast;

import org.piwigo.R;
import org.piwigo.helper.AccountHelper;
import org.piwigo.ui.model.User;
import org.piwigo.ui.view.AccountView;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.inject.Inject;

public class ManageAccountsViewModel extends ViewModel implements Observer {
    private final Resources resources;
    private final AccountHelper accountHelper;

    public ObservableField<String> title = new ObservableField<>();
    public ObservableField<List<User>> users = new ObservableField<>();

    private AccountView view;

    public ManageAccountsViewModel(Resources resources, AccountHelper accountHelper) {
        this.resources = resources;
        this.accountHelper = accountHelper;
        accountHelper.addObserver(this);
        /* TODO: cleanup observer on close of activity */
    }

    public void closeIconClick(View v) {
        view.close();
    }

    public void setView(AccountView view){
        this.view = view;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof AccountHelper) {
            if(data instanceof User) {
                /* nothing for the cahnge of the active user */
            }else{
                users.set(new LinkedList<>(accountHelper.getUsers()));
            }
        }
    }
}
