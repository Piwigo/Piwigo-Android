/*
 * Piwigo for Android
 * Copyright (C) 2017-2018 Piwigo Team http://piwigo.org
 * Copyright (C) 2017-2018 Jeff Ayers
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

import java.util.ArrayList;
import java.util.List;


public class ImageListResponse {

    @SerializedName("stat") public String stat;

    @SerializedName("err") public int err;
    @SerializedName("message") public String message;

    @SerializedName("result") public Result result;

    public class Result {
        @SerializedName("images") public List<ImageInfo> images = new ArrayList<>();
        @SerializedName("paging") public Paging paging;
    }

    public class Paging {
        @SerializedName("page") public int page;
        @SerializedName("per_page") public int perPage;
        @SerializedName("count") public int count;
        @SerializedName("total_count") public int totalCount;

    }
}
