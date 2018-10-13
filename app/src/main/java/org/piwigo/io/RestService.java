/*
 * Piwigo for Android
 * Copyright (C) 2016-2017 Piwigo Team http://piwigo.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.piwigo.io;

import org.piwigo.io.model.AddCategoryResponse;
import org.piwigo.io.model.CategoryListResponse;
import org.piwigo.io.model.GetImageInfoResponse;
import org.piwigo.io.model.ImageListResponse;
import org.piwigo.io.model.ImageUploadResponse;
import org.piwigo.io.model.StatusResponse;
import org.piwigo.io.model.SuccessResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import rx.Observable;

public interface RestService {

    @POST("ws.php?method=pwg.session.login") @FormUrlEncoded Observable<Response<SuccessResponse>> login(
            @Field("username") String username,
            @Field("password") String password
    );

    @GET("ws.php?method=pwg.session.getStatus") Observable<StatusResponse> getStatus();

    @GET("ws.php?method=pwg.session.getStatus") Observable<StatusResponse> getStatus(@Header("Cookie") String pwgIdCookie);

    @GET("ws.php?method=pwg.session.logout") Observable<SuccessResponse> logout();

    @POST("ws.php?method=pwg.categories.add") @FormUrlEncoded Observable<AddCategoryResponse> addCategory(
            @Field("name") String name,
            @Field("parent") Integer parent,
            @Field("comment") String comment,
            @Field("visible") Boolean visible,
            @Field("status") String status,
            @Field("commentable") Boolean commentable
    );

    @GET("ws.php?method=pwg.categories.getList") Observable<CategoryListResponse> getCategories(
            @Query("cat_id") Integer categoryId,
            @Query("thumbnail_size") String thumbnailSize
    );

    @GET("ws.php?method=pwg.images.getInfo") Observable<GetImageInfoResponse> getImageInfo(
            @Query("image_id") int imageId
    );


    //JCA added
    @GET("ws.php?method=pwg.categories.getImages")
    Observable<ImageListResponse> getImages(@Query("cat_id") int categoryId);


    @Multipart
    @POST("ws.php?method=pwg.images.upload")
    Call<ImageUploadResponse> uploadImage(
            @Part("image") RequestBody image,
            @Part("category") Integer category,
            @Part("name") RequestBody name,
            @Part("pwg_token") RequestBody token,
            @Part MultipartBody.Part filePart
    );

}