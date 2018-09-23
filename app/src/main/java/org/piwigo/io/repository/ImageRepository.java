package org.piwigo.io.repository;

/**
 * Created by Jeff on 7/18/2017.
 */


import android.accounts.Account;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Pair;

import org.piwigo.io.RestService;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.io.model.Category;
import org.piwigo.io.model.ImageInfo;
import org.piwigo.io.model.ImageListResponse;
import org.piwigo.io.repository.ImageRepository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func2;




public class ImageRepository extends BaseRepository {

    @Inject public ImageRepository(RestServiceFactory restServiceFactory, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler) {
        super(restServiceFactory, ioScheduler, uiScheduler);
    }

    public Observable<List<ImageInfo>> getImages(Account account, @Nullable Integer categoryId) {
        RestService restService = restServiceFactory.createForAccount(account);

        return restService
                .getImages(categoryId)
                .map(imageListResponse -> imageListResponse.result.images)
                .compose(applySchedulers());


   }



    }


