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

import android.accounts.Account;
import android.util.Log;

import org.piwigo.accounts.UserManager;
import org.piwigo.io.RestService;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.io.model.StatusResponse;
import org.piwigo.io.model.SuccessResponse;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Scheduler;

public class UserRepository extends BaseRepository {

    @Inject UserRepository(RestServiceFactory restServiceFactory, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler, UserManager userManager) {
        super(restServiceFactory, ioScheduler, uiScheduler, userManager);
    }

    public Observable<LoginResponse> login(Account account) {
        String url = userManager.getSiteUrl(account);
        String username = userManager.getUsername(account);
        String password = userManager.getPassword(account);
        return login(url, username, password);
    }

    public Observable<LoginResponse> login(String url, String username, String password) {
        RestService restService = restServiceFactory.createForUrl(url);

        final LoginResponse loginResponse = new LoginResponse();
        loginResponse.url = url;
        loginResponse.username = username;
        loginResponse.password = password;

        Log.d("UserRepository", "login " + username + ":" + password);
        return restService.login(username, password)
                .compose(applySchedulers()).map(response -> loginResponse);
    }

    /* intended only for Login view, otherwise consider status(Account account) */
    public Observable<StatusResponse> status(String siteUrl) {
        RestService restService = restServiceFactory.createForUrl(siteUrl);
        return status(restService).compose(applySchedulers());
    }

    public Observable<StatusResponse> status() {
        RestService restService = restServiceFactory.create();
        return status(restService).compose(applySchedulers());
    }

    private Observable<StatusResponse> status(RestService restService) {
        return restService.getStatus()
                .compose(applySchedulers());
    }

    public Observable<SuccessResponse> logout(Account account) {
        RestService restService = restServiceFactory.createForUrl(userManager.getSiteUrl(account));
        final SuccessResponse successResponse = new SuccessResponse();

        return restService.logout()
                .compose(applySchedulers())
                .map(statusResponse -> successResponse);
    }
}
