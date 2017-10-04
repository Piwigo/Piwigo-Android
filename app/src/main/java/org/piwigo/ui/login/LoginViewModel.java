/*
 * Copyright 2017 Phil Bayfield https://philio.me
 * Copyright 2017 Piwigo Team http://piwigo.org
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

import com.github.jorgecastilloprz.listeners.FABProgressListener;

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

    public ObservableField<String> url = new ObservableField<>("http://");
    public ObservableField<String> urlError = new ObservableField<>();
    public ObservableField<String> username = new ObservableField<>();
    public ObservableField<String> usernameError = new ObservableField<>();
    public ObservableField<String> password = new ObservableField<>();
    public ObservableField<String> passwordError = new ObservableField<>();
    public FABProgressCircleObservable progressCircle = new FABProgressCircleObservable();
    public FABProgressListener progressListener = new LoginFABProgressListener();

    private final UserRepository userRepository;
    private final Resources resources;

    private MutableLiveData<LoginResponse> loginSuccess = new MutableLiveData<>();
    private MutableLiveData<Throwable> loginError = new MutableLiveData<>();
    private MutableLiveData<Boolean> animationFinished = new MutableLiveData<>();

    private Subscription subscription;

    public LoginViewModel(UserRepository userRepository, Resources resources) {
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

    private class LoginFABProgressListener implements FABProgressListener {

        @Override
        public void onFABProgressAnimationEnd() {
            animationFinished.setValue(true);
        }
    }
}
