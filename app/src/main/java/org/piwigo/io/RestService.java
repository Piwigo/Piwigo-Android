package org.piwigo.io;

import org.piwigo.io.response.AddSuccessResponse;
import org.piwigo.io.response.StatusResponse;
import org.piwigo.io.response.SuccessResponse;

import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import rx.Observable;

public interface RestService {

    @POST("/ws.php?method=pwg.categories.add")
    @FormUrlEncoded
    Observable<AddSuccessResponse> addCategory(
            @Field("name") String name,
            @Field("parent") Integer parent,
            @Field("comment") String comment,
            @Field("visible") Boolean visible,
            @Field("status") String status,
            @Field("commentable") Boolean commentable);

    @GET("/ws.php?method=pwg.session.getStatus")
    Observable<StatusResponse> getStatus();

    @POST("/ws.php?method=pwg.session.login")
    @FormUrlEncoded
    Observable<Response> login(
            @Field("username") String username,
            @Field("password") String password);

    @GET("/ws.php?method=pwg.session.logout")
    Observable<SuccessResponse> logout();

}