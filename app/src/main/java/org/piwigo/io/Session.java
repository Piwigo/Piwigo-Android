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

package org.piwigo.io;

public class Session {

    private String cookie;

    private String token;

    /**
     * Set the session cookie
     *
     * @param cookie New session cookie
     */
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    /**
     * Get the session cookie
     *
     * @return Current session cookie
     */
    public String getCookie() {
        return cookie;
    }

    /**
     * Set the user token
     *
     * @param token New user token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Get the user token
     *
     * @return Current user token
     */
    public String getToken() {
        return token;
    }

    /**
     * Purge the current session data
     */
    public void purge() {
        cookie = null;
        token = null;
    }

}
