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

package org.piwigo.bg;

import android.accounts.Account;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.piwigo.R;
import org.piwigo.io.WebServiceFactory;
import org.piwigo.io.event.RefreshRequestEvent;
import org.piwigo.io.event.SnackbarShowEvent;
import org.piwigo.io.restmodel.AddCategoryResponse;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadService extends IntentService {
    public static final String KEY_URL = "url";
    public static final String KEY_ACCOUNT = "account";

    @Inject
    WebServiceFactory webServiceFactory;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra(KEY_URL);
        Account curAccount = intent.getParcelableExtra(KEY_ACCOUNT);

        // TODO: there is a name clash with DownloadService
        org.piwigo.io.DownloadService downloadService = webServiceFactory.downloaderForAccount(curAccount);

        Observable<String> resp = downloadService.downloadFileAtUrl(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(response -> {
            try {
                String header = response.headers().get("Content-Disposition");
                String filename = header.replace("attachment; filename=", "");

                new File("/data/data/" + getPackageName() + "/games").mkdir();
                File destinationFile = new File("/data/data/" + getPackageName() + "/games/" + filename);

                BufferedSink bufferedSink = Okio.buffer(Okio.sink(destinationFile));
                bufferedSink.writeAll(response.body().source());
                bufferedSink.close();

                return Observable.just("kkk");

             } catch (IOException e) {
                e.printStackTrace();
                return Observable.just("err");
            }
        });
    }


/* TODO remove
    private static final DownloadService sDownloadManager = new DownloadService();

    private ThreadPoolExecutor mThreadPoolExecutor;

    private DownloadService(){
        mThreadPoolExecutor = new ThreadPoolExecutor(3, 8, 15, TimeUnit.SECONDS, )
    }

    static public PhotoTask startDownload(
            PhotoView imageView,
            DownloadTask downloadTask,
            boolean cacheFlag) {
        ...
        // Adds a download task to the thread pool for execution
        sInstance.
                downloadThreadPool.
                execute(downloadTask.getHTTPDownloadRunnable());
        ...
    }
 */


}
