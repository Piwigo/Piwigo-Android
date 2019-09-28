/*
 * Piwigo for Android
 * Copyright (C) 2018-2018 Raphael Mack http://www.raphael-mack.de
 * Copyright (C) 2018-2018 Piwigo Team http://piwigo.org
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
import androidx.lifecycle.ViewModel;
import androidx.annotation.NonNull;

import org.piwigo.accounts.UserManager;

public class AccountViewModel extends ViewModel {
    private final @NonNull Account account;
    private final @NonNull UserManager userManager;

    public AccountViewModel(@NonNull UserManager userManager, @NonNull Account account) {
        this.account = account;
        this.userManager = userManager;
    }

    public String getSiteUrl(){
        return userManager.getSiteUrl(account);
    }

    public String getUsername(){
        return userManager.getUsername(account);
    }

    public boolean isActive(){
        return userManager.getActiveAccount().getValue().name.equals(account.name);
    }
}
