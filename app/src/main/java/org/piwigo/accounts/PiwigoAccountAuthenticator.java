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

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.piwigo.PiwigoApplication;
import org.piwigo.R;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.io.repository.UserRepository;
import org.piwigo.ui.login.LoginActivity;
import org.piwigo.ui.login.LoginViewModel;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.support.AndroidSupportInjection;
import rx.Observable;
import rx.Subscription;
import rx.observables.BlockingObservable;

public class PiwigoAccountAuthenticator extends AbstractAccountAuthenticator {

    @Inject UserRepository userRepository;
    @Inject UserManager userManager;

    private final Context context;

    public PiwigoAccountAuthenticator(Context context) {
        super(context);
        this.context = context;
        ((PiwigoApplication) context.getApplicationContext()).inject(this);
    }

    @Override public Bundle addAccount(AccountAuthenticatorResponse response,
                                       String accountType,
                                       String authTokenType,
                                       String[] requiredFeatures,
                                       Bundle options) throws NetworkErrorException {

        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override public Bundle confirmCredentials(AccountAuthenticatorResponse response,
                                               Account account, Bundle options) throws NetworkErrorException {
        if (options != null && options.containsKey(AccountManager.KEY_PASSWORD)) {
            final String password = options.getString(AccountManager.KEY_PASSWORD);
            final String token = onlineConfirmPassword(account, password);
            final Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, token == null ? false : true);
            return result;
        }
        // Launch LoginActivity to confirm credentials
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.setAction(LoginActivity.EDIT_ACCOUNT_ACTION);
        intent.putExtra(LoginActivity.PARAM_ACCOUNT, account.name);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override public Bundle getAuthToken(AccountAuthenticatorResponse response,
                                         Account account,
                                         String authTokenType,
                                         Bundle options) throws NetworkErrorException {
        if (!authTokenType.equals(context.getResources().getString(R.string.account_type))) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }
        final AccountManager am = AccountManager.get(context);
        final String password = am.getPassword(account);

        if (password != null) {
            final String token = onlineConfirmPassword(account, password);
            if (token != null) {
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, context.getResources().getString(R.string.account_type));
                result.putString(AccountManager.KEY_AUTHTOKEN, token);
                return result;
            }
        }
        return null;
    }

    @Override public String getAuthTokenLabel(String authTokenType) {
        if (authTokenType.equals(context.getString(R.string.account_type))) {
            return context.getString(R.string.account_type);
        }
        return null;
    }

    @Override public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }

    @Override public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    /**
     * retrieve a token if user's username/password combination is ok
     * returns null in case of login failure
     */
    private String onlineConfirmPassword(Account account, String password) {
        String url = userManager.getSiteUrl(account);
        String username = userManager.getUsername(account);
        Observable<LoginResponse> response = userRepository
                .login(url, username, password);
        BlockingObservable<LoginResponse> b = response.toBlocking();
// TODO: renmae valriabels
        LoginResponse c = b.first();
        return c.statusResponse.result.pwgToken;

    }
}
