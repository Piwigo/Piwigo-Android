package org.piwigo.io.response;

import com.google.gson.annotations.SerializedName;

public class SuccessResponse {

    @SerializedName("stat")
    public String stat;

    @SerializedName("result")
    public boolean result;

}
