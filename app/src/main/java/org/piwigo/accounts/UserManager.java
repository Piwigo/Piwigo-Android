/*
 * Piwigo for Android
 * Copyright (C) 2016-2018 Piwigo Team http://piwigo.org
 * Copyright (C) 2018 Raphael Mack
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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.StringUtils;
import org.piwigo.R;
import org.piwigo.io.repository.PreferencesRepository;

import java.util.List;
import java.util.Locale;

/**
 * Note: a token in Android is replacing username/password for different logins,
 * while the token in piwigo is only valid during one session.
 * That's why the token is not stored in AccountManagers token handling but in the user data
 */
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

    private final MutableLiveData<Account> mCurrentAccount;
    private final MutableLiveData<List<Account>> mAllAccounts;

    public UserManager(AccountManager accountManager, Resources resources, PreferencesRepository preferencesRepository) {
        this.accountManager = accountManager;
        this.resources = resources;
        this.preferencesRepository = preferencesRepository;
        this.mCurrentAccount = new MutableLiveData<>();
        this.mAllAccounts = new MutableLiveData<>();

        refreshAccounts();
        setActiveAccount(preferencesRepository.getActiveAccountName());
    }

    /* refresh account list - to be called by activities which are aware
     * of a change in the accounts */
    public void refreshAccounts() {
        Account[] accounts = accountManager.getAccountsByType(resources.getString(R.string.account_type));
        mAllAccounts.setValue(ImmutableList.copyOf(accounts));

        Account a = mCurrentAccount.getValue();
        setActiveAccount(a == null ? "" : a.name);
    }

    public boolean hasAccounts() {
        return accountManager.getAccountsByType(resources.getString(R.string.account_type)).length > 0;
    }

    /* get account for username@siteUrl, null if no such account exists */
    public Account getAccountForUser(String siteUrl, String username) {
        String accountName = getAccountName(siteUrl, TextUtils.isEmpty(username) ? GUEST_ACCOUNT_NAME : username);
        for (Account account : accountManager.getAccountsByType(resources.getString(R.string.account_type))) {
            if (account.name.equals(accountName)) {
                return account;
            }
        }
        return null;
    }

    public boolean userExists(String siteUrl, String username) {
        return getAccountForUser(siteUrl, username) != null;
    }

    public Account createUser(String siteUrl, String username, String password, String cookie, String token) {
        Account result;
        if (TextUtils.isEmpty(username) && TextUtils.isEmpty(password)) {
            result = createGuestUser(siteUrl);
        } else {
            result = createNormalUser(siteUrl, username, password, cookie, token);
        }
        accountManager.setUserData(result, KEY_TOKEN, token);

        return result;
    }

    /* observe this LiveData for notifications on account switches */
    public LiveData<Account> getActiveAccount() {
        return mCurrentAccount;
    }

    public LiveData<List<Account>> getAccounts(){
        return mAllAccounts;
    }

    public String getSiteUrl(Account account) {
        String url = accountManager.getUserData(account, KEY_SITE_URL);
        if(url == null){
            url = "/";
        }else if(!url.endsWith("/")){
            /* Retrofit requires the url to have a trailing / */
            url += "/";
        }
        return url;
    }

    public String getUsername(Account account) {
        return accountManager.getUserData(account, KEY_USERNAME);
    }

    public String getPassword(Account account) {
        return isGuest(account) ? "" : accountManager.getPassword(account);
    }

    public String getCookie(Account account) {
        return accountManager.getUserData(account, KEY_COOKIE);
    }

    public void setCookie(Account account, String cookie) {
        accountManager.setUserData(account, KEY_COOKIE, cookie);
    }

    public void setToken(Account account, String token) {
        accountManager.setUserData(account, KEY_TOKEN, token);
    }

    // TODO: rmk, 2018-11-24: not sure whether it is a good idea to store the token in the account at all
    // maybe it would be better to just keep it in the UserRepository
    public String getToken(Account account) {
        return accountManager.getUserData(account, KEY_TOKEN);
    }

    public boolean isGuest(Account account) {
        return GUEST_ACCOUNT_NAME.equals(getUsername(account));
    }

    public String getAccountName(Account account){
        return getAccountName(getSiteUrl(account), getUsername(account));
    }

    private String getAccountName(String siteUrl, String username) {
        Uri uri = Uri.parse(siteUrl);
        String sitename = uri.getHost() + uri.getPath();
        if (sitename.endsWith("/")) {
            sitename = StringUtils.chop(sitename);
        }
        return resources.getString(R.string.account_name, username, sitename.toLowerCase(Locale.ROOT));
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

    public Account getAccountWithName(String accountName){
        Account[] accounts = accountManager.getAccountsByType(resources.getString(R.string.account_type));

        for (Account account : accounts) {
            if (account.name.equals(accountName)) {
                return account;
            }
        }
        return null;
    }

    public void setActiveAccount(Account activeAccount) {
        mCurrentAccount.setValue(activeAccount);
    }

    public void setActiveAccount(String activeAccount) {
        Account[] accounts = accountManager.getAccountsByType(resources.getString(R.string.account_type));

        if (!TextUtils.isEmpty(activeAccount)) {
            for (Account account : accounts) {
                if (account.name.equals(activeAccount)) {
                    preferencesRepository.setActiveAccount(activeAccount);
                    mCurrentAccount.setValue(account);
                    return;
                }
            }
        }

        /* the selected account is not available select default */
        if(accounts.length > 0) {
            mCurrentAccount.setValue(accounts[0]);
        }else{
            mCurrentAccount.setValue(null);
        }
    }

    /* throws IllegalArgumentException if the rename is not allowed because there is already an account with those properties. */
    public void updateAccount(@NonNull Account account, @NonNull String url, String username, String password) throws IllegalArgumentException{
        boolean isGuest = GUEST_ACCOUNT_NAME.equals(username);
        if(username == null || username.length() == 0){
            isGuest = true;
            username = GUEST_ACCOUNT_NAME;
        }

        accountManager.setUserData(account, KEY_IS_GUEST, Boolean.toString(isGuest));
        accountManager.setUserData(account, KEY_SITE_URL, url);
        accountManager.setUserData(account, KEY_USERNAME, username);
        accountManager.setPassword(account, isGuest ? "" : password);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String newname = getAccountName(url, username);
            if(!newname.equals(account.name)) {
                accountManager.renameAccount(account, newname, null, null);
            }
        }
    }

    public void removeAccount(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            accountManager.removeAccount(mCurrentAccount.getValue(), null, null);
        } else {
            accountManager.removeAccountExplicitly(mCurrentAccount.getValue());
        }
    }
}
