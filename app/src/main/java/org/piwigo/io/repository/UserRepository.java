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
import org.piwigo.helper.URLHelper;
import org.piwigo.io.RestService;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.io.model.LoginResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        Observable<LoginResponse> result = null;
        try {
            result = login(url, username, password);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Observable<LoginResponse> login(String url, String username, String password) throws ExecutionException, InterruptedException {
        String newUrl = url;
        newUrl = new URLHelper().execute(url).get();
        RestService restService = restServiceFactory.createForUrl(validateUrl(newUrl));

        final LoginResponse loginResponse = new LoginResponse();
        loginResponse.url = newUrl;
        loginResponse.username = username;
        loginResponse.password = password;

        return restService.login(username, password)
                .flatMap(response -> {
                    if (response.body().result) {
// TODO: check: should we set the cookie in the Usermanager here?
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

    /* intended only for Login view, otherwise consider status(Account account) */
    public Observable<LoginResponse> status(String siteUrl) throws ExecutionException, InterruptedException {
        String newUrl = new URLHelper().execute(siteUrl).get();
        if(!siteUrl.endsWith("/")){
            newUrl = newUrl + "/";
        }
        RestService restService = restServiceFactory.createForUrl(newUrl);
        return status(restService, newUrl);
    }

    public Observable<LoginResponse> status(Account account) {
        String siteUrl = validateUrl(userManager.getSiteUrl(account));
        RestService restService = restServiceFactory.createForAccount(account);
        return status(restService, siteUrl);
    }

    private Observable<LoginResponse> status(RestService restService, String url) {
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
