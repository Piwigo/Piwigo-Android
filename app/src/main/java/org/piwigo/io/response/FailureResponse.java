package org.piwigo.io.response;

import com.google.gson.annotations.SerializedName;

public class FailureResponse {

    @SerializedName("stat")
    public String stat;

    @SerializedName("err")
    public int err;

    @SerializedName("message")
    public String message;

}
