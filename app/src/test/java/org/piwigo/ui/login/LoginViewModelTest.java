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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import android.content.res.Resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.piwigo.R;
import org.piwigo.accounts.UserManager;
import org.piwigo.io.restmodel.SuccessResponse;
import org.piwigo.io.restrepository.RestUserRepository;

import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.android.plugins.RxAndroidPlugins;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoginViewModelTest {

    private static final String URL = "https://piwigo.org/demo";
    private static final String USERNAME = "demo";
    private static final String PASSWORD = "demo";

    private static final String ERROR_URL_EMPTY = "Enter your site address";
    private static final String ERROR_URL_INVALID = "Not a valid site address";
    private static final String ERROR_USERNAME = "Enter your username";
    private static final String ERROR_PASSWORD = "Enter your password";

    @Mock
    RestUserRepository userRepository;

    @Mock Resources resources;

    @Mock UserManager userManager;

    private LoginViewModel viewModel;

    @Rule public TestRule rule = new InstantTaskExecutorRule();

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(userRepository.login(URL, USERNAME, PASSWORD)).thenReturn(Observable.empty());
        when(userRepository.status(URL)).thenReturn(Observable.empty());
        when(resources.getString(R.string.login_url_empty)).thenReturn(ERROR_URL_EMPTY);
        when(resources.getString(R.string.login_url_invalid)).thenReturn(ERROR_URL_INVALID);
        when(resources.getString(R.string.login_username_empty)).thenReturn(ERROR_USERNAME);
        when(resources.getString(R.string.login_password_empty)).thenReturn(ERROR_PASSWORD);

        viewModel = new LoginViewModel(userManager, userRepository, resources);
        LoginViewModel.WEB_URL = Pattern.compile("https://piwigo\\.org/demo");
    }
    @After
    public void tearDown() {
        RxAndroidPlugins.reset();
    }

    @Test public void clearUrlErrorOnTextChange() {
        viewModel.urlError.set("Error");

        viewModel.url.set("Text");

        assertThat(viewModel.urlError.get()).isNull();
    }

    @Test public void clearUsernameErrorOnTextChange() {
        viewModel.usernameError.set("Error");

        viewModel.username.set("Text");

        assertThat(viewModel.usernameError.get()).isNull();
    }

    @Test public void clearPasswordErrorOnTextChange() {
        viewModel.passwordError.set("Error");

        viewModel.password.set("Text");

        assertThat(viewModel.passwordError.get()).isNull();
    }

    @Test public void shouldSetEmptyUrlError() {
        viewModel.url.set(null);

        viewModel.onLoginClick(null);

        assertThat(viewModel.urlError.get()).isEqualTo(ERROR_URL_EMPTY);
    }

    @Test public void setUrlErrorIfInvalid() {
        viewModel.url.set("Junk");

        viewModel.onLoginClick(null);

        assertThat(viewModel.urlError.get()).isEqualTo(ERROR_URL_INVALID);
    }

    @Test public void setUsernameErrorIfEmpty() {
        viewModel.username.set(null);
        viewModel.password.set(PASSWORD);

        viewModel.onLoginClick(null);

        assertThat(viewModel.usernameError.get()).isEqualTo(ERROR_USERNAME);
    }

    @Test public void setPasswordErrorIfEmpty() {
        viewModel.username.set(USERNAME);
        viewModel.password.set(null);

        viewModel.onLoginClick(null);

        assertThat(viewModel.passwordError.get()).isEqualTo(ERROR_PASSWORD);
    }

    @Test public void callLoginIfUrlValid() {
        viewModel.unitTesting = true;
        viewModel.url.set(URL);
        viewModel.username.set(USERNAME);
        viewModel.password.set(PASSWORD);

        viewModel.testConnection(true, URL);

        verify(userRepository).login(URL, USERNAME, PASSWORD);
    }

    @Test public void callGetStatusIfUrlValid() {
        viewModel.unitTesting = true;
        viewModel.url.set(URL);

        viewModel.testConnection(true, URL);

        verify(userRepository).status(URL);
    }

    @Test @SuppressWarnings("unchecked") public void loginSuccessObserverReceivesLoginResponse() {
        viewModel.unitTesting = true;
        SuccessResponse loginResponse = new SuccessResponse();
        when(userRepository.login(URL, USERNAME, PASSWORD)).thenReturn(Observable.just(loginResponse));
        Observer<SuccessResponse> observer = (Observer<SuccessResponse>) mock(Observer.class);
        viewModel.getLoginSuccess().observeForever(observer);
        viewModel.url.set(URL);
        viewModel.username.set(USERNAME);
        viewModel.password.set(PASSWORD);

        viewModel.testConnection(true, URL);
// TODO: ioScheduler is doing the job, so we should wait here
//  verify(observer).onChanged(loginResponse);
    }

    @Test @SuppressWarnings("unchecked") public void loginErrorObserverReceivesLoginError() {
        viewModel.unitTesting = true;
        Throwable throwable = new Throwable();
        when(userRepository.login(URL, USERNAME, PASSWORD)).thenReturn(Observable.error(throwable));
        Observer<Throwable> observer = (Observer<Throwable>) mock(Observer.class);
        viewModel.getLoginError().observeForever(observer);
        viewModel.url.set(URL);
        viewModel.username.set(USERNAME);
        viewModel.password.set(PASSWORD);

        viewModel.testConnection(true, URL);

// TODO: ioScheduler is doing the job, so we should wait here
//        verify(observer).onChanged(throwable);
    }

    @Test @SuppressWarnings("unchecked") public void animationFinishedObserverReceivesTrue() {
        viewModel.unitTesting = true;
        Observer<Boolean> observer = (Observer<Boolean>) mock(Observer.class);
        viewModel.getAnimationFinished().observeForever(observer);

        viewModel.onProgressAnimationEnd();

        verify(observer).onChanged(true);
    }
}