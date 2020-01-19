/*
 * Piwigo for Android
 * Copyright (C) 2016-2019 Piwigo Team http://piwigo.org
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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ImageInfo implements Serializable {

    @SerializedName("id") public int id;

    @SerializedName("name") public String name;

    @SerializedName("file") public String file;

    @SerializedName("description") public String comment;

    @SerializedName("author") public String author;

    @SerializedName("width") public int width;

    @SerializedName("height") public int height;

    @SerializedName("date_creation") public Date dateCreation;

    @SerializedName("date_available") public Date dateAvailable;

    @SerializedName("element_url") public String elementUrl;

    @SerializedName("derivatives") public Derivatives derivatives;

    @SerializedName("categories") public List<CategoryID> categories;


    @Override public boolean equals(Object o) {
        if (o instanceof ImageInfo) {
            return id == ((ImageInfo) o).id;
        }
        return super.equals(o);
    }

    public class CategoryID {
        @SerializedName("id") public int id;

    }
}
