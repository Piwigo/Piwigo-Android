/*
 * Copyright 2016 Phil Bayfield https://philio.me
 * Copyright 2016 Piwigo Team http://piwigo.org
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

package org.piwigo.helper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import org.piwigo.R;
import org.piwigo.io.DynamicEndpoint;
import org.piwigo.io.Session;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.ui.model.User;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class AccountHelper {

    private static final String GUEST_ACCOUNT_NAME = "guest";

    private static final String KEY_IS_GUEST = "is_guest";
    private static final String KEY_URL = "url";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_COOKIE = "cookie";
    private static final String KEY_TOKEN  = "token";

    private Context context;
    private AccountManager accountManager;

    @Inject Session session;
    @Inject DynamicEndpoint endpoint;

    @Inject public AccountHelper(Context context) {
        this.context = context;
        accountManager = AccountManager.get(context);
    }

    public boolean accountExists(LoginResponse loginResponse) {
        String name = getAccountName(loginResponse);
        List<Account> accounts = getAccounts();
        for (Account account : accounts) {
            if (account.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Account createAccount(LoginResponse loginResponse) {
        if (loginResponse.statusResponse.result.username.equals(GUEST_ACCOUNT_NAME)) {
            return createGuestAccount(loginResponse);
        } else {
            return createUserAccount(loginResponse);
        }
    }

    public boolean hasAccount() {
        List<Account> accounts = getAccounts();
        return accounts.size() > 0;
    }

    public Account getAccount(String name, boolean firstIfInvalid) {
        Account account = getAccountIfAvailable(name, firstIfInvalid);
        if (account != null) {
            updateSession(account);
            updateEndpoint(account);
        }
        return account;
    }

    public List<Account> getAccounts() {
        return Arrays.asList(accountManager.getAccountsByType(context.getString(R.string.account_type)));
    }

    public User createUser(Account account) {
        User user = new User();
        user.guest = Boolean.parseBoolean(accountManager.getUserData(account, KEY_IS_GUEST));
        user.url = accountManager.getUserData(account, KEY_URL);
        user.username = accountManager.getUserData(account, KEY_USERNAME);
        return user;
    }

    private Account createUserAccount(LoginResponse loginResponse) {
        String accountName = getAccountName(loginResponse);
        Account account = new Account(accountName, context.getString(R.string.account_type));
        Bundle userdata = new Bundle();
        userdata.putString(KEY_IS_GUEST, Boolean.toString(false));
        userdata.putString(KEY_URL, loginResponse.url);
        userdata.putString(KEY_USERNAME, loginResponse.statusResponse.result.username);
        userdata.putString(KEY_COOKIE, loginResponse.pwgId);
        userdata.putString(KEY_TOKEN, loginResponse.statusResponse.result.pwgToken);
        accountManager.addAccountExplicitly(account, loginResponse.password, userdata);
        return account;
    }

    private Account createGuestAccount(LoginResponse loginResponse) {
        String accountName = getAccountName(loginResponse);
        Account account = new Account(accountName, context.getString(R.string.account_type));
        Bundle userdata = new Bundle();
        userdata.putString(KEY_IS_GUEST, Boolean.toString(true));
        userdata.putString(KEY_URL, loginResponse.url);
        userdata.putString(KEY_USERNAME, GUEST_ACCOUNT_NAME);
        accountManager.addAccountExplicitly(account, null, userdata);
        return account;
    }

    private String getAccountName(LoginResponse loginResponse) {
        String username = loginResponse.statusResponse.result.username;
        String hostname = Uri.parse(loginResponse.url).getHost();
        return context.getString(R.string.account_name, username, hostname);
    }

    private Account getAccountIfAvailable(String name, boolean firstIfInvalid) {
        List<Account> accounts = getAccounts();
        if (accounts.size() == 0) {
            return null;
        }
        if (name == null) {
            return accounts.get(0);
        }
        for (Account account : accounts) {
            if (account.name.equals(name)) {
                return account;
            }
        }
        return firstIfInvalid ? accounts.get(0) : null;
    }

    private void updateSession(Account account) {
        boolean guest = Boolean.parseBoolean(accountManager.getUserData(account, KEY_IS_GUEST));
        session.setCookie(guest ? null : accountManager.getUserData(account, KEY_COOKIE));
        session.setToken(guest ? null : accountManager.getUserData(account, KEY_TOKEN));
    }

    private void updateEndpoint(Account account) {
        endpoint.setUrl(accountManager.getUserData(account, KEY_URL));
    }

}
