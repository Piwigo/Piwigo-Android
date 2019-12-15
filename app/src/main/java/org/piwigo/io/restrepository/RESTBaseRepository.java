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

import org.piwigo.accounts.UserManager;
import org.piwigo.io.WebServiceFactory;

import io.reactivex.Scheduler;

abstract class RESTBaseRepository {

    final WebServiceFactory webServiceFactory;
    final Scheduler ioScheduler;
    final Scheduler uiScheduler;
    final UserManager userManager;

    RESTBaseRepository(WebServiceFactory webServiceFactory, Scheduler ioScheduler, Scheduler uiScheduler, UserManager userManager) {
        this.webServiceFactory = webServiceFactory;
        this.ioScheduler = ioScheduler;
        this.uiScheduler = uiScheduler;
        this.userManager = userManager;
    }

    String validateUrl(String url) {
        String result = url;
        if (!result.endsWith("/")) {
            result = result + "/";
        }
        if (!result.startsWith("http://") && !result.startsWith("https://")) {
            result = "https://" + result;
        }
        return result;
    }
/*
    <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(ioScheduler)
                .observeOn(uiScheduler);
    }
    */
}
