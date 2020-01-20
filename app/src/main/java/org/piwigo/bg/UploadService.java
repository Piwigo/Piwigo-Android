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
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;

import com.tingyik90.snackprogressbar.SnackProgressBar;
import com.tingyik90.snackprogressbar.SnackProgressBarManager;

import org.greenrobot.eventbus.EventBus;
import org.piwigo.R;
import org.piwigo.accounts.UserManager;
import org.piwigo.bg.action.UploadAction;
import org.piwigo.helper.NetworkHelper;
import org.piwigo.helper.NotificationHelper;
import org.piwigo.io.RestService;
import org.piwigo.io.WebServiceFactory;
import org.piwigo.io.event.RefreshRequestEvent;
import org.piwigo.io.event.SnackProgressEvent;
import org.piwigo.io.restmodel.ImageUploadResponse;
import org.piwigo.io.restrepository.RestUserRepository;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadService extends IntentService {

    public static final String KEY_UPLOAD_QUEUE = "image_upload_queue";
    private int totalImagesCount = 0;
    private int uploadedImages = 1;
    private int snackbarId;
    private SnackProgressEvent snackProgressEvent;

    @Inject
    WebServiceFactory webServiceFactory;

    @Inject
    RestUserRepository userRepository;

    @Inject
    UserManager userManager;

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
        ImageUploadQueue<UploadAction> imageUploadQueue = (ImageUploadQueue<UploadAction>) intent.getSerializableExtra(KEY_UPLOAD_QUEUE);
        if (imageUploadQueue == null)
            return;
        totalImagesCount = imageUploadQueue.size();
        snackbarId = new Random().nextInt(100);
        snackProgressEvent = new SnackProgressEvent();

        snackProgressEvent.setSnackbarId(snackbarId);
        snackProgressEvent.setSnackbarType(SnackProgressBar.TYPE_CIRCULAR);
        snackProgressEvent.setSnackbarDuration(SnackProgressBarManager.LENGTH_INDEFINITE);
        snackProgressEvent.setAction(SnackProgressEvent.SnackbarUpdateAction.REFRESH);
        snackProgressEvent.setSnackbarDesc(getResources().getString(R.string.upload_started));
        onChunkedUploadStarted(imageUploadQueue);
    }

    protected void onChunkedUploadStarted(ImageUploadQueue<UploadAction> imageUploadQueue) {
        final UploadAction uploadAction;
        if ((uploadAction = imageUploadQueue.poll()) == null)
            return;

        //Chunks
        long tmpFileSize = getFileSize(uploadAction.getUploadData().getTargetUri());
        int chunkSize = getChunkSize();
        int expectedChunks = tmpFileSize <= chunkSize ? 1 : (int) (tmpFileSize / chunkSize) + 1;
        //Instances
        MultipartBody.Part fileParts[] = new MultipartBody.Part[expectedChunks];
        UploadPromise promise = new UploadPromise();
        RestService restService;
        //Local variables
        ArrayList<RequestBody> requestBodies;
        byte[] content;

        try (InputStream iStream = getContentResolver().openInputStream(uploadAction.getUploadData().getTargetUri())) {
            assert iStream != null;
            content = getBytes(iStream);
        } catch (FileNotFoundException e) {
            Log.e("UploadService", "FileNotFoundException", e.getCause());
            content = new byte[0];
        } catch (IOException e) {
            Log.e("UploadService", "IOException", e.getCause());
            content = new byte[0];
        }

        byte[][] splittedContent = splitArray(content, chunkSize);
        for (int i = 0; i < splittedContent.length; i++) {
            fileParts[i] = MultipartBody.Part.createFormData("file", uploadAction.getFileName(), RequestBody.create(MediaType.parse("image/*"), splittedContent[i]));
        }
        restService = webServiceFactory.create();
        requestBodies = createUploadRequest(uploadAction.getFileName(), getPhotoName(uploadAction.getFileName()));

        //Promise building
        promise.setFileParts(fileParts);
        promise.setRequestBodies(requestBodies);
        promise.setCatId(uploadAction.getUploadData().getCategoryId());
        promise.setCurrentChunk(0);
        promise.setTotalChunks(expectedChunks);
        if (uploadAction.getUploadData().getCategoryId() > 0)
            callChunkedUploadResponse(imageUploadQueue, restService, promise);
    }

    /**
     * Pick a chunk size depending of the network mode
     * @return chunkSize in bytes
     */
    public int getChunkSize()
    {
        int upSpeed = NetworkHelper.INSTANCE.getNetworkSpeed(getApplication());
        int chunkSize = userManager.getChunkSize(userManager.getActiveAccount().getValue());

        if(chunkSize > upSpeed * 1024 / 8){
            chunkSize = upSpeed * 1024 / 8;
        }

        return chunkSize;
    }

    /**
     * Get the file size from the given file URI (getting as a File does not work because of Android's way to manage temp content)
     *
     * @param fileUri (file path on the device)
     * @return fileSize (as long)
     */
    public long getFileSize(Uri fileUri) {
        Cursor cursor = getContentResolver().query(fileUri, null, null, null, null);
        long size = 0;

        if (cursor != null) {
            cursor.moveToFirst();
            size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
            cursor.close();
        }
        return size;
    }

    /**
     * Split the file in parts to send multiple Multipart.Parts
     *
     * @param source    byte array containing the whole image
     * @param chunkSize size of the chunks (1024 * 1024 = 1MB for instance)
     * @return an array of x chunks containing chunks of source
     */
    public static byte[][] splitArray(byte[] source, int chunkSize) {
        byte[][] newArray = new byte[(source.length / chunkSize) + 1][chunkSize];
        int start = 0;

        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = Arrays.copyOfRange(source, start, start + chunkSize);
            start += chunkSize;
        }
        return newArray;
    }

    /**
     * Return the name of the photo (basically the name of the file without the extension)
     * Used for display purpose
     *
     * @param imageName filename (IMG_001.jpg for instance)
     * @return the photo name as a string
     */
    private String getPhotoName(String imageName) {
        String photoName;

        if (imageName == null)
            return (null);
        if (imageName.indexOf(".") > 0)
            photoName = imageName.substring(0, imageName.lastIndexOf("."));
        else
            photoName = imageName;
        return (photoName);
    }

    /**
     * Create the request bodies for the upload form
     *
     * @param imageName  (filename with the extension)
     * @param photoName  (filename without the extension - used for display)
     * @return an array of RequestBody with the same "get" order than the parameters
     */
    private ArrayList<RequestBody> createUploadRequest(String imageName, String photoName) {
        ArrayList<RequestBody> requestBodies = new ArrayList<RequestBody>();

        requestBodies.add(0, RequestBody.create(MediaType.parse("text/plain"), imageName));
        requestBodies.add(1, RequestBody.create(MediaType.parse("text/plain"), photoName));
        requestBodies.add(2, RequestBody.create(MediaType.parse("text/plain"), userManager.sessionToken()));
        return (requestBodies);
    }

    private void callChunkedUploadResponse(ImageUploadQueue<UploadAction> imageUploadQueue, RestService restService, UploadPromise promise) {

        RefreshRequestEvent refreshEvent = new RefreshRequestEvent(promise.getCatId());

        //We have uploaded every chunk, let's jump to the next image
        if (promise.getCurrentChunk() > promise.getTotalChunks() - 1) {
            uploadedImages++;
            onChunkedUploadStarted(imageUploadQueue);
            return;
        }
        Call<ImageUploadResponse> call = restService.uploadChunkedImage(
                promise.getRequestBodies().get(0),
                promise.getCatId(),
                promise.getRequestBodies().get(1),
                promise.getRequestBodies().get(2),
                promise.getCurrentChunk(),
                promise.getTotalChunks(),
                promise.getFileParts()[promise.getCurrentChunk()]);

        call.enqueue(new Callback<ImageUploadResponse>() {
            @Override
            public void onResponse(@NonNull Call<ImageUploadResponse> call, @NonNull Response<ImageUploadResponse> response) {
                if (response.raw().code() == 200 && ("ok".equals(response.body().up_stat))) {
                    snackProgressEvent.setSnackbarProgressMax(promise.getTotalChunks());
                    snackProgressEvent.setSnackbarProgress(promise.getCurrentChunk());
                    snackProgressEvent.setSnackbarDesc(String.format(getResources().getString(R.string.upload_progress_body), uploadedImages, totalImagesCount));
                    EventBus.getDefault().post(snackProgressEvent);
                    promise.setCurrentChunk(promise.getCurrentChunk() + 1);
                    if (imageUploadQueue.size() <= 0 && promise.getCurrentChunk() == promise.getTotalChunks()) {
                        NotificationHelper.INSTANCE.sendNotification(getResources().getString(R.string.upload_success), String.format(getResources().getString(R.string.upload_success_body), totalImagesCount, response.body().up_result.up_category.catlabel), getApplicationContext());
                        snackProgressEvent.setSnackbarDesc(String.format(getResources().getString(R.string.upload_success_body), totalImagesCount, response.body().up_result.up_category.catlabel));
                        snackProgressEvent.setAction(SnackProgressEvent.SnackbarUpdateAction.KILL);
                        EventBus.getDefault().post(snackProgressEvent);
                        EventBus.getDefault().post(refreshEvent);
                    }
                    callChunkedUploadResponse(imageUploadQueue, restService, promise);
                } else {
                    String add = " (code " + response.raw().code() + ": " + response.raw().message() + ")";
                    // TODO: handle this properly for #161
                    if (response.body() != null) {
                        if (response.body().err != null) {
                            add = " (" + response.body().err.message + ")";
                        } else {
                            add = " (unknown)"; // this should be quite abnormal to happen but who knows...
                        }
                    }
                    NotificationHelper.INSTANCE.sendNotification(getResources().getString(R.string.upload_failed), getResources().getString(R.string.upload_error) + add, getApplicationContext());
                    snackProgressEvent.setSnackbarDesc(getResources().getString(R.string.upload_failed));
                    snackProgressEvent.setAction(SnackProgressEvent.SnackbarUpdateAction.KILL);
                    EventBus.getDefault().post(snackProgressEvent);
                    EventBus.getDefault().post(refreshEvent);
                }
            }

            @Override
            public void onFailure(Call<ImageUploadResponse> call, Throwable t) {
                // TODO: handle this properly for #161
                NotificationHelper.INSTANCE.sendNotification(getResources().getString(R.string.upload_failed), getResources().getString(R.string.upload_error), getApplicationContext());
                snackProgressEvent.setSnackbarDesc(getResources().getString(R.string.upload_failed));
                snackProgressEvent.setAction(SnackProgressEvent.SnackbarUpdateAction.KILL);
                EventBus.getDefault().post(snackProgressEvent);
                EventBus.getDefault().post(refreshEvent);
            }
        });
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

    public class UploadPromise {
        MultipartBody.Part[] fileParts;
        ArrayList<RequestBody> requestBodies;
        int catId, currentChunk, totalChunks;

        public void setFileParts(MultipartBody.Part[] fileParts) {
            this.fileParts = fileParts;
        }

        public void setRequestBodies(ArrayList<RequestBody> requestBodies) {
            this.requestBodies = requestBodies;
        }

        public void setCatId(int catId) {
            this.catId = catId;
        }

        public void setCurrentChunk(int currentChunk) {
            this.currentChunk = currentChunk;
        }

        public void setTotalChunks(int totalChunks) {
            this.totalChunks = totalChunks;
        }

        public MultipartBody.Part[] getFileParts() {
            return fileParts;
        }

        public ArrayList<RequestBody> getRequestBodies() {
            return requestBodies;
        }

        public int getCatId() {
            return catId;
        }

        public int getCurrentChunk() {
            return currentChunk;
        }

        public int getTotalChunks() {
            return totalChunks;
        }
    }

}
