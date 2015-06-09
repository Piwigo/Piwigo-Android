package org.piwigo.io;

import org.piwigo.io.response.AddSuccessResponse;
import org.piwigo.io.response.StatusResponse;
import org.piwigo.io.response.SuccessResponse;

import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import rx.Observable;

public interface RestService {

    @POST("/ws.php?method=pwg.categories.add")
    @FormUrlEncoded
    Observable<AddSuccessResponse> addCategory(String name, Integer parent, String comment, Boolean visible, String status, Boolean commentable);

    @GET("/ws.php?method=pwg.session.getStatus")
    Observable<StatusResponse> getStatus();

    @POST("/ws.php?method=pwg.session.login")
    @FormUrlEncoded
    Observable<SuccessResponse> login(String username, String password);

    @GET("/ws.php?method=pwg.session.logout")
    Observable<SuccessResponse> logout();

}
