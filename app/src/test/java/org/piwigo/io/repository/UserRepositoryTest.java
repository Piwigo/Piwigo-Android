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

package org.piwigo.io.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.piwigo.io.RestService;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.io.model.StatusResponse;
import org.piwigo.io.model.SuccessResponse;

import okhttp3.Headers;
import retrofit2.Response;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserRepositoryTest {

    private static final String URL = "http://demo.piwigo.org/";
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";
    private static final String BAD_CREDENTIAL = "bad";
    private static final String GUEST_USER = "guest";
    private static final String COOKIE_PWG_ID = "1234567890";
    private static final String TOKEN = "abcdefghijklmnop";
    private static final String STATUS_OK = "ok";
    private static final String STATUS_FAIL = "fail";

    @Mock RestServiceFactory restServiceFactory;
    @Mock RestService restService;

    private UserRepository userRepository;

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(restServiceFactory.createForUrl(anyString())).thenReturn(restService);
        when(restService.getStatus()).thenReturn(getStatusResponse(GUEST_USER));
        when(restService.login(USERNAME, PASSWORD)).thenReturn(getLoginSuccessResponse());
        when(restService.login(BAD_CREDENTIAL, BAD_CREDENTIAL)).thenReturn(getLoginFailureResponse());

        userRepository = new UserRepository(restServiceFactory, Schedulers.immediate(), Schedulers.immediate());
    }

    @Test public void login_withValidCredentials_returnsSuccessResponse() {
        when(restService.getStatus("pwg_id=" + COOKIE_PWG_ID)).thenReturn(getStatusResponse(USERNAME));

        Observable<LoginResponse> observable = userRepository.login(URL, USERNAME, PASSWORD);
        TestSubscriber<LoginResponse> subscriber = new TestSubscriber<>();
        observable.subscribe(subscriber);

        subscriber.assertNoErrors();
        LoginResponse loginResponse = subscriber.getOnNextEvents().get(0);
        verify(restServiceFactory).createForUrl(URL);
        assertThat(loginResponse.url).isEqualTo(URL);
        assertThat(loginResponse.username).isEqualTo(USERNAME);
        assertThat(loginResponse.password).isEqualTo(PASSWORD);
        assertThat(loginResponse.pwgId).isEqualTo(COOKIE_PWG_ID);
        assertThat(loginResponse.statusResponse.stat).isEqualTo(STATUS_OK);
        assertThat(loginResponse.statusResponse.result.pwgToken).isEqualTo(TOKEN);
        assertThat(loginResponse.statusResponse.result.username).isEqualTo(USERNAME);
    }

    @Test public void login_withInvalidCredentials_returnsErrorResponse() {
        Observable<LoginResponse> observable = userRepository.login(URL, BAD_CREDENTIAL, BAD_CREDENTIAL);
        TestSubscriber<LoginResponse> subscriber = new TestSubscriber<>();
        observable.subscribe(subscriber);

        subscriber.assertError(Throwable.class);
    }

    @Test public void status_returnsStatusResponse() {
        Observable<LoginResponse> observable = userRepository.status(URL);
        TestSubscriber<LoginResponse> subscriber = new TestSubscriber<>();
        observable.subscribe(subscriber);

        subscriber.assertNoErrors();
        LoginResponse loginResponse = subscriber.getOnNextEvents().get(0);
        verify(restServiceFactory).createForUrl(URL);
        assertThat(loginResponse.url).isEqualTo(URL);
        assertThat(loginResponse.statusResponse.stat).isEqualTo(STATUS_OK);
        assertThat(loginResponse.statusResponse.result.username).isEqualTo(GUEST_USER);
    }

    private Observable<StatusResponse> getStatusResponse(String user) {
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.stat = STATUS_OK;
        statusResponse.result = new StatusResponse.Status();
        statusResponse.result.username = user;
        statusResponse.result.pwgToken = TOKEN;
        return Observable.just(statusResponse);
    }

    private Observable<Response<SuccessResponse>> getLoginSuccessResponse() {
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.stat = STATUS_OK;
        successResponse.result = true;
        Response<SuccessResponse> response = Response.success(successResponse, Headers.of("Set-Cookie", "pwg_id=" + COOKIE_PWG_ID));
        return Observable.just(response);
    }

    private Observable<Response<SuccessResponse>> getLoginFailureResponse() {
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.stat = STATUS_FAIL;
        Response<SuccessResponse> response = Response.success(successResponse);
        return Observable.just(response);
    }
}