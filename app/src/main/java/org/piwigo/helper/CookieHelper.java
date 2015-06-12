package org.piwigo.helper;

import android.support.v4.util.ArrayMap;

import java.net.HttpCookie;
import java.util.List;

import retrofit.client.Header;

public class CookieHelper {

    private static final String COOKIE_HEADER = "Set-Cookie";

    /**
     * Extracted a named cookie from a list of headers
     *
     * @param name Name of the cookie to extract
     * @param headers List of headers
     * @return Value of the cookie if present
     */
    public static String extract(String name, List<Header> headers) {
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
    public static ArrayMap<String, String> extractAll(List<Header> headers) {
        ArrayMap<String, String> cookies = new ArrayMap<>();
        for (Header header : headers) {
            if (header.getName().equalsIgnoreCase(COOKIE_HEADER)) {
                List<HttpCookie> httpCookies = HttpCookie.parse(header.getValue());
                for (HttpCookie httpCookie : httpCookies) {
                    cookies.put(httpCookie.getName(), httpCookie.getValue());
                }
            }
        }
        return cookies;
    }

}
