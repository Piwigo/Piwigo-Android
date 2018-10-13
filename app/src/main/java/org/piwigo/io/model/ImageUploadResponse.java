package org.piwigo.io.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeff on 6/24/2017.
 */

public class ImageUploadResponse {

    @SerializedName("stat") public String up_stat;
    @SerializedName("result") public ImageUploadResponse.UpResult up_result;

    //only on upload fail
    @SerializedName("err") public int up_err;
    @SerializedName("message") public String up_message;

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
