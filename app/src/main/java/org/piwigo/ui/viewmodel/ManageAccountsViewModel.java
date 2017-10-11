/*
 * Piwigo for Android
 *
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

package org.piwigo.ui.viewmodel;

import android.content.res.Resources;
import android.databinding.ObservableField;
import android.view.View;
import android.widget.Toast;

import org.piwigo.R;
import org.piwigo.helper.AccountHelper;
import org.piwigo.ui.model.User;
import org.piwigo.ui.view.AccountView;

import java.util.List;

import javax.inject.Inject;

public class ManageAccountsViewModel extends BaseViewModel {
    public ObservableField<String> title = new ObservableField<>();

    /* TODO: not sure whether makeing users public is a good idea... */
    public List<User> users;
    private AccountView view;

    private AccountHelper accountHelper;

    @Inject
    public ManageAccountsViewModel(AccountHelper accountHelper) {
        this.accountHelper = accountHelper;
        users = accountHelper.getUsers();

        int i = 0;
        for(User u:users)
        {
            //u.username + " " + u.url;
            i++;
        }

/* TODO use it... */
/* TODO: fill accountImage with a nice picute. the best is probably the 'link rel=shortcut icon' from the html of that gallery */
    }

    public void closeIconClick(View v) {
        view.close();
    }

    public void setView(AccountView view){
        this.view = view;
    }
}
