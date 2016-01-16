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

import com.google.gson.Gson;

import org.piwigo.helper.CookieHelper;
import org.piwigo.io.RestService;
import org.piwigo.io.SessionManager;
import org.piwigo.io.model.response.StatusResponse;
import org.piwigo.io.model.response.SuccessResponse;

import retrofit.mime.TypedByteArray;
import rx.Observable;
import rx.Scheduler;

public class UserRepository extends BaseRepository {

    private Gson gson;

    public UserRepository(SessionManager sessionManager, RestService restService, Scheduler ioScheduler, Scheduler uiScheduler, Gson gson) {
        super(sessionManager, restService, ioScheduler, uiScheduler);
        this.gson = gson;
    }

    public Observable<StatusResponse> login(String url, String username, String password) {
        sessionManager.setUrl(url);
        return restService
                .login(username, password)
                .flatMap(response -> {
                    String body = new String(((TypedByteArray) response.getBody()).getBytes());
                    SuccessResponse successResponse = gson.fromJson(body, SuccessResponse.class);
                    if (successResponse.result) {
                        String cookie = CookieHelper.extract("pwg_id", response.getHeaders());
                        sessionManager.setCookie(cookie);
                        return Observable.just(successResponse);
                    }
                    return Observable.error(new Throwable("Login failed"));
                })
                .flatMap(successResponse -> restService.getStatus())
                .map(statusResponse -> {
                    sessionManager.setToken(statusResponse.result.pwgToken);
                    return statusResponse;
                })
                .compose(applySchedulers());
    }

}
