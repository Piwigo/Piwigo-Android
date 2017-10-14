/*
 * Piwigo for Android
 * Copyright (C) 2017 Raphael Mack http://www.raphael-mack.de
 * Copyright (C) 2016 Phil Bayfield https://philio.me
 * Copyright (C) 2016 Piwigo Team http://piwigo.org
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

package org.piwigo.helper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import org.apache.commons.lang3.StringUtils;
import org.piwigo.R;
import org.piwigo.io.Session;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.ui.model.User;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;

/* TODO: cleanup this class. It has strange methcods... also the Userlist could be improved
* Observer notifications to be documented */
@Singleton
public class AccountHelper extends Observable {

    private static final String GUEST_ACCOUNT_NAME = "guest";

    private static final String KEY_IS_GUEST = "is_guest";
    private static final String KEY_URL = "url";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_COOKIE = "cookie";
    private static final String KEY_TOKEN  = "token";

    private Context context;
    private AccountManager accountManager;
    private User user;

    @Inject Session session;

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
        String accountName = getAccountName(loginResponse);
        Account account = new Account(accountName, context.getString(R.string.account_type));
        Bundle userdata = new Bundle();

        if (loginResponse.statusResponse.result.username.equals(GUEST_ACCOUNT_NAME)) {
            return updateGuestAccount(account, userdata, loginResponse);
        } else {
            return updateUserAccount(account, userdata, loginResponse);
        }
    }

    public void updateAccount(Account account, LoginResponse loginResponse) {
        Bundle userdata = new Bundle();
        if (loginResponse.statusResponse.result.username.equals(GUEST_ACCOUNT_NAME)) {
            updateGuestAccount(account, userdata, loginResponse);
        } else {
            updateUserAccount(account, userdata, loginResponse);
        }
    }


    public boolean hasAccount() {
        List<Account> accounts = getAccounts();
        return accounts.size() > 0;
    }

    public User getUser(String name, boolean firstIfInvalid){
        List<User> users = getUsers();
        if (users.size() == 0) {
            return null;
        }
        if (name == null){
            if (firstIfInvalid) {
                return users.get(0);
            }else{
                return null;
            }
        }
        for (User user: users) {
            if (user.account.name.equals(name)) {
                return user;
            }
        }
        return firstIfInvalid ? users.get(0) : null;

    }

    public Account getAccount(String name, boolean firstIfInvalid) {
        User user = getUser(name, firstIfInvalid);
        if (user != null) {
            updateSession(user);
            return user.account;
        }else{
            return null;
        }
    }

    /* return the currently selected user */
    public User getUser() {
        return user;
    }

    public void setAccount(Account account) {
        updateSession(getUser(account.name, false));
    }

    /* TODO make private? */
    public List<Account> getAccounts() {
        return Arrays.asList(accountManager.getAccountsByType(context.getString(R.string.account_type)));
    }

    public List<User> getUsers(){
        /* todo make list of users an array and static, update it from the accounts on each call? */
        LinkedList<User> users = new LinkedList<User>();
        for (Account a: getAccounts()) {
            users.addLast(createUser(a));
        }
        return users;
    }

    private User createUser(Account account) {
        User user = new User();
        user.guest = Boolean.parseBoolean(accountManager.getUserData(account, KEY_IS_GUEST));
        user.url = accountManager.getUserData(account, KEY_URL);
        user.username = accountManager.getUserData(account, KEY_USERNAME);
        user.account = account;
        return user;
    }

    public String getAccountUrl(Account account) {
        return accountManager.getUserData(account, KEY_URL);
    }

    private Account updateUserAccount(Account account, Bundle userdata, LoginResponse loginResponse) {
        userdata.putString(KEY_IS_GUEST, Boolean.toString(false));
        userdata.putString(KEY_URL, loginResponse.url);
        userdata.putString(KEY_USERNAME, loginResponse.statusResponse.result.username);
        userdata.putString(KEY_COOKIE, loginResponse.pwgId);
        userdata.putString(KEY_TOKEN, loginResponse.statusResponse.result.pwgToken);
        accountManager.addAccountExplicitly(account, loginResponse.password, userdata);
        setChanged();
        notifyObservers();
        return account;
    }

    private Account updateGuestAccount(Account account, Bundle userdata, LoginResponse loginResponse) {
        userdata.putString(KEY_IS_GUEST, Boolean.toString(true));
        userdata.putString(KEY_URL, loginResponse.url);
        userdata.putString(KEY_USERNAME, GUEST_ACCOUNT_NAME);
        accountManager.addAccountExplicitly(account, null, userdata);
        setChanged();
        notifyObservers();
        return account;
    }

    public Account removeAccount(Account account) {
        accountManager.removeAccount(account, null, null);
        setChanged();
        notifyObservers();
        return account;
    }

    private String getAccountName(LoginResponse loginResponse) {
        Uri uri = Uri.parse(loginResponse.url);
        String username = loginResponse.statusResponse.result.username;
        String sitename = uri.getHost() + uri.getPath();
        if (sitename.endsWith("/")) {
            sitename = StringUtils.chop(sitename);
        }
        return context.getString(R.string.account_name, username, sitename.toLowerCase());
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


    private void updateSession(User user) {
        this.user = user;

        boolean guest = Boolean.parseBoolean(accountManager.getUserData(user.account, KEY_IS_GUEST));
        session.setAccount(user.account);
        session.setCookie(guest ? null : accountManager.getUserData(user.account, KEY_COOKIE));
        session.setToken(guest ? null : accountManager.getUserData(user.account, KEY_TOKEN));
        setChanged();
        notifyObservers(user);
    }

}
