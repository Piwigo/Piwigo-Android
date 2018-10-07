package org.piwigo.io.model;

/**
 * Created by Jeff on 7/18/2017.
 */


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class ImageListResponse {

    @SerializedName("stat") public String stat;

    @SerializedName("result") public Result result;

    public class Result {

        @SerializedName("images") public List<ImageInfo> images = new ArrayList<>();

    }

}
