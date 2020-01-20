/*
 * Piwigo for Android
 * Copyright (C) 2016-2019 Piwigo Team http://piwigo.org
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

import android.accounts.Account;

import org.piwigo.accounts.UserManager;
import org.piwigo.helper.CookieHelper;
import org.piwigo.io.PiwigoLoginException;
import org.piwigo.io.RestService;
import org.piwigo.io.WebServiceFactory;
import org.piwigo.io.restmodel.LoginResponse;
import org.piwigo.io.restmodel.SuccessResponse;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

public class RestUserRepository extends RESTBaseRepository {

    @Inject
    RestUserRepository(WebServiceFactory webServiceFactory, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler, UserManager userManager) {
        super(webServiceFactory, ioScheduler, uiScheduler, userManager);
    }

    public Observable<LoginResponse> login(Account account) {
        String url = userManager.getSiteUrl(account);
        String username = userManager.getUsername(account);
        String password = userManager.getPassword(account);
        Observable<LoginResponse> result = login(url, username, password);
        return result;
    }

    public Observable<LoginResponse> login(String url, String username, String password) {
        RestService restService = webServiceFactory.createForUrl(validateUrl(url));

        final LoginResponse loginResponse = new LoginResponse();

        return restService.login(username, password)
                .subscribeOn(ioScheduler)
                .observeOn(uiScheduler)
                .flatMap(response -> {
                    if (response.body() != null && response.body().result) {
                        // TODO: check: should we set the cookie in the UserManager here?
                        loginResponse.pwgId = CookieHelper.extract("pwg_id", response.headers());
                        return Observable.just(response.body())
                                .subscribeOn(ioScheduler)
                                .observeOn(uiScheduler)
                                ;
                    }
                    if (response.body() == null) {
                        return Observable.error(new PiwigoLoginException("Login for user '" + username + "' failed with null response body"));
                    }
                    return Observable.error(new PiwigoLoginException("Login for user '" + username + "' failed with code " + response.body().err + ": " + response.body().message));
                })
                .flatMap(successResponse -> restService.getStatus("pwg_id=" + loginResponse.pwgId)
                                .subscribeOn(ioScheduler)
                                .observeOn(uiScheduler)
                )
                .map(statusResponse -> {
                    loginResponse.statusResponse = statusResponse;
                    return loginResponse;
                })
                ;
    }

    /* intended only for Login view, otherwise consider status() */
    public Observable<LoginResponse> status(String siteUrl) {
        if(!siteUrl.endsWith("/")){
            siteUrl = siteUrl + "/";
        }
        RestService restService = webServiceFactory.createForUrl(siteUrl);

        return status(restService)
                .subscribeOn(ioScheduler)
                .observeOn(uiScheduler);

    }

    public Observable<LoginResponse> status() {
        RestService restService = webServiceFactory.create();
        return status(restService)
                .subscribeOn(ioScheduler)
                .observeOn(uiScheduler)
                ;
    }

    private Observable<LoginResponse> status(RestService restService) {
        final LoginResponse loginResponse = new LoginResponse();

        return restService.getStatus()
                .subscribeOn(ioScheduler)
                .observeOn(uiScheduler)
                .map(statusResponse -> {
                    loginResponse.statusResponse = statusResponse;
                    return loginResponse;
                })
                ;
    }

    public Observable<SuccessResponse> logout(Account account) {
        RestService restService = webServiceFactory.createForUrl(validateUrl(userManager.getSiteUrl(account)));
        final SuccessResponse successResponse = new SuccessResponse();

        return restService.logout()
                .subscribeOn(ioScheduler)
                .observeOn(uiScheduler)
                .map(statusResponse -> successResponse);
    }
}
