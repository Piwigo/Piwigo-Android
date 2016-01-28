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

package org.piwigo.io.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CategoryListResponse {

    @SerializedName("stat") public String stat;

    @SerializedName("result") public Result result;

    public class Result {

        @SerializedName("categories") public List<Category> categories = new ArrayList<>();

        public class Category {

            @SerializedName("id") public Integer id;

            @SerializedName("name") public String name;

            @SerializedName("comment") public String comment;

            @SerializedName("permalink") public String permalink;

            @SerializedName("uppercats") public String uppercats;

            @SerializedName("global_rank") public String globalRank;

            @SerializedName("id_uppercat") public Object idUppercat;

            @SerializedName("nb_images") public Integer nbImages;

            @SerializedName("total_nb_images") public Integer totalNbImages;

            @SerializedName("representative_picture_id") public String representativePictureId;

            @SerializedName("date_last") public String dateLast;

            @SerializedName("max_date_last") public String maxDateLast;

            @SerializedName("nb_categories") public Integer nbCategories;

            @SerializedName("url") public String url;

            @SerializedName("tn_url") public String tnUrl;

        }

    }

}
