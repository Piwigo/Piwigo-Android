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

package org.piwigo.io.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class StatusResponse {

    @SerializedName("stat") public String stat;

    @SerializedName("result") public Status result;

    public static class Status {

        @SerializedName("username") public String username;

        @SerializedName("status") public String status;

        @SerializedName("theme") public String theme;

        @SerializedName("language") public String language;

        @SerializedName("pwg_token") public String pwgToken;

        @SerializedName("charset") public String charset;

        @SerializedName("current_datetime") public Date currentDatetime;

        @SerializedName("version") public String version;

        @SerializedName("upload_file_types") public String uploadFileTypes;

    }

}
