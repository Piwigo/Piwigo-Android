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

package org.piwigo.ui.main;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.piwigo.R;
import org.piwigo.databinding.ActivityMainBinding;
import org.piwigo.databinding.DrawerHeaderBinding;
import org.piwigo.helper.CommonVars;
import org.piwigo.io.RestService;
import org.piwigo.io.model.ImageUploadResponse;
import org.piwigo.ui.about.AboutActivity;
import org.piwigo.ui.account.ManageAccountsActivity;
import org.piwigo.ui.shared.BaseActivity;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.accounts.UserManager;

import java.io.File;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Provider;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements HasSupportFragmentInjector {

    @Inject DispatchingAndroidInjector<Fragment> fragmentInjector;
    @Inject MainViewModelFactory viewModelFactory;
    @Inject RestServiceFactory restServiceFactory;




    CommonVars comvars = CommonVars.getInstance();

    int SELECT_PICTURES = 1;

    @Override protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        DrawerHeaderBinding headerBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.drawer_header, binding.navigationView, false);


        MainViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);
        viewModel.getSelectedNavigationItemId().observe(this, this::itemSelected);

        binding.setViewModel(viewModel);
        headerBinding.setViewModel(viewModel);
        binding.navigationView.addHeaderView(headerBinding.getRoot());
        setSupportActionBar(binding.toolbar);

        final Observer<Account> accountObserver = account -> {
            // reload the albums on account changes
            if(account != null) {
                viewModel.username.set(userManager.getUsername(account));
                viewModel.url.set(userManager.getSiteUrl(account));
            }else{
                viewModel.username.set("");
                viewModel.url.set("");
            }
        };
        userManager.getActiveAccount().observe(this, accountObserver);

        if (savedInstanceState == null) {
            viewModel.setTitle(getString(R.string.nav_albums));
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content, new AlbumsFragment())
                    .commit();
        }
    }

    @Override protected void onResume() {
        super.onResume();
        MainViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);
        viewModel.navigationItemId.set(R.id.nav_albums);
    }

    @Override public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentInjector;
    }

    private void itemSelected(int itemId) {

        switch (itemId) {
            case R.id.nav_albums:
                break;
            case R.id.nav_manage_accounts:
                startActivity(new Intent(getApplicationContext(),
                        ManageAccountsActivity.class));
                break;
            case R.id.nav_upload:
                Intent intent = new Intent();
                intent.setType("image/*");
                /* TODO: fix API dependency EXTRA_ALLOW_MULTIPLE is not available in API 14 */
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), SELECT_PICTURES);
				break;
            case R.id.nav_about:
                startActivity(new Intent(getApplicationContext(),
                        AboutActivity.class));
                break;

			default:
                Toast.makeText(this,"not yet implemented",Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SELECT_PICTURES) {
            if(resultCode == RESULT_OK) {
                if(data.getData() != null) {


                    String imagePath = "";
                    Uri targetUri = data.getData();
                    if (data.toString().contains("content:")) {
                        imagePath = getRealPathFromURI(targetUri);
                    } else if (data.toString().contains("file:")) {
                        imagePath = targetUri.getPath();
                    } else {
                        imagePath = null;
                    }

                    File file = new File(imagePath);
                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
                    //do something with the image (save it to some directory or whatever you need to do with it here)
                   // String x = imagePath;
                    Account curAccount = comvars.getAccount();
                    RestService restService = restServiceFactory.createForAccount(comvars.getAccount());
                   // String result = restService.uploadImage("image1","14", "name2",filePart);    //Integer.toString(comvars.getValue())
                    String imageFilename = getFilename(imagePath,true);
                    String imageName = getFilename(imagePath,false);
                    AccountManager accountManager = AccountManager.get(this);
                    String token = accountManager.getUserData(curAccount, "token");
                    RequestBody imagefilenameBody = RequestBody.create(MediaType.parse("text/plain"), imageFilename);
                    RequestBody imagenameBody = RequestBody.create(MediaType.parse("text/plain"), imageName);
                    RequestBody tokenBody = RequestBody.create(MediaType.parse("text/plain"), token);
                    int catid = comvars.getValue();
                    if (catid > 0) {
                        Toast.makeText(getApplicationContext(), "Uploading Image", Toast.LENGTH_LONG).show();
                        //creating a call and calling the upload image method
                        Call<ImageUploadResponse> call = restService.uploadImage(imagefilenameBody, catid, imagenameBody, tokenBody, filePart);

                        //finally performing the call
                        call.enqueue(new Callback<ImageUploadResponse>() {
                            @Override
                            public void onResponse(Call<ImageUploadResponse> call, Response<ImageUploadResponse> response) {
                                if (response.body().up_stat.equals("ok")) {
                                    String uploadresp = "Uploaded: " + response.body().up_result.up_src + " to " + response.body().up_result.up_category.catlabel + "(" + Integer.toString(response.body().up_result.up_category.catid) + ")";
                                    Toast.makeText(getApplicationContext(), uploadresp, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Fail Response = " + response.body().up_message, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ImageUploadResponse> call, Throwable t) {
                                Toast.makeText(getApplicationContext(), "Upload Err = " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Upload - Select Album First!", Toast.LENGTH_LONG).show();
                    }



            }
        }
    }
    }

    //-
    private String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(contentUri, proj, null, null,
                    null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String getFilename(String filePath, Boolean withExt){
        String filename = filePath.substring(filePath.lastIndexOf("/")+1);
        String file;
        if (filename.indexOf(".") > 0) {
            file = filename.substring(0, filename.lastIndexOf("."));
        } else {
            file =  filename;
        }

        return withExt ? filename : file;
    }


}
