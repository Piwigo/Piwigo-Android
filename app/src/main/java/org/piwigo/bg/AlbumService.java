package org.piwigo.bg;

import android.accounts.Account;
import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

import org.piwigo.R;
import org.piwigo.io.RestService;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.io.model.AddCategoryResponse;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumService extends IntentService {

    public static final String KEY_CATEGORY_NAME = "category_name";
    public static final String KEY_PARENT_CATEGORY_ID = "category_id";
    public static final String KEY_ACCOUNT = "account";

    @Inject
    RestServiceFactory restServiceFactory;

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
        Account curAccount = intent.getParcelableExtra(KEY_ACCOUNT);
        int parentId = intent.getIntExtra(KEY_PARENT_CATEGORY_ID, 0);

        RestService restService = restServiceFactory.createForAccount(curAccount);

        Call<AddCategoryResponse> call = restService.addCategory(catName, parentId, null, null, null, null);

        call.enqueue(new Callback<AddCategoryResponse>() {
            @Override
            public void onResponse(Call<AddCategoryResponse> call, Response<AddCategoryResponse> response) {
                if (response.raw().code() == 200) {
                    if (response.body().stat.equals("ok"))
                        Toast.makeText(getApplicationContext(), R.string.create_album_success, Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getApplicationContext(), R.string.create_album_error, Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(getApplicationContext(), R.string.create_album_error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<AddCategoryResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
