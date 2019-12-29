/*
 * Piwigo for Android
 * Copyright (C) 2016-2019 Piwigo Team http://piwigo.org
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

package org.piwigo.data.repository;

import android.Manifest;
import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import org.apache.commons.lang3.StringUtils;
import org.piwigo.accounts.UserManager;
import org.piwigo.data.db.CacheDBInternals;
import org.piwigo.data.model.Image;
import org.piwigo.data.model.ImageVariant;
import org.piwigo.data.model.PositionedItem;
import org.piwigo.helper.PermissionDeniedException;
import org.piwigo.io.PreferencesRepository;
import org.piwigo.data.db.CacheDatabase;
import org.piwigo.io.WebServiceFactory;
import org.piwigo.io.restmodel.Derivative;
import org.piwigo.io.restmodel.ImageInfo;
import org.piwigo.io.restrepository.RESTImageRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okio.BufferedSink;
import okio.Okio;

public class ImageRepository implements Observer<Account> {

    private final RESTImageRepository mRestImageRepo;
    private final UserManager mUserManager;
    private final Scheduler ioScheduler;
    private final Scheduler uiScheduler;
    private final PreferencesRepository mPreferences;
    private final Context mContext;

    private WebServiceFactory mWebServiceFactory;

    private Object dbAccountLock = new Object();
    private CacheDatabase mCache;
    private Account mAccount;

    @Inject public ImageRepository(RESTImageRepository restImageRepo, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler, UserManager userManager, PreferencesRepository preferences,
                                   WebServiceFactory webServiceFactory, Context context) {
        this.ioScheduler = ioScheduler;
        this.uiScheduler = uiScheduler;
        mRestImageRepo = restImageRepo;
        mUserManager = userManager;
        mPreferences = preferences;
        mWebServiceFactory = webServiceFactory;
        mContext = context;

        mUserManager.getActiveAccount().observeForever(this);
        synchronized (dbAccountLock) {
            mAccount = mUserManager.getActiveAccount().getValue();
            mCache = mUserManager.getDatabaseForAccount(mAccount);
        }
    }

    /**
     * fetch all images in given category
     *
     * @param categoryId
     * @return items with their position
     */
    public Observable<PositionedItem<Image>> getImages(@Nullable Integer categoryId) {
        CacheDatabase db;
        Account a;
        synchronized (dbAccountLock) {
            db = mCache; // this will keep the database if the account is switched. As the old DB will be closed this thread will be reporting an exception but we accept that for now
            a = mAccount;
        }
        if(db == null){
            return Observable.empty();
        }else {
            Single<List<String>> folder = db.categoryDao().getCategoryPath(categoryId);
            AtomicReference<String> folderStr = new AtomicReference<>(null);
            // TODO: #90 implement sorting
            return db.imageDao().getImagesInCategory(categoryId)
                    .subscribeOn(ioScheduler)
                    .observeOn(uiScheduler)
                    .flattenAsFlowable(s -> s)
                    .zipWith(Flowable.range(0, Integer.MAX_VALUE),
                            (item, counter) -> new PositionedItem<Image>(counter, item))

                    .concatWith(
                            mRestImageRepo.getImages(a, categoryId)
                                    .toFlowable(BackpressureStrategy.BUFFER)
                                    .zipWith(Flowable.range(0, Integer.MAX_VALUE), (info, counter) -> {
                                        Derivative d;
                                        switch (mPreferences.getString(PreferencesRepository.KEY_PREF_DOWNLOAD_SIZE)) {
                                            case "thumb":
                                                d = info.derivatives.thumb;
                                                break;
                                            case "small":
                                                d = info.derivatives.small;
                                                break;
                                            case "xsmall":
                                                d = info.derivatives.xsmall;
                                                break;
                                            case "medium":
                                                d = info.derivatives.medium;
                                                break;
                                            case "large":
                                                d = info.derivatives.large;
                                                break;
                                            case "xlarge":
                                                d = info.derivatives.xlarge;
                                                break;
                                            case "xxlarge":
                                                d = info.derivatives.xxlarge;
                                                break;
                                            case "square":
                                            default:
                                                d = info.derivatives.square;
                                        }

                                        Image i = new Image(info.elementUrl, d.width, d.height);
                                        i.name = info.name;
                                        i.file = info.file;
                                        i.id = info.id;
                                        i.author = info.author;
                                        i.description = info.comment;
                                        i.height = info.height;
                                        i.width = info.width;
                                        i.creationDate = info.dateCreation;
                                        i.availableDate = info.dateAvailable;
                                        db.imageDao().upsert(i);
                                        List<CacheDBInternals.ImageCategoryMap> join = new ArrayList<>(info.categories.size());
                                        for (ImageInfo.CategoryID c : info.categories) {
                                            join.add(new CacheDBInternals.ImageCategoryMap(c.id, i.id));
                                        }
                                        db.imageCategoryMapDao().insert(join);                                        synchronized (folderStr) {
                                            if (folderStr.get() == null) {
                                                folderStr.set(StringUtils.join(folder.blockingGet(), File.separator));
                                            }
                                        }

                                        List<ImageVariant> all = db.variantDao().variantsForImage(i.id, d.url).blockingGet();
                                        Map<String, String> addHeaders = null;
                                        if (all.size() == 1) {
                                            addHeaders = new HashMap<>();
                                            addHeaders.put("If-Modified-Since", all.get(0).lastModified);
                                            Log.i("ImageRepository.URLa", d.url + " Last-Modified = " + all.get(0).lastModified);
                                        } else if (all.size() > 1) {
                                            throw new RuntimeException("Invalid Cache State: " + all.size() + " variants for URL " + i.elementUrl);
                                        } else {
                                            Log.i("ImageRepository.URLa", d.url + " Last-Modified = n/a (not yet in DB)");

                                        }

                                        downloadURL(d.url, folderStr.get(), i.file, i.id, addHeaders, db, a).subscribe(new DisposableObserver<String>() {
                                            @Override
                                            public void onNext(String s) {
                                                boolean expose = mPreferences.getBool(PreferencesRepository.KEY_PREF_EXPOSE_PHOTOS);
                                                // TODO: insert variant
                                                Log.i("ImageRepository", "downloaded " + s);
                                                if (expose) {
                                                    // add file to mediastore
                                                    ContentResolver resolver = mContext.getContentResolver();
                                                    ContentValues values = new ContentValues();
                                                    values.put(MediaStore.Images.ImageColumns.DATA, s);
                                                    values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, i.name);
                                                    if (i.creationDate != null) {
                                                        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, i.creationDate.getTime());
                                                    }
                                                    if (i.description != null) {
                                                        values.put(MediaStore.Images.ImageColumns.DESCRIPTION, i.description);
                                                    }

                                                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                                }
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Log.e("ImageRepository", "download failed", e);
                                            }

                                            @Override
                                            public void onComplete() {
                                            }
                                        });


                                        // TODO: delete images in database after they have been deleted on the server
                                        // TODO: move image cache files if parent album was renamed
                                        return new PositionedItem<>(counter, i);
                                    })
                                    .subscribeOn(ioScheduler)
                                    .observeOn(uiScheduler)
                    )
                    .toObservable();
        }
    }


    private Observable<String> downloadURL(String url, String folder, String fileName, int imageId, @Nullable Map<String, String> addHeaders, CacheDatabase db, Account a){
        if(db == null){
            return Observable.empty();
        }else {

            org.piwigo.io.DownloadService downloadService = mWebServiceFactory.downloaderForAccount(a, addHeaders);

            Observable<String> result = downloadService.downloadFileAtUrl(url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap(response -> {
                        try {
                            boolean expose = mPreferences.getBool(PreferencesRepository.KEY_PREF_EXPOSE_PHOTOS);

                            String last_mod = response.headers().get("Last-Modified");
                            Log.i("ImageRepository.URLa", response.code() + " " + url + " Last-Modified = " + last_mod);
                            if (response.code() == 200) {
                                File root;
                                if (expose) {
                                    root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Piwigo");
                                } else {
                                    root = mContext.getExternalFilesDir(null);
                                }

                                String subdir = a.name
                                        + File.separator + folder;
                                String directory = root + File.separator + subdir;
                                String fullPath = directory
                                        + File.separator + fileName;

                                // TODO: store path in DB
                                File destinationFile = new File(fullPath);

                                int permissionCheck = ContextCompat.checkSelfPermission(mContext,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                // TODO ask for permission
                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {

                                    File outputDir = destinationFile.getParentFile();
                                    outputDir.mkdirs();
                                    BufferedSink bufferedSink = Okio.buffer(Okio.sink(destinationFile));
                                    bufferedSink.writeAll(response.body().source());
                                    bufferedSink.close();

                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inJustDecodeBounds = true;
                                    BitmapFactory.decodeFile(destinationFile.getAbsolutePath(), options);
                                    int h = options.outHeight;
                                    int w = options.outWidth;

                                    ImageVariant iv = new ImageVariant(imageId, w, h, fullPath, last_mod, url);
                                    Log.i("ImageRepository.URLa", "Inserting " + url + " Last-Modified = " + last_mod);
                                    db.variantDao().insert(iv);

                                    return Observable.just(fullPath);
                                } else {
                                    /* no permission */
                                    return Observable.error(new PermissionDeniedException(Manifest.permission.WRITE_EXTERNAL_STORAGE));
                                }
                            } else {
                                return Observable.empty();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            return Observable.error(e);
                        }
                    });
            return result;
        }
    }

    /**
     * store the img
     *
     * new images will be stored locally and add to the upload queue
     * for existing images the meta data will be updated
     *
     * exchanging the bitmap (locally or remotely) is not yet supported
     *
     * @param img
     */
    public void saveImage(Image img){
        // TODO: implement
        // if img has no id, add a new one, locally in the DB and add it to the upload queue
        // if img has an id, update the exisitng one
    }

    // TODO: move
    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Called when the account is changed.
     *
     * @param account The new data
     */
    @Override
    public void onChanged(Account account) {
        synchronized (dbAccountLock) {
            mAccount = mUserManager.getActiveAccount().getValue();
            mCache = mUserManager.getDatabaseForAccount(mAccount);
        }
    }
}


