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

package org.piwigo.io.restrepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.piwigo.accounts.UserManager;
import org.piwigo.io.RestService;
import org.piwigo.io.WebServiceFactory;
import org.piwigo.io.restmodel.StatusResponse;
import org.piwigo.io.restmodel.SuccessResponse;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import okhttp3.Headers;
import retrofit2.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RestUserRepositoryTest {

    private static final String URL = "https://piwigo.org/demo/";
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";
    private static final String BAD_CREDENTIAL = "bad";
    private static final String GUEST_USER = "guest";
    private static final String COOKIE_PWG_ID = "1234567890";
    private static final String TOKEN = "abcdefghijklmnop";
    private static final String STATUS_OK = "ok";
    private static final String STATUS_FAIL = "fail";

    @Mock
    WebServiceFactory webServiceFactory;
    @Mock RestService restService;
    @Mock UserManager userManager;

    private RestUserRepository userRepository;

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(webServiceFactory.createForUrl(anyString())).thenReturn(restService);
        when(restService.getStatus()).thenReturn(getStatusResponse(GUEST_USER));
        when(restService.login(USERNAME, PASSWORD)).thenReturn(getLoginSuccessResponse());
        when(restService.login(BAD_CREDENTIAL, BAD_CREDENTIAL)).thenReturn(getLoginFailureResponse());

        userRepository = new RestUserRepository(webServiceFactory, Schedulers.trampoline(), Schedulers.trampoline(), userManager);
    }

    @Test public void status_returnsStatusResponse() {
        Observable<StatusResponse> observable = userRepository.status(URL);
        TestObserver<StatusResponse> subscriber = new TestObserver<>();
        observable.subscribe(subscriber);

        subscriber.assertNoErrors();
        StatusResponse statusResponse = subscriber.values().get(0);
        verify(webServiceFactory).createForUrl(URL);
        assertThat(statusResponse.stat).isEqualTo(STATUS_OK);
        assertThat(statusResponse.result.username).isEqualTo(GUEST_USER);
    }

    private Observable<StatusResponse> getStatusResponse(String user) {
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.stat = STATUS_OK;
        statusResponse.result = new StatusResponse.Status();
        statusResponse.result.username = user;
        statusResponse.result.pwgToken = TOKEN;
        return Observable.just(statusResponse);
    }

    private Observable<SuccessResponse> getLoginSuccessResponse() {
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.stat = STATUS_OK;
        successResponse.result = true;
        return Observable.just(successResponse);
    }

    private Observable<SuccessResponse> getLoginFailureResponse() {
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.stat = STATUS_FAIL;
        return Observable.just(successResponse);
    }
}