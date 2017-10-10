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

package org.piwigo.helper;

import android.support.v4.util.ArrayMap;

import org.junit.Before;
import org.junit.Test;

import okhttp3.Headers;

import static org.assertj.core.api.Assertions.assertThat;

public class CookieHelperTest {

    private Headers headers;

    @Before public void setUp() {
        headers = Headers.of("Not-Cookie", "aValue", "Set-Cookie", "pwg_id=asdfghjklqwertyuiop", "Also-Not-Cookie", "anotherValue", "Set-Cookie", "something=else");
    }

    @Test public void extractAllCookies() {
        ArrayMap<String, String> cookies = CookieHelper.extractAll(headers);

        assertThat(cookies).hasSize(2)
                .containsEntry("pwg_id", "asdfghjklqwertyuiop")
                .containsEntry("something", "else")
                .doesNotContainEntry("Not-Cookie", "aValue")
                .doesNotContainEntry("Also-Not-Cookie", "anotherValue");
    }

    @Test public void extractPwgIdCookie() {
        String value = CookieHelper.extract("pwg_id", headers);

        assertThat(value).isEqualTo("asdfghjklqwertyuiop");
    }
}