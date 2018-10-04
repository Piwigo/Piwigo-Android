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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.res.Resources;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.util.Patterns;

import org.piwigo.R;
import org.piwigo.internal.binding.observable.FABProgressCircleObservable;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.io.repository.UserRepository;

import java.util.regex.Pattern;

import rx.Subscriber;
import rx.Subscription;

public class LoginViewModel extends ViewModel {

    private static final String TAG = LoginViewModel.class.getName();

    @VisibleForTesting static Pattern WEB_URL = Patterns.WEB_URL;

    public ObservableField<String> url = new ObservableField<>("https://");
    public ObservableField<String> urlError = new ObservableField<>();
    public ObservableField<String> username = new ObservableField<>();
    public ObservableField<String> usernameError = new ObservableField<>();
    public ObservableField<String> password = new ObservableField<>();
    public ObservableField<String> passwordError = new ObservableField<>();
    public FABProgressCircleObservable progressCircle = new FABProgressCircleObservable(FABProgressCircleObservable.STATE_HIDDEN);

    private final UserRepository userRepository;
    private final Resources resources;

    private MutableLiveData<LoginResponse> loginSuccess = new MutableLiveData<>();
    private MutableLiveData<Throwable> loginError = new MutableLiveData<>();
    private MutableLiveData<Boolean> animationFinished = new MutableLiveData<>();

    private Subscription subscription;

    LoginViewModel(UserRepository userRepository, Resources resources) {
        this.userRepository = userRepository;
        this.resources = resources;

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

    public void onLoginClick() {
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

    public void onProgressAnimationEnd() {
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

    void accountCreated() {
        progressCircle.beginFinalAnimation();
    }

    void accountExists() {
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

    private class LoginSubscriber extends Subscriber<LoginResponse> {

        @Override public void onCompleted() {}

        @Override public void onError(Throwable e) {
            Log.e(TAG, e.getMessage());
            progressCircle.hide();
            loginError.setValue(e);
        }

        @Override public void onNext(LoginResponse loginResponse) {
            loginSuccess.setValue(loginResponse);
        }
    }
}
