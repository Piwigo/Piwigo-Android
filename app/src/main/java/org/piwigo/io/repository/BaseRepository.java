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

import org.piwigo.io.RestServiceFactory;

import rx.Observable;
import rx.Scheduler;

abstract class BaseRepository {

    final RestServiceFactory restServiceFactory;
    private final Scheduler ioScheduler;
    private final Scheduler uiScheduler;

    BaseRepository(RestServiceFactory restServiceFactory, Scheduler ioScheduler, Scheduler uiScheduler) {
        this.restServiceFactory = restServiceFactory;
        this.ioScheduler = ioScheduler;
        this.uiScheduler = uiScheduler;
    }

    String validateUrl(String url) {
        if (!url.endsWith("/")) {
            return url + "/";
        }
        return url;
    }

    <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(ioScheduler)
                .observeOn(uiScheduler);
    }
}
