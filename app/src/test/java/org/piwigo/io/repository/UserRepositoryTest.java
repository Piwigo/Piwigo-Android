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

package org.piwigo.io.repository;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.piwigo.io.DynamicEndpoint;
import org.piwigo.io.MockRestService;
import org.piwigo.io.RestService;
import org.piwigo.io.Session;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.io.model.StatusResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserRepositoryTest {

    private static final String URL = "http://demo.piwigo.org";
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";
    private static final String BAD_CREDENTIAL = "bad";
    private static final String GUEST_USER = "guest";
    private static final String COOKIE_PWG_ID = "1234567890";
    private static final String STATUS_OK = "ok";
    private static final String TOKEN = "abcdefghijklmnop";

    @Mock Session session;
    @Mock DynamicEndpoint dynamicEndpoint;
    @Mock RestService restService;

    private UserRepository userRepository;

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);
        userRepository = new UserRepository();
        userRepository.session = session;
        userRepository.endpoint = dynamicEndpoint;
        userRepository.restService = restService;
        userRepository.gson = new Gson();
        userRepository.ioScheduler = Schedulers.immediate();
        userRepository.uiScheduler = Schedulers.immediate();
        when(restService.getStatus()).thenReturn(getStatusResponse(GUEST_USER));
        when(restService.login(USERNAME, PASSWORD)).thenReturn(getLoginSuccessResponse());
        when(restService.login(BAD_CREDENTIAL, BAD_CREDENTIAL)).thenReturn(getLoginFailureResponse());
    }

    @Test public void loginSuccess() {
        when(restService.getStatus()).thenReturn(getStatusResponse(USERNAME));

        Observable<LoginResponse> observable = userRepository.login(URL, USERNAME, PASSWORD);
        TestSubscriber<LoginResponse> subscriber = new TestSubscriber<>();
        observable.subscribe(subscriber);

        subscriber.assertNoErrors();
        LoginResponse loginResponse = subscriber.getOnNextEvents().get(0);
        verify(dynamicEndpoint).setUrl(URL);
        verify(session).setCookie(COOKIE_PWG_ID);
        assertThat(loginResponse.url).isEqualTo(URL);
        assertThat(loginResponse.username).isEqualTo(USERNAME);
        assertThat(loginResponse.password).isEqualTo(PASSWORD);
        assertThat(loginResponse.pwgId).isEqualTo(COOKIE_PWG_ID);
        assertThat(loginResponse.statusResponse.stat).isEqualTo(STATUS_OK);
        assertThat(loginResponse.statusResponse.result.pwgToken).isEqualTo(TOKEN);
        assertThat(loginResponse.statusResponse.result.username).isEqualTo(USERNAME);
    }

    @Test public void loginError() {
        Observable<LoginResponse> observable = userRepository.login(URL, BAD_CREDENTIAL, BAD_CREDENTIAL);
        TestSubscriber<LoginResponse> subscriber = new TestSubscriber<>();
        observable.subscribe(subscriber);

        subscriber.assertError(Throwable.class);
    }

    @Test public void shouldGetStatus() {
        Observable<LoginResponse> observable = userRepository.status(URL);
        TestSubscriber<LoginResponse> subscriber = new TestSubscriber<>();
        observable.subscribe(subscriber);

        subscriber.assertNoErrors();
        LoginResponse loginResponse = subscriber.getOnNextEvents().get(0);
        verify(dynamicEndpoint).setUrl(URL);
        assertThat(loginResponse.url).isEqualTo(URL);
        assertThat(loginResponse.statusResponse.stat).isEqualTo(MockRestService.STATUS_OK);
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

    private Observable<Response> getLoginSuccessResponse() {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Set-Cookie", "pwg_id=" + COOKIE_PWG_ID));
        TypedByteArray body = new TypedByteArray("application/json", "{\"stat\":\"ok\",\"result\":true}".getBytes());

        return Observable.just(new Response("http://test.piwigo.org/ws.php?format=json&method=pwg.session.login", HTTP_OK, "OK", headers, body));
    }

    private Observable<Response> getLoginFailureResponse() {
        List<Header> headers = new ArrayList<>();
        TypedByteArray body = new TypedByteArray("application/json", "{\"stat\":\"fail\",\"err\":999,\"message\":\"Invalid username\\/password\"}".getBytes());

        return Observable.just(new Response("http://test.piwigo.org/ws.php?format=json&method=pwg.session.login", HTTP_OK, "OK", headers, body));
    }
}