package org.piwigo.io.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MethodListResponse
{

    @SerializedName("stat") public String stat;

    @SerializedName("result") public Result result;

    public class Result {

        @SerializedName("methods") public List<String> methods = new ArrayList<>();

    }
}
