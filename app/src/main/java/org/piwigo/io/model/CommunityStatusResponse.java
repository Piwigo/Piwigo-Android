package org.piwigo.io.model;

import com.google.gson.annotations.SerializedName;

public class CommunityStatusResponse {

    @SerializedName("stat") public String stat;

    @SerializedName("result") public Result result;

    public class Result {

        @SerializedName("real_user_status") public String status;

        @SerializedName("upload_categories_getList_method") public String uploadCategoriesGetListMethod;

    }

}
