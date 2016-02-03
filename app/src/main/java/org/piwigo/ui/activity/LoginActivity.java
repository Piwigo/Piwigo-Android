/*
 * Copyright 2015 Phil Bayfield https://philio.me
 * Copyright 2015 Piwigo Team http://piwigo.org
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

package org.piwigo.ui.activity;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;

import org.piwigo.R;
import org.piwigo.databinding.ActivityLoginBinding;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.ui.view.LoginView;
import org.piwigo.ui.viewmodel.LoginViewModel;

import javax.inject.Inject;

public class LoginActivity extends BaseActivity implements LoginView {

    @Inject LoginViewModel viewModel;

    private AccountAuthenticatorResponse authenticatorResponse;
    private Bundle resultBundle;

    private ActivityLoginBinding binding;

    private Handler handler = new Handler();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityComponent().inject(this);

        // Account authenticator stuff, applicable if user comes from "add account" in settings only
        // as ordinarily this is skipped to enable activity scene transitions
        authenticatorResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (authenticatorResponse != null) {
            authenticatorResponse.onRequestContinued();
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        viewModel.setView(this);
        bindLifecycleEvents(viewModel);
        binding.setViewModel(viewModel);
    }

    @Override public void finish() {
        /** Clean up the account authenticator stuff, see {@link AccountAuthenticatorActivity} */
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

    @Override public void onSuccess(LoginResponse response) {
        if (accountHelper.accountExists(response)) {
            Snackbar.make(binding.getRoot(), R.string.login_account_error, Snackbar.LENGTH_LONG)
                    .show();
            viewModel.onAccountExists();
        } else {
            Account account = accountHelper.createAccount(response);
            setResultIntent(account);
            viewModel.onAccountCreated();
        }
    }

    @Override public void onError() {
        Snackbar.make(binding.getRoot(), R.string.login_error, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override public void onAnimationFinished() {
        handler.postDelayed(this::finishAfterTransition, 500);
    }

    private void setResultIntent(Account account) {
        Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        resultBundle = intent.getExtras();
        setResult(RESULT_OK, intent);
    }

}
