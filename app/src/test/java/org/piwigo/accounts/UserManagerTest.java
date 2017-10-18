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
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.piwigo.BuildConfig;
import org.piwigo.R;
import org.piwigo.TestPiwigoApplication;
import org.piwigo.io.repository.PreferencesRepository;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestPiwigoApplication.class, constants = BuildConfig.class)
public class UserManagerTest {

    private static final String ACCOUNT_TYPE = "account_type";
    private static final String ACCOUNT_NAME = "account_name";
    private static final String SITE_URL = "http://piwigo.org/";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String COOKIE = "cookie";
    private static final String TOKEN = "token";

    @Mock AccountManager accountManager;
    @Mock Resources resources;
    @Mock PreferencesRepository preferencesRepository;

    private UserManager userManager;

    @Before public void setup() {
        MockitoAnnotations.initMocks(this);

        when(resources.getString(R.string.account_type)).thenReturn(ACCOUNT_TYPE);
        when(resources.getString(eq(R.string.account_name), anyString(), anyString())).thenReturn(ACCOUNT_NAME);

        userManager = new UserManager(accountManager, resources, preferencesRepository);
    }

    @Test public void isLoggedIn_accountsExist_returnsTrue() {
        when(accountManager.getAccountsByType(ACCOUNT_TYPE)).thenReturn(new Account[1]);

        assertThat(userManager.isLoggedIn()).isTrue();
    }

    @Test public void isLoggedIn_noAccountsExist_returnsFalse() {
        when(accountManager.getAccountsByType(ACCOUNT_TYPE)).thenReturn(new Account[0]);

        assertThat(userManager.isLoggedIn()).isFalse();
    }

    @Test public void userExists_usernameAndUrlMatchAnAccount_returnsTrue() {
        Account account = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
        when(accountManager.getAccountsByType(ACCOUNT_TYPE)).thenReturn(new Account[] {account});

        assertThat(userManager.userExists(SITE_URL, USERNAME)).isTrue();
    }

    @Test public void userExists_usernameAndUrlDoNotMatchAnAccount_returnsFalse() {
        Account account = new Account("another_name", ACCOUNT_TYPE);
        when(accountManager.getAccountsByType(ACCOUNT_TYPE)).thenReturn(new Account[] {account});

        assertThat(userManager.userExists(SITE_URL, USERNAME)).isFalse();
    }

    @Test public void createUser_withoutCredentials_createsGuestAccount() {
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);

        userManager.createUser(SITE_URL, null, null, null, null);

