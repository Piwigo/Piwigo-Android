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
import org.piwigo.io.model.LoginResponse;
import org.piwigo.io.model.SuccessResponse;

import javax.inject.Inject;

import retrofit.mime.TypedByteArray;
import rx.Observable;

public class UserRepository extends BaseRepository {

    @Inject Gson gson;

    @Inject public UserRepository() {}

    public Observable<LoginResponse> login(String url, String username, String password) {
        endpoint.setUrl(url);

        final LoginResponse loginResponse = new LoginResponse();
        loginResponse.url = url;
        loginResponse.username = username;
        loginResponse.password = password;

        return restService
                .login(username, password)
                .flatMap(response -> {
                    String body = new String(((TypedByteArray) response.getBody()).getBytes());
                    SuccessResponse successResponse = gson.fromJson(body, SuccessResponse.class);
                    if (successResponse.result) {
                        String sessionId = CookieHelper.extract("pwg_id", response.getHeaders());
                        loginResponse.pwgId = sessionId;
                        session.setCookie(sessionId);
                        return Observable.just(successResponse);
                    }
                    return Observable.error(new Throwable("Login failed"));
                })
                .flatMap(successResponse -> restService.getStatus())
                .map(statusResponse -> {
                    loginResponse.statusResponse = statusResponse;
                    return loginResponse;
                })
                .compose(applySchedulers());
    }

    public Observable<LoginResponse> status(String url) {
        endpoint.setUrl(url);

        final LoginResponse loginResponse = new LoginResponse();
        loginResponse.url = url;

        return restService
                .getStatus()
                .map(statusResponse -> {
                    loginResponse.statusResponse = statusResponse;
                    return loginResponse;
                })
                .compose(applySchedulers());
    }

}
