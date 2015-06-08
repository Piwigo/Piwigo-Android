package org.piwigo.io;

import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;

public interface RestService {

    @POST("/ws.php?method=pwg.session.login")
    @FormUrlEncoded
    public void login(String username, String password);

    @GET("/ws.php?method=pwg.session.logout")
    public void logout();

}
