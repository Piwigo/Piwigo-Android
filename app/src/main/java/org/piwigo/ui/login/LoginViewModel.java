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

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.content.res.Resources;

import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.annotation.VisibleForTesting;

import android.util.Log;
import android.util.Patterns;

import com.github.jorgecastilloprz.FABProgressCircle;

import org.piwigo.R;
import org.piwigo.accounts.UserManager;
import org.piwigo.helper.URLHelper;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.io.model.StatusResponse;
import org.piwigo.io.repository.UserRepository;

import java.util.regex.Pattern;

import rx.Subscriber;
import rx.Subscription;

public class LoginViewModel extends ViewModel {

    private static final String TAG = LoginViewModel.class.getName();
    private static final String HIDDEN_PASSWORD = "*****";

    @VisibleForTesting
    static Pattern WEB_URL = Patterns.WEB_URL;

    public ObservableField<String> url = new ObservableField<>();
    public ObservableField<String> urlError = new ObservableField<>();
    public ObservableField<String> username = new ObservableField<>();
    public ObservableField<String> usernameError = new ObservableField<>();
    public ObservableField<String> password = new ObservableField<>();
    public ObservableField<String> passwordError = new ObservableField<>();

    private final UserRepository userRepository;
    private final Resources resources;

    private MutableLiveData<LoginResponse> loginSuccess = new MutableLiveData<>();
    private MutableLiveData<Throwable> loginError = new MutableLiveData<>();
    private MutableLiveData<Boolean> animationFinished = new MutableLiveData<>();

    private Subscription subscription;
    private final UserManager userManager;
    private Account account = null;

    boolean unitTesting = false;

    LoginViewModel(UserManager userManager, UserRepository userRepository, Resources resources) {
        this.userRepository = userRepository;
        this.resources = resources;
        this.userManager = userManager;

        clearOnPropertyChange(url, urlError);
        clearOnPropertyChange(username, usernameError);
        clearOnPropertyChange(password, passwordError);
    }

    @Override
    protected void onCleared() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    /**
     * Handles the click event on the login button
     * Check if the URL is valid and get what protocol to use using URLHelper
     * @param fabCircle - FAB on login view (nullable for unit testing purpose..)
     *                  //TODO Find a better way to interact with the FAB to avoid Nullable arg
     */
    void onLoginClick(@Nullable FABProgressCircle fabCircle) {
        boolean siteValid = isSiteValid();
        boolean loginValid = isGuest() || isLoginValid();

        url.set(userManager.validateUrl(url.get()));

        if (!siteValid) {
            return;
        }
        if (fabCircle != null) {
            fabCircle.show();
        }

        //Trying to log in with "HTTPS" protocol first..
        testConnection(loginValid, url.get());
    }

    void testConnection(boolean loginValid, String url) {
         try {
            if (isGuest()) {
                subscription = userRepository.status(url)
                        .subscribe(new StatusSubscriber());
            } else if (loginValid) {
                subscription = userRepository.login(url, username.get(), password.get())
                        .subscribe(new LoginSubscriber());
            }
        } catch (IllegalArgumentException illArgE) {
            Log.e(TAG, illArgE.getMessage() + illArgE);
            loginError.setValue(illArgE);
        }
    }

    void onProgressAnimationEnd() {
        animationFinished.setValue(true);
    }

    LiveData<LoginResponse> getLoginSuccess() {
        return loginSuccess;
    }

    LiveData<Throwable> getLoginError() {
        return loginError;
    }

    LiveData<Boolean> getAnimationFinished() {
        return animationFinished;
    }

    boolean isEditExisting() {
        return account != null;
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

    public boolean isGuest() {
        return isEmpty(username.get()) && isEmpty(password.get());
    }

    private boolean isLoginValid() {
        boolean valid = true;

        if (isEmpty(username.get())) {
            usernameError.set(resources.getString(R.string.login_username_empty));
            valid = false;
        }

        if (isEmpty(password.get())) {
            passwordError.set(resources.getString(R.string.login_password_empty));
            valid = false;
        }

        return valid;
    }

    private void clearOnPropertyChange(ObservableField source, ObservableField<String> fieldToClear) {
        source.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                fieldToClear.set(null);
            }
        });
    }

    private boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    void loadAccount(Account account) {
        if (account != null) {
            url.set(userManager.getSiteUrl(account));
            if (userManager.isGuest(account)) {
                username.set("");
                password.set("");
            } else {
                username.set(userManager.getUsername(account));
                password.set(HIDDEN_PASSWORD);
            }
        }
        this.account = account;
    }

    private class LoginSubscriber extends Subscriber<LoginResponse> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e)  {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            loginError.setValue(e);
         }

        @Override
        public void onNext(LoginResponse loginResponse) {
            Log.d(TAG, "login was success " + loginResponse.url);
            if (account != null) {
                userManager.updateAccount(account, url.get(), username.get(), password.get());
            }
            loginSuccess.setValue(loginResponse);
        }
    }

    private class StatusSubscriber extends Subscriber<StatusResponse> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, e.getMessage());
            loginError.setValue(e);
        }

        @Override
        public void onNext(StatusResponse response) {
            try {
                if (account != null) {
                    userManager.updateAccount(account, url.get(), username.get(), password.get());
                }
                // fake login response
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.url = url.get();
                loginResponse.username = username.get();
                loginResponse.password = password.get();
                loginSuccess.setValue(loginResponse);
            } catch (IllegalArgumentException e) {
                loginError.setValue(e);
            }
        }
    }
}
