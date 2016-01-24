package org.piwigo.ui.viewmodel;

import android.content.res.Resources;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.piwigo.R;
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

    private LoginViewModel viewModel;

    @Before public void setUp() {
        viewModel = new LoginViewModel();
        LoginViewModel.WEB_URL = Pattern.compile("http://demo\\.piwigo\\.org");
        viewModel.userRepository = mock(UserRepository.class);
        when(viewModel.userRepository.login(URL, USERNAME, PASSWORD)).thenReturn(Observable.empty());
        when(viewModel.userRepository.status(URL)).thenReturn(Observable.empty());
        viewModel.resources = mock(Resources.class);
        when(viewModel.resources.getString(R.string.login_url_empty)).thenReturn(ERROR_URL_EMPTY);
        when(viewModel.resources.getString(R.string.login_url_invalid)).thenReturn(ERROR_URL_INVALID);
        when(viewModel.resources.getString(R.string.login_username_empty)).thenReturn(ERROR_USERNAME);
        when(viewModel.resources.getString(R.string.login_password_empty)).thenReturn(ERROR_PASSWORD);
    }

    @Test public void shouldSaveState() {
        viewModel.url.set(URL);
        viewModel.username.set(USERNAME);
        viewModel.password.set(PASSWORD);
        Bundle bundle = mock(Bundle.class);
        viewModel.onSaveState(bundle);
        verify(bundle).putString(LoginViewModel.STATE_URL, URL);
        verify(bundle).putString(LoginViewModel.STATE_USERNAME, USERNAME);
        verify(bundle).putString(LoginViewModel.STATE_PASSWORD, PASSWORD);
    }

    @Test public void shouldRestoreState() {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getString(LoginViewModel.STATE_URL)).thenReturn(URL);
        when(bundle.getString(LoginViewModel.STATE_USERNAME)).thenReturn(USERNAME);
        when(bundle.getString(LoginViewModel.STATE_PASSWORD)).thenReturn(PASSWORD);
        viewModel.onRestoreState(bundle);
        assertThat(viewModel.url.get()).isEqualTo(URL);
        assertThat(viewModel.username.get()).isEqualTo(USERNAME);
        assertThat(viewModel.password.get()).isEqualTo(PASSWORD);
    }

    @Test public void shouldSetEmptyUrlError() {
        viewModel.url.set(null);
        viewModel.onLoginClick(null);
        assertThat(viewModel.urlError.get()).isEqualTo(ERROR_URL_EMPTY);
    }

    @Test public void shouldSetInvalidUrlError() {
        viewModel.url.set("Junk");
        viewModel.onLoginClick(null);
        assertThat(viewModel.urlError.get()).isEqualTo(ERROR_URL_INVALID);
    }

    @Test public void shouldSetEmptyUsernameError() {
        viewModel.username.set(null);
        viewModel.password.set(PASSWORD);
        viewModel.onLoginClick(null);
        assertThat(viewModel.usernameError.get()).isEqualTo(ERROR_USERNAME);
    }

    @Test public void shouldSetEmptyPasswordError() {
        viewModel.username.set(USERNAME);
        viewModel.password.set(null);
        viewModel.onLoginClick(null);
        assertThat(viewModel.passwordError.get()).isEqualTo(ERROR_PASSWORD);
    }

    @Test public void shouldLoginIfValid() {
        viewModel.url.set(URL);
        viewModel.username.set(USERNAME);
        viewModel.password.set(PASSWORD);
        viewModel.onLoginClick(null);
        verify(viewModel.userRepository).login(URL, USERNAME, PASSWORD);
    }

    @Test public void shouldGetStatusIfUrlValid() {
        viewModel.url.set(URL);
        viewModel.onLoginClick(null);
        verify(viewModel.userRepository).status(URL);
    }

}