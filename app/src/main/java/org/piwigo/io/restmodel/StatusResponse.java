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

package org.piwigo.io.restmodel;

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
// TODO: add "available_sizes":["square","thumb","small","medium","large","xxlarge"]

        @SerializedName("upload_form_chunk_size") public Integer uploadFormChunkSize; // upload_form_chunk_size is returned in kB by piwigo server
    }

}
