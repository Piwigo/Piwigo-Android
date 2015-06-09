package org.piwigo.io.response;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class StatusResponse {

    @SerializedName("stat")
    public String stat;

    @SerializedName("result")
    public Status result;

    public class Status {

        @SerializedName("username")
        public String username;

        @SerializedName("status")
        public String status;

        @SerializedName("theme")
        public String theme;

        @SerializedName("language")
        public String language;

        @SerializedName("pwg_token")
        public String pwgToken;

        @SerializedName("charset")
        public String charset;

        @SerializedName("current_datetime")
        public Date currentDatetime;

        @SerializedName("version")
        public String version;

        @SerializedName("upload_file_types")
        public String uploadFileTypes;

    }

}
