/*
 * Copyright 2017 Phil Bayfield https://philio.me
 * Copyright 2017 Piwigo Team http://piwigo.org
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

import android.accounts.Account;

import org.piwigo.helper.AccountHelper;

import retrofit2.Retrofit;

public class RestServiceFactory {

    private final Retrofit.Builder builder;
    private final AccountHelper accountHelper;

    public RestServiceFactory(Retrofit.Builder builder, AccountHelper accountHelper) {
        this.builder = builder;
        this.accountHelper = accountHelper;
    }

    public RestService createForUrl(String url) {
        return builder.baseUrl(url).build().create(RestService.class);
    }

    public RestService createForAccount(Account account) {
        String url = accountHelper.getAccountUrl(account);
        return createForUrl(url);
    }
}
