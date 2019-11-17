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

package org.piwigo.ui.login;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;

import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.material.snackbar.Snackbar;

import org.piwigo.R;
import org.piwigo.databinding.ActivityLoginBinding;
import org.piwigo.helper.DialogHelper;
import org.piwigo.io.PiwigoLoginException;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.ui.shared.BaseActivity;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;

import javax.inject.Inject;
import javax.net.ssl.SSLPeerUnverifiedException;

import dagger.android.AndroidInjection;
import retrofit2.HttpException;

public class LoginActivity extends BaseActivity {

    public static final String EDIT_ACCOUNT_ACTION = "org.piwigo.action.EDIT_ACCOUNT";
    public static final String PARAM_ACCOUNT = "account";
    @Inject
    LoginViewModelFactory viewModelFactory;

    private LoginViewModel viewModel;
    private ActivityLoginBinding binding;

    private FABProgressCircle fabProgressCircle;

    private AccountAuthenticatorResponse authenticatorResponse;
    private Bundle resultBundle;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        setTheme(R.style.Theme_Piwigo_Login);
        super.onCreate(savedInstanceState);

        // Account authenticator stuff, applicable if user comes from "add account" in settings only
        // as ordinarily this is skipped to enable activity scene transitions
        authenticatorResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (authenticatorResponse != null) {
            authenticatorResponse.onRequestContinued();
        }
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LoginViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setViewModel(viewModel);

        fabProgressCircle = findViewById(R.id.fabLoginCircle);

        viewModel.getLoginSuccess().observe(this, this::loginSuccess);
        viewModel.getLoginError().observe(this, this::loginError);

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        Account account = null;

        if (EDIT_ACCOUNT_ACTION.equals(intent.getAction())) {
            account = intent.getParcelableExtra(PARAM_ACCOUNT);
        }

        viewModel.loadAccount(account);
    }

    public void onClick(View v)
    {
        viewModel.onLoginClick(fabProgressCircle);
    }

    /**
     * Clean up the account authenticator stuff, see {@link AccountAuthenticatorActivity}
     */
    @Override
    public void finish() {
        if (authenticatorResponse != null) {
            if (resultBundle != null) {
                authenticatorResponse.onResult(resultBundle);
            } else {
                authenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED, "canceled");
            }
            authenticatorResponse = null;
        }
        super.finish();
    }

    private void loginSuccess(LoginResponse response) {
        fabProgressCircle.hide();
        if (viewModel.isEditExisting()) {
            finish();
        } else if (userManager.userExists(response.url, response.username)) {
            Snackbar.make(binding.getRoot(), R.string.login_account_error, Snackbar.LENGTH_LONG)
                    .show();
        } else {
            Account account = userManager.createUser(response.url, response.statusResponse.result.username, response.password, response.pwgId, response.statusResponse.result.pwgToken);
            userManager.setActiveAccount(account);
            setResultIntent(account);
            finish();
        }
    }

    private void loginError(Throwable throwable) {
        fabProgressCircle.hide();
        String msg;
        URI uri;
        String host = viewModel.url.get();
        String path = host;
        try {
            uri = new URI(viewModel.url.get());
            if(!uri.isAbsolute()) {
                uri = uri.resolve(new URI("https://" + host));
            }
            host = uri.getHost();
            path = uri.getPath();
        } catch (URISyntaxException e) {
            /* this one should not occur, otherwise we should not even login */
        }

        if(throwable instanceof IllegalArgumentException){
            msg = getResources().getString(R.string.login_baseurl_invalid, path);
        }else if(throwable instanceof HttpException){
            HttpException he = (HttpException) throwable;
            if(he.code() == 404){
                List<String> segments = he.response().raw().request().url().pathSegments();
                if("ws.php".equals(segments.get(segments.size() - 1))){
                    msg = getResources().getString(R.string.login_baseurl_invalid, path);
                }else{
                    msg = getResources().getString(R.string.login_http_404_error, he.response().raw().request().url().encodedPath());
                }
            }else {
                msg = getResources().getString(R.string.login_http_error, he.code() + ": " + he.message());
            }
        }else if (throwable instanceof SSLPeerUnverifiedException) {
            msg = getResources().getString(R.string.login_ssl_error, host);
        }else if (throwable instanceof UnknownHostException) {
            msg = getResources().getString(R.string.login_host_error, host);
        }else if (throwable instanceof PiwigoLoginException) {
            msg = getResources().getString(R.string.login_invalid_credentials, viewModel.username.get());
        }else {
            msg = getResources().getString(R.string.login_error);
        }

        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).setAction(R.string.show_details, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.INSTANCE.showLogDialog(getResources().getString(R.string.login_error), msg, throwable, "URL: " + viewModel.url.get() + ", GUEST: " + (viewModel.isGuest() ? "TRUE" : "FALSE") + ", EDIT_EXISTING: " + (viewModel.isEditExisting() ? "TRUE" : "FALSE"), binding.getRoot().getContext());
            }
        }).show();
    }


    private void setResultIntent(Account account) {
        Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        resultBundle = intent.getExtras();
        setResult(RESULT_OK, intent);
    }
}
