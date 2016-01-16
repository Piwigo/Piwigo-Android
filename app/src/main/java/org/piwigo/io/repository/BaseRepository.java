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

import org.piwigo.io.RestService;
import org.piwigo.io.SessionManager;

import rx.Observable;
import rx.Scheduler;

public abstract class BaseRepository {

    protected final SessionManager sessionManager;
    protected final RestService restService;
    protected final Scheduler ioScheduler;
    protected final Scheduler uiScheduler;

    protected BaseRepository(SessionManager sessionManager, RestService restService, Scheduler ioScheduler, Scheduler uiScheduler) {
        this.sessionManager = sessionManager;
        this.restService = restService;
        this.ioScheduler = ioScheduler;
        this.uiScheduler = uiScheduler;
    }

    protected <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(ioScheduler)
                .observeOn(uiScheduler);
    }

}
