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

import org.piwigo.helper.CookieHelper;
import org.piwigo.io.RestService;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.io.Session;
import org.piwigo.io.model.LoginResponse;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Scheduler;

public class UserRepository extends BaseRepository {

    @Inject public UserRepository(Session session, RestServiceFactory restServiceFactory, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler) {
        super(session, restServiceFactory, ioScheduler, uiScheduler);
    }

    public Observable<LoginResponse> login(String url, String username, String password) {
        RestService restService = restServiceFactory.createForUrl(validateUrl(url));

        final LoginResponse loginResponse = new LoginResponse();
        loginResponse.url = url;
        loginResponse.username = username;
        loginResponse.password = password;

        return restService.login(username, password)
                .flatMap(response -> {
                    if (response.body().result) {
                        String sessionId = CookieHelper.extract("pwg_id", response.headers());
                        loginResponse.pwgId = sessionId;
                        session.setCookie(sessionId);
                        return Observable.just(response.body());
                    }
                    return Observable.error(new Throwable("Login failed"));
                })
                .flatMap(successResponse -> restService.getStatus())
                .map(statusResponse -> {
                    loginResponse.statusResponse = statusResponse;
                    return loginResponse;
                })
                .compose(applySchedulers());
    }

    public Observable<LoginResponse> status(String url) {
        RestService restService = restServiceFactory.createForUrl(validateUrl(url));

        final LoginResponse loginResponse = new LoginResponse();
        loginResponse.url = url;

        return restService.getStatus()
                .map(statusResponse -> {
                    loginResponse.statusResponse = statusResponse;
                    return loginResponse;
                })
                .compose(applySchedulers());
    }
}
