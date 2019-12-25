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

public class Category {

    @SerializedName("id") public int id;

    @SerializedName("name") public String name;

    @SerializedName("description") public String comment;

    @SerializedName("global_rank") public String globalRank;

    @SerializedName("nb_images") public int nbImages;

    @SerializedName("total_nb_images") public int totalNbImages;

    @SerializedName("representative_picture_id") public int representativePictureId;

    @SerializedName("nb_categories") public int nbCategories;

    @SerializedName("id_uppercat") public int idUppercat;

    @SerializedName("tn_url") public String thumbnailUrl;

    @Override public boolean equals(Object o) {
        if (o instanceof Category) {
            return id == ((Category) o).id;
        }
        return super.equals(o);
    }

}