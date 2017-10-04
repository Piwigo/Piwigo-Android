package org.piwigo.ui.login;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;
import android.content.res.Resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.piwigo.R;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.io.repository.UserRepository;

import java.util.regex.Pattern;

import rx.Observable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoginViewModelTest {

    private static final String URL = "http://demo.piwigo.org";
    private static final String USERNAME = "demo";
    private static final String PASSWORD = "demo";

    private static final String ERROR_URL_EMPTY = "Enter your site address";
    private static final String ERROR_URL_INVALID = "Not a valid site address";
    private static final String ERROR_USERNAME = "Enter your username";
    private static final String ERROR_PASSWORD = "Enter your password";

    @Mock UserRepository userRepository;

    @Mock Resources resources;

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

        viewModel = new LoginViewModel(userRepository, resources);
        LoginViewModel.WEB_URL = Pattern.compile("http://demo\\.piwigo\\.org");
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

        viewModel.onLoginClick();

        assertThat(viewModel.urlError.get()).isEqualTo(ERROR_URL_EMPTY);
    }

    @Test public void setUrlErrorIfInvalid() {
        viewModel.url.set("Junk");

        viewModel.onLoginClick();

        assertThat(viewModel.urlError.get()).isEqualTo(ERROR_URL_INVALID);
    }

    @Test public void setUsernameErrorIfEmpty() {
        viewModel.username.set(null);
        viewModel.password.set(PASSWORD);

        viewModel.onLoginClick();

        assertThat(viewModel.usernameError.get()).isEqualTo(ERROR_USERNAME);
    }

    @Test public void setPasswordErrorIfEmpty() {
        viewModel.username.set(USERNAME);
        viewModel.password.set(null);

        viewModel.onLoginClick();

        assertThat(viewModel.passwordError.get()).isEqualTo(ERROR_PASSWORD);
    }

    @Test public void callloginIfUrlValid() {
        viewModel.url.set(URL);
        viewModel.username.set(USERNAME);
        viewModel.password.set(PASSWORD);

        viewModel.onLoginClick();

        verify(userRepository).login(URL, USERNAME, PASSWORD);
    }

    @Test public void callGetStatusIfUrlValid() {
        viewModel.url.set(URL);

        viewModel.onLoginClick();

        verify(userRepository).status(URL);
    }

    @Test @SuppressWarnings("unchecked") public void loginSuccessObserverReceivesLoginResponse() {
        LoginResponse loginResponse = new LoginResponse();
        when(userRepository.login(URL, USERNAME, PASSWORD)).thenReturn(Observable.just(loginResponse));
        Observer<LoginResponse> observer = (Observer<LoginResponse>) mock(Observer.class);
        viewModel.getLoginSuccess().observeForever(observer);
        viewModel.url.set(URL);
        viewModel.username.set(USERNAME);
        viewModel.password.set(PASSWORD);

        viewModel.onLoginClick();

        verify(observer).onChanged(loginResponse);
    }

    @Test @SuppressWarnings("unchecked") public void loginErrorObserverReceivesLoginError() {
        Throwable throwable = new Throwable();
        when(userRepository.login(URL, USERNAME, PASSWORD)).thenReturn(Observable.error(throwable));
        Observer<Throwable> observer = (Observer<Throwable>) mock(Observer.class);
        viewModel.getLoginError().observeForever(observer);
        viewModel.url.set(URL);
        viewModel.username.set(USERNAME);
        viewModel.password.set(PASSWORD);

        viewModel.onLoginClick();

        verify(observer).onChanged(throwable);
    }

    @Test @SuppressWarnings("unchecked") public void animationFinishedObserverReceivesTrue() {
        Observer<Boolean> observer = (Observer<Boolean>) mock(Observer.class);
        viewModel.getAnimationFinished().observeForever(observer);

        viewModel.progressListener.onFABProgressAnimationEnd();

        verify(observer).onChanged(true);
    }
}