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

import org.piwigo.helper.CookieHelper;
import org.piwigo.io.RestService;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.io.model.LoginResponse;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Scheduler;

public class UserRepository extends BaseRepository {

    @Inject UserRepository(RestServiceFactory restServiceFactory, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler) {
        super(restServiceFactory, ioScheduler, uiScheduler);
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
                        loginResponse.pwgId = CookieHelper.extract("pwg_id", response.headers());
                        return Observable.just(response.body());
                    }
                    return Observable.error(new Throwable("Login failed"));
                })
                .flatMap(successResponse -> restService.getStatus("pwg_id=" + loginResponse.pwgId))
                .map(statusResponse -> {
                    loginResponse.statusResponse = statusResponse;
                    return loginResponse;
                })
                .compose(applySchedulers());
    }

    public Observable<LoginResponse> status(String url) {
        String baseUrl = validateUrl(url);
        RestService restService = restServiceFactory.createForUrl(baseUrl);

        final LoginResponse loginResponse = new LoginResponse();
        loginResponse.url = baseUrl;

        return restService.getStatus()
                .map(statusResponse -> {
                    loginResponse.statusResponse = statusResponse;
                    return loginResponse;
                })
                .compose(applySchedulers());
    }
}
