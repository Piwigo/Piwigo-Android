/*
 * Copyright 2017-2017 Raphael Mack http://www.raphael-mack.de
 * Copyright 2017-2017 Piwigo Team http://piwigo.org
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
