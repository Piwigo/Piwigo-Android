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

package org.piwigo.ui.viewmodel;

import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import org.piwigo.R;
import org.piwigo.internal.binding.observable.EditTextObservable;
import org.piwigo.io.model.response.StatusResponse;
import org.piwigo.io.repository.UserRepository;

import java.util.regex.Pattern;

import javax.inject.Inject;

import rx.Subscriber;

public class LoginViewModel extends BaseViewModel {

    private static final String TAG = LoginViewModel.class.getName();

    @VisibleForTesting static final String STATE_URL = "url";
    @VisibleForTesting static final String STATE_USERNAME = "username";
    @VisibleForTesting static final String STATE_PASSWORD = "password";

    @VisibleForTesting static Pattern WEB_URL = Patterns.WEB_URL;

    public EditTextObservable url = new EditTextObservable("http://");
    public EditTextObservable username = new EditTextObservable();
    public EditTextObservable password = new EditTextObservable();

    @Inject UserRepository userRepository;

    @Inject public LoginViewModel() {}

    @Override public void onSave(Bundle outState) {
        outState.putString(STATE_URL, url.get());
        outState.putString(STATE_USERNAME, username.get());
        outState.putString(STATE_PASSWORD, password.get());
    }

    @Override public void onRestore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            url.set(savedInstanceState.getString(STATE_URL));
            username.set(savedInstanceState.getString(STATE_USERNAME));
            password.set(savedInstanceState.getString(STATE_PASSWORD));
        }
    }

    public void onLoginClick(View view) {
        if (isValid()) {
            userRepository
                    .login(url.get(), username.get(), password.get())
                    .subscribe(new LoginSubscriber());
        }
    }

    private boolean isValid() {
        boolean valid = true;

        if (url.get() == null || url.get().isEmpty()) {
            url.setError(R.string.login_url_empty);
            valid = false;
        } else if (!WEB_URL.matcher(url.get()).matches()) {
            url.setError(R.string.login_url_invalid);
            valid = false;
        }

        if (username.get() == null || username.get().isEmpty()) {
            username.setError(R.string.login_username_empty);
            valid = false;
        }

        if (password.get() == null || password.get().isEmpty()) {
            password.setError(R.string.login_password_empty);
            valid = false;
        }

        return valid;
    }

    private class LoginSubscriber extends Subscriber<StatusResponse> {

        @Override public void onCompleted() {

        }

        @Override public void onError(Throwable e) {
            Log.e(TAG, e.getMessage());
        }

        @Override public void onNext(StatusResponse statusResponse) {

        }

    }

}
