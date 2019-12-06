package org.piwigo.io.repository;

import android.accounts.Account;

import org.piwigo.accounts.UserManager;
import org.piwigo.io.RestService;
import org.piwigo.io.RestServiceFactory;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Scheduler;

public class MethodsRepository extends BaseRepository {
    @Inject
    MethodsRepository(RestServiceFactory restServiceFactory, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler, UserManager userManager) {
        super(restServiceFactory, ioScheduler, uiScheduler, userManager);
    }

    public Observable<List<String>> getMethodList(Account account) {
        RestService restService = restServiceFactory.createForAccount(account);

        return restService
                .getMethodList()
                .compose(applySchedulers())
                .map(methodListResponse -> {
                    if (methodListResponse.result != null) {
                        return methodListResponse.result.methods;
                    } else {
                        return null;
                    }
                });
    }
}
