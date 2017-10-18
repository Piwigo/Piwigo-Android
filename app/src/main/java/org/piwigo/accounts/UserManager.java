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

package org.piwigo.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import org.apache.commons.lang3.StringUtils;
import org.piwigo.R;
import org.piwigo.io.repository.PreferencesRepository;

public class UserManager {

    @VisibleForTesting static final String KEY_IS_GUEST = "is_guest";
    @VisibleForTesting static final String KEY_SITE_URL = "url";
    @VisibleForTesting static final String KEY_USERNAME = "username";
    @VisibleForTesting static final String KEY_COOKIE = "cookie";
    @VisibleForTesting static final String KEY_TOKEN  = "token";

    @VisibleForTesting static final String GUEST_ACCOUNT_NAME = "guest";

    private final AccountManager accountManager;
    private final Resources resources;
    private final PreferencesRepository preferencesRepository;

    public UserManager(AccountManager accountManager, Resources resources, PreferencesRepository preferencesRepository) {
        this.accountManager = accountManager;
        this.resources = resources;
        this.preferencesRepository = preferencesRepository;
    }

    public boolean isLoggedIn() {
        return accountManager.getAccountsByType(resources.getString(R.string.account_type)).length > 0;
    }

    public boolean userExists(String siteUrl, String username) {
        String accountName = getAccountName(siteUrl, TextUtils.isEmpty(username) ? GUEST_ACCOUNT_NAME : username);
        for (Account account : accountManager.getAccountsByType(resources.getString(R.string.account_type))) {
            if (account.name.equals(accountName)) {
                return true;
            }
        }
        return false;
    }

    public Account createUser(String siteUrl, String username, String password, String cookie, String token) {
        if (TextUtils.isEmpty(username) && TextUtils.isEmpty(password)) {
            return createGuestUser(siteUrl);
        } else {
            return createNormalUser(siteUrl, username, password, cookie, token);
        }
    }

    @SuppressWarnings("Guava")
    public Optional<Account> getActiveAccount() {
        Account[] accounts = accountManager.getAccountsByType(resources.getString(R.string.account_type));
        if (accounts.length == 0) {
            return Optional.absent();
        }
        String activeAccount = preferencesRepository.getActiveAccount();
        if (!TextUtils.isEmpty(activeAccount)) {
            for (Account account : accounts) {
                if (account.name.equals(activeAccount)) {
                    return Optional.of(account);
                }
            }
        }
        return Optional.of(accounts[0]);
    }

    public String getSiteUrl(Account account) {
        return accountManager.getUserData(account, KEY_SITE_URL);
    }

    public String getUsername(Account account) {
        return accountManager.getUserData(account, KEY_USERNAME);
    }

    public String getCookie(Account account) {
        return accountManager.getUserData(account, KEY_COOKIE);
    }

    public String getToken(Account account) {
        return accountManager.getUserData(account, KEY_TOKEN);
    }

    private String getAccountName(String siteUrl, String username) {
        Uri uri = Uri.parse(siteUrl);
        String sitename = uri.getHost() + uri.getPath();
        if (sitename.endsWith("/")) {
            sitename = StringUtils.chop(sitename);
        }
        return resources.getString(R.string.account_name, username, sitename.toLowerCase());
    }

    private Account createNormalUser(String siteUrl, String username, String password, String cookie, String token) {
        String accountName = getAccountName(siteUrl, username);
        Account account = new Account(accountName, resources.getString(R.string.account_type));
        Bundle userdata = new Bundle();
        userdata.putString(KEY_IS_GUEST, Boolean.toString(false));
        userdata.putString(KEY_SITE_URL, siteUrl);
        userdata.putString(KEY_USERNAME, username);
        userdata.putString(KEY_COOKIE, cookie);
        userdata.putString(KEY_TOKEN, token);
        accountManager.addAccountExplicitly(account, password, userdata);
        return account;
    }

    private Account createGuestUser(String siteUrl) {
        String accountName = getAccountName(siteUrl, GUEST_ACCOUNT_NAME);
        Account account = new Account(accountName, resources.getString(R.string.account_type));
        Bundle userdata = new Bundle();
        userdata.putString(KEY_IS_GUEST, Boolean.toString(true));
        userdata.putString(KEY_SITE_URL, siteUrl);
        userdata.putString(KEY_USERNAME, GUEST_ACCOUNT_NAME);
        accountManager.addAccountExplicitly(account, null, userdata);
        return account;
    }
}
