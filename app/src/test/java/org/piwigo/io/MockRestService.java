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

package org.piwigo.io;

import org.piwigo.io.model.response.AddCategoryResponse;
import org.piwigo.io.model.response.CategoryListResponse;
import org.piwigo.io.model.response.StatusResponse;
import org.piwigo.io.model.response.SuccessResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.mime.TypedByteArray;
import rx.Observable;

import static java.net.HttpURLConnection.HTTP_OK;

public class MockRestService implements RestService {

    public static final String STATUS_OK = "ok";
    public static final String TOKEN = "abcdefghijklmnop";
    public static final String COOKIE_PWG_ID = "1234567890";
    public static final String GUEST_USER = "guest";

    private String loggedInUser = GUEST_USER;

    @Override public Observable<StatusResponse> getStatus() {
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.stat = STATUS_OK;
        statusResponse.result = new StatusResponse.Status();
        statusResponse.result.username = loggedInUser;
        statusResponse.result.pwgToken = TOKEN;
        return Observable.from(new StatusResponse[]{statusResponse});
    }

    @Override public Observable<Response> login(@Field("username") String username, @Field("password") String password) {
        final int code;
        final String data;
        if (username.equals("test") && password.equals("test")) {
            code = HTTP_OK;
            loggedInUser = "test";
            data = "{\"stat\":\"ok\",\"result\":true}";
        } else {
            code = HTTP_OK;
            data = "{\"stat\":\"fail\",\"err\":999,\"message\":\"Invalid username\\/password\"}";
        }

        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Set-Cookie", "pwg_id=" + COOKIE_PWG_ID));

        TypedByteArray body = new TypedByteArray("application/json", data.getBytes());

        return Observable.from(new Response[]{new Response("http://test.piwigo.org/ws.php?format=json&method=pwg.session.login", code, "OK", headers, body)});
    }

    @Override public Observable<SuccessResponse> logout() {
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.stat = "ok";
        successResponse.result = true;
        return Observable.from(new SuccessResponse[]{successResponse});
    }

    @Override public Observable<AddCategoryResponse> addCategory(@Field("name") String name, @Field("parent") Integer parent, @Field("comment") String comment, @Field("visible") Boolean visible, @Field("status") String status, @Field("commentable") Boolean commentable) {
        return null;
    }

    @Override public Observable<CategoryListResponse> getCategories() {
        return null;
    }

}
