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
