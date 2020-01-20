package org.piwigo.bg;

import android.accounts.Account;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.piwigo.R;
import org.piwigo.io.RestService;
import org.piwigo.io.WebServiceFactory;
import org.piwigo.io.event.RefreshRequestEvent;
import org.piwigo.io.event.SnackbarShowEvent;
import org.piwigo.io.restmodel.AddCategoryResponse;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumService extends IntentService {

    public static final String KEY_CATEGORY_NAME = "category_name";
    public static final String KEY_PARENT_CATEGORY_ID = "category_id";

    @Inject
    WebServiceFactory webServiceFactory;

    public AlbumService() {
        super("AlbumService");
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String catName = intent.getStringExtra(KEY_CATEGORY_NAME);
        int parentId = intent.getIntExtra(KEY_PARENT_CATEGORY_ID, 0);

        RestService restService = webServiceFactory.create();

        Call<AddCategoryResponse> call = restService.addCategory(catName, parentId, null, null, null, null);

        call.enqueue(new Callback<AddCategoryResponse>() {
            @Override
            public void onResponse(Call<AddCategoryResponse> call, Response<AddCategoryResponse> response) {
                if (response.raw().code() == 200 && response.body().stat.equals("ok")) {
                    EventBus.getDefault().post(new SnackbarShowEvent(String.format(getResources().getString(R.string.create_album_success), catName), Snackbar.LENGTH_LONG));
                    EventBus.getDefault().post(new RefreshRequestEvent(parentId));
                }
                else {
                    // TODO: handle this properly for #161
                    Toast.makeText(getApplicationContext(), R.string.create_album_error, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<AddCategoryResponse> call, Throwable t) {
                // TODO: handle this properly for #161
                Toast.makeText(getApplicationContext(), R.string.create_album_error, Toast.LENGTH_LONG).show();
                Log.e("AlbumService", "Unable to create a new album:", t);
            }
        });
    }
}
