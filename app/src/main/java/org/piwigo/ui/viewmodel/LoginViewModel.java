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

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.github.jorgecastilloprz.listeners.FABProgressListener;

import org.piwigo.R;
import org.piwigo.internal.binding.observable.EditTextObservable;
import org.piwigo.internal.binding.observable.ErrorObservable;
import org.piwigo.internal.binding.observable.FABProgressCircleObservable;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.io.repository.UserRepository;
import org.piwigo.ui.view.LoginView;

import java.util.regex.Pattern;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;

public class LoginViewModel extends BaseViewModel {

    private static final String TAG = LoginViewModel.class.getName();

    @VisibleForTesting static final String STATE_URL = "url";
    @VisibleForTesting static final String STATE_USERNAME = "username";
    @VisibleForTesting static final String STATE_PASSWORD = "password";

    @VisibleForTesting static Pattern WEB_URL = Patterns.WEB_URL;

    public EditTextObservable url = new EditTextObservable("http://");
    public ErrorObservable urlError = new ErrorObservable();
    public EditTextObservable username = new EditTextObservable();
    public ErrorObservable usernameError = new ErrorObservable();
    public EditTextObservable password = new EditTextObservable();
    public ErrorObservable passwordError = new ErrorObservable();
    public FABProgressCircleObservable progressCircle = new FABProgressCircleObservable();
    public FABProgressListener progressListener = new LoginFABProgressListener();

    @Inject UserRepository userRepository;
    @Inject Resources resources;

    private LoginView view;
    private Subscription subscription;

    @Inject public LoginViewModel() {}

    public void setView(LoginView view) {
        this.view = view;
    }

    @Override public void onSaveState(Bundle outState) {
        outState.putString(STATE_URL, url.get());
        outState.putString(STATE_USERNAME, username.get());
        outState.putString(STATE_PASSWORD, password.get());
    }

    @Override public void onRestoreState(Bundle savedState) {
        if (savedState != null) {
            url.set(savedState.getString(STATE_URL));
            username.set(savedState.getString(STATE_USERNAME));
            password.set(savedState.getString(STATE_PASSWORD));
        }
    }

    @Override public void onDestroy() {
        view = null;
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    public void onLoginClick(View view) {
        boolean siteValid = isSiteValid();
        boolean loginValid = isGuest() || isLoginValid();

        if (siteValid) {
            if (isGuest()) {
                progressCircle.show();
                subscription = userRepository
                        .status(url.get())
                        .subscribe(new LoginSubscriber());
            } else if (loginValid) {
                progressCircle.show();
                subscription = userRepository
                        .login(url.get(), username.get(), password.get())
                        .subscribe(new LoginSubscriber());
            }
        }
    }

    public void onAccountCreated() {
        progressCircle.beginFinalAnimation();
    }

    public void onAccountExists() {
        progressCircle.hide();
    }

    private boolean isSiteValid() {
        if (url.get() == null || url.get().isEmpty()) {
            urlError.set(resources.getString(R.string.login_url_empty));
            return false;
        } else if (!WEB_URL.matcher(url.get()).matches()) {
            urlError.set(resources.getString(R.string.login_url_invalid));
            return false;
        }
        return true;
    }

    private boolean isGuest() {
        return username.isEmpty() && password.isEmpty();
    }

    private boolean isLoginValid() {
        boolean valid = true;

        if (username.isEmpty()) {
            usernameError.set(resources.getString(R.string.login_username_empty));
            valid = false;
        }

        if (password.isEmpty()) {
            passwordError.set(resources.getString(R.string.login_password_empty));
            valid = false;
        }

        return valid;
    }

    private boolean hasView() {
        return view != null;
    }

    private class LoginSubscriber extends Subscriber<LoginResponse> {

        @Override public void onCompleted() {}

        @Override public void onError(Throwable e) {
            Log.e(TAG, e.getMessage());
            progressCircle.hide();
        }

        @Override public void onNext(LoginResponse loginResponse) {
            view.onSuccess(loginResponse);
        }

    }

    private class LoginFABProgressListener implements FABProgressListener {

        @Override
        public void onFABProgressAnimationEnd() {
            if (hasView()) {
                view.onAnimationFinished();
            }
        }

    }

}
