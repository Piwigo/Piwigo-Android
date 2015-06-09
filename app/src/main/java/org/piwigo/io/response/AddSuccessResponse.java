package org.piwigo.io.response;

import com.google.gson.annotations.SerializedName;

public class AddSuccessResponse {

    @SerializedName("stat")
    public String stat;

    @SerializedName("result")
    public Result result;

    public class Result {

        @SerializedName("id")
        public int id;

        @SerializedName("info")
        public String info;

    }

}
