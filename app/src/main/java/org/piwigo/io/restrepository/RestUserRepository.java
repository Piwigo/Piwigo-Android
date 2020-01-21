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
import org.piwigo.io.RestService;
import org.piwigo.io.WebServiceFactory;
import org.piwigo.io.restmodel.StatusResponse;
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

    public Observable<SuccessResponse> login(Account account) {
        String url = userManager.getSiteUrl(account);
        String username = userManager.getUsername(account);
        String password = userManager.getPassword(account);
        return login(url, username, password);
    }

    public Observable<SuccessResponse> login(String url, String username, String password) {
        RestService restService = webServiceFactory.createForUrl(validateUrl(url));
        return restService.login(username, password);
    }

    /* intended only for Login view, otherwise consider status() */
    public Observable<StatusResponse> status(String siteUrl) {
        if(!siteUrl.endsWith("/")){
            siteUrl = siteUrl + "/";
        }
        RestService restService = webServiceFactory.createForUrl(siteUrl);

        return status(restService)
                .subscribeOn(ioScheduler)
                .observeOn(uiScheduler);
    }

    public Observable<StatusResponse> status() {
        RestService restService = webServiceFactory.create();
        return status(restService)
                .subscribeOn(ioScheduler)
                .observeOn(uiScheduler);
    }

    private Observable<StatusResponse> status(RestService restService) {
        return restService.getStatus()
                .subscribeOn(ioScheduler)
                .observeOn(uiScheduler);
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
