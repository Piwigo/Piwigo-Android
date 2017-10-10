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

import java.net.HttpCookie;
import java.util.List;

import okhttp3.Headers;

public class CookieHelper {

    private static final String COOKIE_HEADER = "Set-Cookie";

    /**
     * Extracted a named cookie from a list of headers
     *
     * @param name Name of the cookie to extract
     * @param headers List of headers
     * @return Value of the cookie if present
     */
    public static String extract(String name, Headers headers) {
        ArrayMap<String, String> cookies = extractAll(headers);
        if (cookies.containsKey(name)) {
            return cookies.get(name);
        }
        return null;
    }

    /**
     * Extract all cookies from a list of headers
     *
     * @param headers List of headers
     * @return Map of cookies
     */
    public static ArrayMap<String, String> extractAll(Headers headers) {
        ArrayMap<String, String> cookies = new ArrayMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String name = headers.name(i);
            if (name.equalsIgnoreCase(COOKIE_HEADER)) {
                List<HttpCookie> httpCookies = HttpCookie.parse(headers.value(i));
                for (HttpCookie httpCookie : httpCookies) {
                    cookies.put(httpCookie.getName(), httpCookie.getValue());
                }
            }
        }
        return cookies;
    }

}
