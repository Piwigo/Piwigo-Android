/*
 * Copyright 2015 Phil Bayfield https://philio.me
 * Copyright 2015 Piwigo Team http://piwigo.org
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

package org.piwigo.io.observable;

import com.google.gson.Gson;

import org.piwigo.helper.CookieHelper;
import org.piwigo.io.RestService;
import org.piwigo.io.response.StatusResponse;
import org.piwigo.io.response.SuccessResponse;
import org.piwigo.manager.SessionManager;

import retrofit.mime.TypedByteArray;
import rx.Observable;
import rx.Scheduler;

public class LoginObservable extends BaseObservable<StatusResponse> {

    private Gson gson;

    private String username;

    private String password;

    public LoginObservable(SessionManager sessionManager, RestService restService, Scheduler ioScheduler, Scheduler uiScheduler, Gson gson) {
        super(sessionManager, restService, ioScheduler, uiScheduler);
        this.gson = gson;
    }

    /**
     * Set username
     *
     * @param username
     * @return
     */
    public LoginObservable username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Set password
     *
     * @param password
     * @return
     */
    public LoginObservable password(String password) {
        this.password = password;
        return this;
    }

    /**
     * Create {@link Observable} for logging in a user. This performs two requests, firstly login to
     * obtain the pwg_id cookie and then getStatus to obtain the token. These values are populated
     * in the {@link SessionManager} automatically.
     *
     * @return Login observable
     */
    @Override public Observable<StatusResponse> create() {
        if (observable == null) {
            observable = restService
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
        return observable;
    }

}
