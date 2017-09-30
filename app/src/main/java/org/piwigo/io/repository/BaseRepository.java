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

import android.text.TextUtils;

import org.piwigo.io.DynamicRetrofit;
import org.piwigo.io.RestService;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.io.Session;

import rx.Observable;
import rx.Scheduler;

import static android.view.View.Z;

abstract class BaseRepository {

    final Session session;
    final RestServiceFactory restServiceFactory;
    private final Scheduler ioScheduler;
    private final Scheduler uiScheduler;

    BaseRepository(Session session, RestServiceFactory restServiceFactory, Scheduler ioScheduler, Scheduler uiScheduler) {
        this.session = session;
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
