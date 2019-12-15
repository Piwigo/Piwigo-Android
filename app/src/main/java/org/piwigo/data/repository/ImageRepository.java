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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.piwigo.PiwigoApplication;
import org.piwigo.accounts.UserManager;
import org.piwigo.bg.DownloadService;
import org.piwigo.data.db.CacheDBInternals;
import org.piwigo.data.db.ImageCategoryMapDao;
import org.piwigo.data.model.Image;
import org.piwigo.data.model.PositionedItem;
import org.piwigo.io.PreferencesRepository;
import org.piwigo.data.db.CacheDatabase;
import org.piwigo.io.WebServiceFactory;
import org.piwigo.io.restmodel.Derivative;
import org.piwigo.io.restmodel.ImageInfo;
import org.piwigo.io.restrepository.RESTImageRepository;
import org.reactivestreams.Subscriber;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okio.BufferedSink;
import okio.Okio;

public class ImageRepository {

    private final RESTImageRepository mRestImageRepo;
    private final UserManager mUserManager;
    private final Scheduler ioScheduler;
    private final Scheduler uiScheduler;
    private final PreferencesRepository mPreferences;
    private final Context mContext;

    private CacheDatabase mCache;
    private WebServiceFactory mWebServiceFactory;

    @Inject public ImageRepository(RESTImageRepository restImageRepo, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler, UserManager userManager, PreferencesRepository preferences, CacheDatabase cache,
                                   WebServiceFactory webServiceFactory, Context context) {
        this.ioScheduler = ioScheduler;
        this.uiScheduler = uiScheduler;
        mRestImageRepo = restImageRepo;
        mUserManager = userManager;
        mPreferences = preferences;
        mWebServiceFactory = webServiceFactory;
        mContext = context;

        mCache = cache;


    }

    /**
     * fetch all images in given category
     *
     * @param categoryId
     * @return items with their position
     */
    public Observable<PositionedItem<Image>> getImages(@Nullable Integer categoryId) {
// TODO: #90 implement sorting
    return mCache.imageDao().getImagesInCategory(categoryId)
            .subscribeOn(ioScheduler)
            .observeOn(uiScheduler)
            .flattenAsFlowable(s -> s)
            .zipWith(Flowable.range(0, Integer.MAX_VALUE),
                    (item, counter) -> new PositionedItem<Image>(counter, item))

            .concatWith(
                    mRestImageRepo.getImages(mUserManager.getActiveAccount().getValue(), categoryId)
                    .toFlowable(BackpressureStrategy.BUFFER)
                    .zipWith(Flowable.range(0, Integer.MAX_VALUE), (info, counter) -> {
                        Derivative d;
                        switch(mPreferences.getString(PreferencesRepository.KEY_PREF_DOWNLOAD_SIZE)){
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

                        Image i = new Image(d.url, d.width, d.height);
                        i.name = info.name;
                        i.file = info.file;
                        i.id = info.id;
                        i.author = info.author;
                        i.comment = info.comment;
                        i.height = info.height;
                        i.width = info.width;
                        i.creationDate = info.dateCreation;
                        i.availableDate = info.dateAvailable;
                        mCache.imageDao().upsert(i);
                        List<CacheDBInternals.ImageCategoryMap> join = new ArrayList<>(info.categories.size());
                        for(ImageInfo.CategoryID c : info.categories){
                            join.add(new CacheDBInternals.ImageCategoryMap(c.id, i.id));
                        }
                        mCache.imageCategoryMapDao().insert(join);
                        downloadURL(i.elementUrl, i.file, i.id);

     // TODO: delete images in database after they have been deleted on the server
                        return new PositionedItem<>(counter, i);
                    })
            .subscribeOn(ioScheduler)
            .observeOn(uiScheduler)
            )
        .toObservable();
    }


    public void downloadURL(String url, String filePath, int imageId){
        Account a = mUserManager.getActiveAccount().getValue();
        org.piwigo.io.DownloadService downloadService = mWebServiceFactory.downloaderForAccount(a);

        Observable<String> resp = downloadService.downloadFileAtUrl(url)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(response -> {
                    try {
                        String last_mod = response.headers().get("Last-Modified");
                        String length = response.headers().get("Content-Length");
//TODO: adjust path
                        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "Piwigo"
                                + File.separator + a.name
                                + File.separator + filePath;
// TODO: store path in DB
                        File destinationFile = new File(path);

                        int permissionCheck = ContextCompat.checkSelfPermission(mContext,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE);

                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {

                            File outputDir = destinationFile.getParentFile();
                            outputDir.mkdirs();

                            BufferedSink bufferedSink = Okio.buffer(Okio.sink(destinationFile));
                            bufferedSink.writeAll(response.body().source());
                            bufferedSink.close();

                            return Observable.just("kkk");
                        } else {
                            /* no permission, return null */
                            return Observable.just("no Perm");
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                        return Observable.just("err");
                    }
                });

        resp.subscribe(new DisposableObserver<String>(){
                    @Override
                    public void onNext(String s) {
                        Log.e("ImageRepository", "downloaded " + s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("ImageRepository", "failed", e);
                    }

                    /**
                     * Notifies the Observer that the {@link Observable} has finished sending push-based notifications.
                     * <p>
                     * The {@link Observable} will not call this method if it calls {@link #onError}.
                     */
                    @Override
                    public void onComplete() {
                        Log.e("ImageRepository", "done ");
                    }
                });
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


}


