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
import org.piwigo.helper.CookieHelper;
import org.piwigo.io.PiwigoLoginException;
import org.piwigo.io.RestService;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.io.model.LoginResponse;
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
        Observable<LoginResponse> result = login(url, username, password);
        return result;
    }

    public Observable<LoginResponse> login(String url, String username, String password) {
        RestService restService = restServiceFactory.createForUrl(validateUrl(url));

        final LoginResponse loginResponse = new LoginResponse();
        loginResponse.url = url;
        loginResponse.username = username;
        loginResponse.password = password;

        return restService.login(username, password)
                .compose(applySchedulers())
                .flatMap(response -> {
                    if (response.body().result) {
// TODO: check: should we set the cookie in the Usermanager here?
                        loginResponse.pwgId = CookieHelper.extract("pwg_id", response.headers());
                        return Observable.just(response.body()).compose(applySchedulers());
                    }
                    // TODO:
//            return Observable.error(new Throwable("Login failed"));
                    return Observable.error(new PiwigoLoginException("Login for user '" + username + "' failed with code " + response.body().err + ": " + response.body().message));
                })
                .flatMap(successResponse -> restService.getStatus("pwg_id=" + loginResponse.pwgId).compose(applySchedulers()))
                .map(statusResponse -> {
                    loginResponse.statusResponse = statusResponse;
                    return loginResponse;
                })
                ;
    }

    /* intended only for Login view, otherwise consider status(Account account) */
    public Observable<LoginResponse> status(String siteUrl) {
        if(!siteUrl.endsWith("/")){
            siteUrl = siteUrl + "/";
        }
        RestService restService = restServiceFactory.createForUrl(siteUrl);

        return status(restService, siteUrl).compose(applySchedulers());
    }

    public Observable<LoginResponse> status(Account account) {
        String siteUrl = validateUrl(userManager.getSiteUrl(account));
        RestService restService = restServiceFactory.createForAccount(account);
        return status(restService, siteUrl).compose(applySchedulers());
    }

    private Observable<LoginResponse> status(RestService restService, String url) {
        final LoginResponse loginResponse = new LoginResponse();
        loginResponse.url = url;

        return restService.getStatus()
                .compose(applySchedulers())
                .map(statusResponse -> {
                    loginResponse.statusResponse = statusResponse;
                    return loginResponse;
                })
                ;
    }

    public Observable<SuccessResponse> logout(Account account) {
        RestService restService = restServiceFactory.createForUrl(validateUrl(userManager.getSiteUrl(account)));
        final SuccessResponse successResponse = new SuccessResponse();

        return restService.logout()
                .compose(applySchedulers())
                .map(statusResponse -> successResponse);
    }
}
