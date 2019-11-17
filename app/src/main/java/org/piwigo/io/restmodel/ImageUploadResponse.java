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


public class ImageUploadResponse {

    @SerializedName("stat") public String up_stat;
    @SerializedName("result") public ImageUploadResponse.UpResult up_result;

    //only on upload fail
    @SerializedName("err") public int up_err; // not sure whether this is set at all
    @SerializedName("message") public String up_message;
    @SerializedName("error") public Error err;

    public class UpResult {
        @SerializedName("image_id") public int up_image_id;
        @SerializedName("src") public String up_src;
        @SerializedName("name") public String up_name;
        @SerializedName("category") public UpCategory up_category;
    }

    public class UpCategory {
        @SerializedName("id")
        public int catid;
        @SerializedName("nb_photos")
        public String catnb_photos;
        @SerializedName("label")
        public String catlabel;
    }
}
