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
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.util.Patterns;

import com.github.jorgecastilloprz.FABProgressCircle;

import org.piwigo.R;
import org.piwigo.accounts.UserManager;
import org.piwigo.helper.URLHelper;
import org.piwigo.io.PiwigoLoginException;
import org.piwigo.io.restmodel.StatusResponse;
import org.piwigo.io.restmodel.SuccessResponse;
import org.piwigo.io.restrepository.RestUserRepository;

import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

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

    private final RestUserRepository userRepository;
    private final Resources resources;

    private MutableLiveData<SuccessResponse> loginSuccess = new MutableLiveData<>();
    private MutableLiveData<Throwable> loginError = new MutableLiveData<>();
    private MutableLiveData<Boolean> animationFinished = new MutableLiveData<>();

    private final UserManager userManager;
    private Account account = null;

    boolean unitTesting = false;
    private String testedUrl = "";

    LoginViewModel(UserManager userManager, RestUserRepository userRepository, Resources resources) {
        this.userRepository = userRepository;
        this.resources = resources;
        this.userManager = userManager;

        clearOnPropertyChange(url, urlError);
        clearOnPropertyChange(username, usernameError);
        clearOnPropertyChange(password, passwordError);
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

        if (!siteValid) {
            return;
        }
        if (fabCircle != null) {
            fabCircle.show();
        }

        //Trying to log in with "HTTPS" protocol first..
        testConnection(loginValid, URLHelper.INSTANCE.getUrlWithMethod(url.get(), "https"));
    }

    void testConnection(boolean loginValid, String url) {
        testedUrl = url;
        try {
            if (isGuest()) {
                userRepository.status(url)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new StatusSubscriber());
            } else if (loginValid) {
                userRepository.login(url, username.get(), password.get())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
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

    LiveData<SuccessResponse> getLoginSuccess() {
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

    private class LoginSubscriber extends DisposableObserver<SuccessResponse> {

        @Override
        public void onComplete() {

        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, e.getMessage());
            loginError.setValue(e);

            /**
             *  HTTPS login did fail - retrying with HTTPS
             *  Checking for 'https' in string to avoid infinite loop..
             */
            if (testedUrl != null && testedUrl.contains("https") && !unitTesting)
                testConnection(true, URLHelper.INSTANCE.getUrlWithMethod(url.get(), "http"));
        }

        @Override
        public void onNext(SuccessResponse loginResponse) {
            Log.d(TAG, "login was successfull '" + loginResponse.stat + "'");
            if (loginResponse.stat.equals("fail")) {
                onError(new PiwigoLoginException("Login for user '" + username.get() + "' failed with: " + loginResponse.message));
                return;
            }
            try {
                if (account != null) {
                    userManager.updateAccount(account, url.get(), username.get(), password.get());
                }
                loginSuccess.setValue(loginResponse);
            } catch (IllegalArgumentException e) {
                loginError.setValue(e);
            }
        }
    }

    private class StatusSubscriber extends DisposableObserver<StatusResponse> {

        public void onComplete() {
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, e.getMessage());
            loginError.setValue(e);
        }

        @Override
        public void onNext(StatusResponse response) {
            Log.d(TAG, "status was successful");
            try {
                if (account != null) {
                    userManager.updateAccount(account, url.get(), username.get(), password.get());
                }
                // fake login response
                SuccessResponse loginResponse = new SuccessResponse();
                loginSuccess.setValue(loginResponse);
            } catch (IllegalArgumentException e) {
                loginError.setValue(e);
            }
        }
    }
}
