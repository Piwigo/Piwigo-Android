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

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.room.Room;

import android.text.TextUtils;
import android.util.Log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.StringUtils;
import org.piwigo.PiwigoApplication;
import org.piwigo.R;
import org.piwigo.data.db.CacheDatabase;
import org.piwigo.io.PreferencesRepository;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Cookie;

/**
 * Note: a token in Android is replacing username/password for different logins,
 * while the token in piwigo is only valid during one session.
 * That's why the token is not stored in AccountManagers token handling but in the user data
 */
public class UserManager {

    @VisibleForTesting static final String KEY_IS_GUEST = "is_guest";
    @VisibleForTesting static final String KEY_SITE_URL = "url";
    @VisibleForTesting static final String KEY_USERNAME = "username";
    @VisibleForTesting static final String KEY_CHUNK_SIZE  = "chunk_size";

    @VisibleForTesting static final String GUEST_ACCOUNT_NAME = "guest";
    private static final String TAG = UserManager.class.getName();

    private final AccountManager accountManager;
    private final Resources resources;
    private final PreferencesRepository preferencesRepository;

    private final MutableLiveData<Account> mCurrentAccount;
    private final MutableLiveData<List<Account>> mAllAccounts;
    private final Context mContext;
    private Map<String, CacheDatabase> databases = new HashMap<String, CacheDatabase>();
    private Cookie mSessionCookie;
    private String mSessionToken;

    public UserManager(AccountManager accountManager, Resources resources, PreferencesRepository preferencesRepository, Context ctx) {
        this.accountManager = accountManager;
        this.resources = resources;
        this.preferencesRepository = preferencesRepository;
        this.mCurrentAccount = new MutableLiveData<>();
        this.mAllAccounts = new MutableLiveData<>();
        this.mContext = ctx;

        setActiveAccount(preferencesRepository.getActiveAccountName());
        refreshAccounts();
    }

    public CacheDatabase getDatabaseForAccount(Account a) {
        if (a == null)
            return null;
        CacheDatabase result = databases.get(a.name);
        if (result == null){
            updateDB();
            result = databases.get(a.name);
        }
        return result;
    }

    /* refresh account list - to be called by activities which are aware
     * of a change in the accounts */
    public void refreshAccounts() {
        Account[] accounts = accountManager.getAccountsByType(resources.getString(R.string.account_type));
        mAllAccounts.setValue(ImmutableList.copyOf(accounts));

        Account a = mCurrentAccount.getValue();
        setActiveAccount(a == null ? "" : a.name);
    }

    public int countOfAcounts() {
        return accountManager.getAccountsByType(resources.getString(R.string.account_type)).length;
    }

    public boolean hasAccounts() {
        return countOfAcounts() > 0;
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

    public Account createUser(String siteUrl, String username, String password) {
        Account result;
        if (TextUtils.isEmpty(username) && TextUtils.isEmpty(password)) {
            result = createGuestUser(siteUrl);
        } else {
            result = createNormalUser(siteUrl, username, password);
        }
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

    public Cookie sessionCookie() {
        Log.d("UserManager", "sessionCookie: " + mSessionCookie);
        return mSessionCookie;

    }

    public void setSessionCookie(Cookie cookie) {
        mSessionCookie = cookie;
        Log.d("UserManager", "setSessionCookie: " + cookie);
    }

    public String sessionToken() {
        Log.d("UserManager", "sessionToken: " + mSessionToken);
        return mSessionToken;
    }

    public void setSessionToken(String token) {
        mSessionToken = token;
    }


    /** set the chunk size for upload in bytes
     * note: piwigo server returns the preferred chunk-size in kB
     * */
    public void setChunkSize(Account account, int chunkSize) {
        accountManager.setUserData(account, KEY_CHUNK_SIZE, String.valueOf(chunkSize));
    }

    public int getChunkSize(Account account)
    {
        String junksize = accountManager.getUserData(account, KEY_CHUNK_SIZE);
        if(junksize == null){
            return 128;
        }
        return Integer.parseInt(junksize);
    }

    public boolean isGuest(Account account) {
        return GUEST_ACCOUNT_NAME.equals(getUsername(account));
    }

    private String getAccountName(String siteUrl, String username) {
        Uri uri = Uri.parse(siteUrl);
        String sitename = uri.getHost() + uri.getPath();
        if (sitename.endsWith("/")) {
            sitename = StringUtils.chop(sitename);
        }
        return resources.getString(R.string.account_name, username, sitename.toLowerCase(Locale.ROOT));
    }

    private Account createNormalUser(String siteUrl, String username, String password) {
        String accountName = getAccountName(siteUrl, username);
        Account account = new Account(accountName, resources.getString(R.string.account_type));
        Bundle userdata = new Bundle();
        userdata.putString(KEY_IS_GUEST, Boolean.toString(false));
        userdata.putString(KEY_SITE_URL, siteUrl);
        userdata.putString(KEY_USERNAME, username);
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

    public void setActiveAccount(Account activeAccount) {
        if (activeAccount != null)
            preferencesRepository.setActiveAccount(activeAccount.name);
        mCurrentAccount.setValue(activeAccount);
        updateDB();
    }

    private void updateDB(){
        Account a = mCurrentAccount.getValue();
        if(a != null) {
            CacheDatabase cache = Room.databaseBuilder(mContext,
                    CacheDatabase.class, dbNameFor(a))
                    .fallbackToDestructiveMigration() /* as the complete database is only a cache we'll loose nothing critical if we drop it */
                    .build();
            databases.put(a.name, cache); // TODO: should we use here a Map with WeakReferences to the database to free the memory once all threads are finished accessing the DB?
        }else{
            Log.e(TAG, "no account set");
        }
        
    }

    public void setActiveAccount(String activeAccount) {
        setSessionCookie(null);
        setSessionToken(null);
        Account[] accounts = accountManager.getAccountsByType(resources.getString(R.string.account_type));

        if (!TextUtils.isEmpty(activeAccount)) {
            for (Account account : accounts) {
                if (account.name.equals(activeAccount)) {
                    setActiveAccount(account);
                    return;
                }
            }
        }

        /* the selected account is not available select default */
        if(accounts.length > 0) {
            setActiveAccount(accounts[0]);
        } else {
            setActiveAccount((Account)null);
         }
    }

    private String dbNameFor(Account account) {
        return "cache-" + account.name.replace("/", "-");
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

    public void removeAccount(Account account) {
        String dbName = dbNameFor(account);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            accountManager.removeAccount(account, null, future -> refreshAccounts(), null);
        } else {
            accountManager.removeAccount(account, future -> refreshAccounts(), null);
        }

        databases.get(account.name).close();

        File databases = new File(mContext.getApplicationInfo().dataDir + "/databases");
        File db = new File(databases, dbName);
        if (!db.delete()) {
            throw new RuntimeException("Failed to delete database " + dbName);
        }

        File journal = new File(databases, dbName + "-journal");
        if (journal.exists()) {
            if (!journal.delete()) {
                throw new RuntimeException("Failed to delete database journal " + dbName);
            }
        }
        File shm = new File(databases, dbName + "-shm");
        if (shm.exists()) {
            if (!shm.delete()) {
                throw new RuntimeException("Failed to delete database shm " + dbName);
            }
        }
        File wal = new File(databases, dbName + "-wal");
        if (wal.exists()) {
            if (!wal.delete()) {
                throw new RuntimeException("Failed to delete database wal " + dbName);
            }
        }
    }
}
