/*
 * Piwigo for Android
 * Copyright (C) 2018-2018 Piwigo Team http://piwigo.org
 * Copyright (C) 2018-2018 Raphael Mack http://www.raphael-mack.de
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

package org.piwigo.bg;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import org.piwigo.R;
import org.piwigo.io.RestService;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.io.model.ImageUploadResponse;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadService extends IntentService {

    public static final String KEY_IMAGE_NAME = "image_name";
    public static final String KEY_IMAGE_URI = "image_uri";
    public static final String KEY_ACCOUNT = "account";
    public static final String KEY_CATEGORY_ID = "category_id";

    @Inject
    RestServiceFactory restServiceFactory;

    public UploadService() {
        super("UploadService");
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               Context#startService(Intent)}.
     *               This may be null if the service is being restarted after
     *               its process has gone away; see
     *               {@link Service#onStartCommand}
     *               for details.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Uri fileUri = intent.getParcelableExtra(KEY_IMAGE_URI);
        String imageName = intent.getStringExtra(KEY_IMAGE_NAME);
        Account curAccount = intent.getParcelableExtra(KEY_ACCOUNT);
        int catid = intent.getIntExtra(KEY_CATEGORY_ID, 0);

        Toast.makeText(this, "Uploading " + imageName, Toast.LENGTH_LONG).show();

        byte[] content;
        InputStream iStream = null;
        try {
            iStream = getContentResolver().openInputStream(fileUri);
            content = getBytes(iStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            /* TODO add proper error handling */
            content = new byte[0];
        } catch (IOException e) {
            e.printStackTrace();
            /* TODO add proper error handling */
            content = new byte[0];
        } finally {
            if(iStream != null){
                try {
                    iStream.close();
                } catch (IOException e) {
                    /* if this fails, we silently do nothing */
                }
            }
        }
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", imageName, RequestBody.create(MediaType.parse("image/*"), content));

        RestService restService = restServiceFactory.createForAccount(curAccount);
        String photoName;
        if (imageName.indexOf(".") > 0) {
            photoName = imageName.substring(0, imageName.lastIndexOf("."));
        }else{
            photoName = imageName;
        }

        AccountManager accountManager = AccountManager.get(this);
        String token = accountManager.getUserData(curAccount, "token");
// TODO: fix usage of token

//    public AccountManagerFuture<Bundle> getAuthToken(
//            final Account account, final String authTokenType, final Bundle options,
//            final Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {

//                    token = accountManager.peekAuthToken(curAccount,getResources().getString(R.string.account_type));
                    AccountManagerFuture<Bundle> a = accountManager.getAuthToken(curAccount,
                            getResources().getString(R.string.account_type),
                            null,
                            true, null, null
                    );
                    try {
                        token = a.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                    } catch (AuthenticatorException e) {
                        e.printStackTrace(); // TODO: add proper error handling
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (OperationCanceledException e) {
                        e.printStackTrace();
                    }

/*                    token = accountManager.getAuthToken(curAccount,
                            getResources().getString(R.string.account_type),
                    null,
                    true, null, null
                    );
                    */
        RequestBody imagefilenameBody = RequestBody.create(MediaType.parse("text/plain"), imageName);
        RequestBody imagenameBody = RequestBody.create(MediaType.parse("text/plain"), photoName);
        RequestBody tokenBody = RequestBody.create(MediaType.parse("text/plain"), token);

        if(catid < 1) {
            Toast.makeText(getApplicationContext(), R.string.uploading_not_to_cat_null, Toast.LENGTH_LONG).show();
        }else {
            // TODO: #40 replace toast by notification with a status bar
            Toast.makeText(getApplicationContext(), R.string.uploading_toast, Toast.LENGTH_LONG).show();
            //creating a call and calling the upload image method
            Call<ImageUploadResponse> call = restService.uploadImage(imagefilenameBody, catid, imagenameBody, tokenBody, filePart);

            //finally performing the call
            call.enqueue(new Callback<ImageUploadResponse>() {
                @Override
                public void onResponse(Call<ImageUploadResponse> call, Response<ImageUploadResponse> response) {
                    if (response.raw().code() == 200) {
                        if (response.body().up_stat.equals("ok")) {
                            // TODO: make text localizable
                            String uploadresp = "Uploaded: " + response.body().up_result.up_src + " to " + response.body().up_result.up_category.catlabel + "(" + Integer.toString(response.body().up_result.up_category.catid) + ")";
                            Toast.makeText(getApplicationContext(), uploadresp, Toast.LENGTH_LONG).show();
                            /* TODO: refresh the current album here */
                        } else {
                            Toast.makeText(getApplicationContext(), "Fail Response = " + response.body().up_message, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Upload Unsuccessful = " + response.raw().message(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ImageUploadResponse> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Upload Err = " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    /* get content of an open InputStream as byte array */
    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

}
