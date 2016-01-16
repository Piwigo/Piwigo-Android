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

package org.piwigo.helper;

import android.support.v4.util.ArrayMap;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import retrofit.client.Header;

import static org.assertj.core.api.Assertions.assertThat;

public class CookieHelperTest {

    List<Header> headers = new ArrayList<>();

    @Before public void setUp() {
        headers.add(new Header("Not-Cookie", "aValue"));
        headers.add(new Header("Set-Cookie", "pwg_id=asdfghjklqwertyuiop"));
        headers.add(new Header("Also-Not-Cookie", "anotherValue"));
        headers.add(new Header("Set-Cookie", "something=else"));
    }

    @Test public void shouldExtractAllCookies() {
        ArrayMap<String, String> cookies = CookieHelper.extractAll(headers);

        assertThat(cookies).hasSize(2)
                .containsEntry("pwg_id", "asdfghjklqwertyuiop")
                .containsEntry("something", "else")
                .doesNotContainEntry("Not-Cookie", "aValue")
                .doesNotContainEntry("Also-Not-Cookie", "anotherValue");
    }

    @Test public void shouldExtractCookie() {
        String value = CookieHelper.extract("pwg_id", headers);

        assertThat(value).isEqualTo("asdfghjklqwertyuiop");
    }

}