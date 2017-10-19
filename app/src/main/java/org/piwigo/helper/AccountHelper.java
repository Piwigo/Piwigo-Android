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
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import org.apache.commons.lang3.StringUtils;
import org.piwigo.R;
import org.piwigo.io.Session;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.ui.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;

/* TODO: cleanup this class. It has strange methcods... also the Userlist could be improved
* Observer notifications to be documented */
/* TODO: accounthelper should recognize if an account was deleted in the android system settings
    and notify the observers accordingly */
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
    private ArrayList<User> users = new ArrayList<User>();

    @Inject Session session;

    @Inject public AccountHelper(Context context) {
        this.context = context;
        accountManager = AccountManager.get(context);

        for (Account a: accountManager.getAccountsByType(context.getString(R.string.account_type))) {
            users.add(createUser(a));
        }
    }

    public boolean accountExists(LoginResponse loginResponse) {
        String name = getAccountName(loginResponse);
        for (Account account : accountManager.getAccountsByType(context.getString(R.string.account_type))) {
            if (account.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Account createAccount(LoginResponse loginResponse) {
        String accountName = getAccountName(loginResponse);
        Account account = new Account(accountName, context.getString(R.string.account_type));
        User user;
        updateAccount(account, loginResponse);

        user = createUser(account);
        users.add(user);

        updateSession(user);
        return account;
    }

    public void updateAccount(Account account, LoginResponse loginResponse) {
        String oldAccountName = account.name;

        accountManager.setUserData(account, KEY_USERNAME, loginResponse.statusResponse.result.username);
        accountManager.setUserData(account, KEY_URL, loginResponse.url);

        if (loginResponse.statusResponse.result.username.equals(GUEST_ACCOUNT_NAME)) {
            accountManager.setUserData(account, KEY_IS_GUEST, Boolean.toString(true));
            accountManager.setUserData(account, KEY_COOKIE, null);
            accountManager.setUserData(account, KEY_TOKEN, null);
            accountManager.setPassword(account, null);
        }else {
            accountManager.setUserData(account, KEY_IS_GUEST, Boolean.toString(false));
            accountManager.setUserData(account, KEY_COOKIE, loginResponse.pwgId);
            accountManager.setUserData(account, KEY_TOKEN, loginResponse.statusResponse.result.pwgToken);
            accountManager.setPassword(account, loginResponse.password);
        }

        if (!getAccountName(loginResponse).equals(oldAccountName))
        {
            /* account need to be renamed */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    accountManager.renameAccount(account, getAccountName(loginResponse), null, null).getResult(); /* rename and wait until rename is done */
                } catch (Exception e) {
                    /* just do nothing for now as I do not see a reasonable reaction... */
                    /* TODO: properly handle the exceptions during account rename */
                }
            }else{
                removeAccount(account);
                createAccount(loginResponse);
                /* now rebuild the User list */
                String currentUserName = user.account.name;
                if(user.account == account){
                    currentUserName = getAccountName(loginResponse);
                }

                for (Account a: accountManager.getAccountsByType(context.getString(R.string.account_type))) {
                    User u = createUser(a);
                    users.add(u);
                    if(u.account.name.equals(currentUserName)){
                        user = u;
                    }
                }
            }
        }

        setChanged();
        notifyObservers();
    }

    public boolean hasAccount() {
        return accountManager.getAccountsByType(context.getString(R.string.account_type)).length > 0;
    }

    public User getUser(String name, boolean firstIfInvalid){
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
        /* TODO set current user */
        updateSession(getUser(account.name, false));
    }

    public List<User> getUsers(){
        return Collections.unmodifiableList(users);
    }

    private User createUser(Account account) {
        /* TODO remove this one here */
        User user = new User();
        user.guest = Boolean.parseBoolean(accountManager.getUserData(account, KEY_IS_GUEST));
        user.url = accountManager.getUserData(account, KEY_URL);
        user.username = accountManager.getUserData(account, KEY_USERNAME);
        user.account = account;
        return user;
    }

    public String getAccountUrl(Account account) {
        /* TODO: replace calls to this by getUser().url*/
        return accountManager.getUserData(account, KEY_URL);
    }

    public void removeAccount(User user) {
        removeAccount(user.account);
    }

    public void removeAccount(Account account) {
        accountManager.removeAccount(account, null, null);
        User userToRemove = null;
        for (User user: users) {
            if (user.account == account) {
                userToRemove = user;
                break;
            }
        }
        users.remove(userToRemove);
        setChanged();
        notifyObservers();
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