        verify(accountManager).addAccountExplicitly(accountCaptor.capture(), eq(null), bundleCaptor.capture());
        Account account = accountCaptor.getValue();
        Bundle bundle = bundleCaptor.getValue();
        assertThat(account).hasName(ACCOUNT_NAME);
        assertThat(account).hasType(ACCOUNT_TYPE);
        assertThat(bundle).hasSize(3);
        assertThat(bundle).hasKey(UserManager.KEY_IS_GUEST);
        assertThat(bundle.getString(UserManager.KEY_IS_GUEST)).isEqualTo(Boolean.toString(true));
        assertThat(bundle).hasKey(UserManager.KEY_SITE_URL);
        assertThat(bundle.getString(UserManager.KEY_SITE_URL)).isEqualTo(SITE_URL);
        assertThat(bundle).hasKey(UserManager.KEY_USERNAME);
        assertThat(bundle.getString(UserManager.KEY_USERNAME)).isEqualTo(UserManager.GUEST_ACCOUNT_NAME);
    }

    @Test public void createUser_withCredentials_createsNormalAccount() {
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);

        userManager.createUser(SITE_URL, USERNAME, PASSWORD, COOKIE, TOKEN);

        verify(accountManager).addAccountExplicitly(accountCaptor.capture(), eq(PASSWORD), bundleCaptor.capture());
        Account account = accountCaptor.getValue();
        Bundle bundle = bundleCaptor.getValue();
        assertThat(account).hasName(ACCOUNT_NAME);
        assertThat(account).hasType(ACCOUNT_TYPE);
        assertThat(bundle).hasSize(5);
        assertThat(bundle).hasKey(UserManager.KEY_IS_GUEST);
        assertThat(bundle.getString(UserManager.KEY_IS_GUEST)).isEqualTo(Boolean.toString(false));
        assertThat(bundle).hasKey(UserManager.KEY_SITE_URL);
        assertThat(bundle.getString(UserManager.KEY_SITE_URL)).isEqualTo(SITE_URL);
        assertThat(bundle).hasKey(UserManager.KEY_USERNAME);
        assertThat(bundle.getString(UserManager.KEY_USERNAME)).isEqualTo(USERNAME);
        assertThat(bundle).hasKey(UserManager.KEY_COOKIE);
        assertThat(bundle.getString(UserManager.KEY_COOKIE)).isEqualTo(COOKIE);
        assertThat(bundle).hasKey(UserManager.KEY_TOKEN);
        assertThat(bundle.getString(UserManager.KEY_TOKEN)).isEqualTo(TOKEN);
    }

    @Test public void getActiveAccount_withNoAccounts_returnsAbsent() {
        when(accountManager.getAccountsByType(ACCOUNT_TYPE)).thenReturn(new Account[0]);

        assertThat(userManager.getActiveAccount().isPresent()).isFalse();
    }

    @Test public void getActiveAccount_withNoActiveAccount_returnsFirstAccount() {
        Account firstAccount = mock(Account.class);
        Account secondAccount = mock(Account.class);
        when(accountManager.getAccountsByType(ACCOUNT_TYPE)).thenReturn(new Account[] {firstAccount, secondAccount});
        when(preferencesRepository.getActiveAccount()).thenReturn(null);

        assertThat(userManager.getActiveAccount().get()).isEqualTo(firstAccount);
    }

    @Test public void getActiveAccount_withActiveAccount_returnsActiveAccount() {
        Account firstAccount = new Account("first_account", ACCOUNT_TYPE);
        Account secondAccount = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
        when(accountManager.getAccountsByType(ACCOUNT_TYPE)).thenReturn(new Account[] {firstAccount, secondAccount});
        when(preferencesRepository.getActiveAccount()).thenReturn(ACCOUNT_NAME);

        assertThat(userManager.getActiveAccount().get()).isEqualTo(secondAccount);
    }

    @Test public void getActiveAccount_withInvalidActiveAccount_returnsFirstAccount() {
        Account firstAccount = new Account("first_account", ACCOUNT_TYPE);
        Account secondAccount = new Account("second_account", ACCOUNT_TYPE);
        when(accountManager.getAccountsByType(ACCOUNT_TYPE)).thenReturn(new Account[] {firstAccount, secondAccount});
        when(preferencesRepository.getActiveAccount()).thenReturn(ACCOUNT_NAME);

        assertThat(userManager.getActiveAccount().get()).isEqualTo(firstAccount);
    }

    @Test public void getSiteUrl_callsAccountManager() {
        Account account = mock(Account.class);

        userManager.getSiteUrl(account);

        verify(accountManager).getUserData(account, UserManager.KEY_SITE_URL);
    }

    @Test public void getUsername_callsAccountManager() {
        Account account = mock(Account.class);

        userManager.getUsername(account);

        verify(accountManager).getUserData(account, UserManager.KEY_USERNAME);
    }

    @Test public void getCookie_callsAccountManager() {
        Account account = mock(Account.class);

        userManager.getCookie(account);

        verify(accountManager).getUserData(account, UserManager.KEY_COOKIE);
    }

    @Test public void getToken_callsAccountManager() {
        Account account = mock(Account.class);

        userManager.getToken(account);

        verify(accountManager).getUserData(account, UserManager.KEY_TOKEN);
    }
}