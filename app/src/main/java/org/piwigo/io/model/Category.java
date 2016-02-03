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
package org.piwigo.io.model;

import com.google.gson.annotations.SerializedName;

public class Category {

    @SerializedName("id") public int id;

    @SerializedName("name") public String name;

    @SerializedName("comment") public String comment;

    @SerializedName("global_rank") public String globalRank;

    @SerializedName("nb_images") public int nbImages;

    @SerializedName("total_nb_images") public int totalNbImages;

    @SerializedName("representative_picture_id") public int representativePictureId;

    @SerializedName("nb_categories") public int nbCategories;

    @Override public boolean equals(Object o) {
        if (o instanceof Category) {
            return id == ((Category) o).id;
        }
        return super.equals(o);
    }

}