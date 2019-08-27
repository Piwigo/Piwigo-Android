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

import android.Manifest;
import android.accounts.Account;

import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;

import androidx.databinding.DataBindingUtil;

import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;

import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import org.piwigo.R;
import org.piwigo.bg.AlbumService;
import org.piwigo.bg.UploadService;
import org.piwigo.databinding.ActivityMainBinding;
import org.piwigo.databinding.DrawerHeaderBinding;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.io.repository.UserRepository;
import org.piwigo.ui.about.AboutActivity;
import org.piwigo.ui.about.PrivacyPolicyActivity;
import org.piwigo.ui.account.ManageAccountsActivity;
import org.piwigo.ui.shared.BaseActivity;
import org.piwigo.io.RestServiceFactory;

import java.io.File;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import rx.Observable;

public class MainActivity extends BaseActivity implements HasSupportFragmentInjector {
    private static final String TAG = MainActivity.class.getName();

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentInjector;
    @Inject
    MainViewModelFactory viewModelFactory;
    @Inject
    RestServiceFactory restServiceFactory;
    @Inject
    UserRepository userRepository;

    private Account currentAccount = null;

    private SpeedDialView speedDialView;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 184;

    int SELECT_PICTURES = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        speedDialView = findViewById(R.id.speedDial);
        setFABListener();
        refreshFAB(0);

        currentAccount = userManager.getActiveAccount().getValue();

        final Observer<Account> accountObserver = account -> {
            // reload the albums on account changes
            if (account != null && !account.equals(currentAccount)) {
                currentAccount = account;
                viewModel.username.set(userManager.getUsername(account));
                viewModel.url.set(userManager.getSiteUrl(account));
                /* Login to the new site after account changes.
                 * It seems quite unclean to do that here -> TODO: FIXME*/
                Observable<LoginResponse> a = userRepository.login(account);
                a.subscribe(new rx.Observer<LoginResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Login failed: " + e.getMessage());
                    }

                    @Override
                    public void onNext(LoginResponse loginResponse) {
                        Log.i(TAG, "Login succeeded: " + loginResponse.pwgId + " token: " + loginResponse.statusResponse.result.pwgToken);
// TODO: it is crazy to have this code here AND in LauncherActivity
                        userManager.setCookie(account, loginResponse.pwgId);
                        userManager.setToken(account, loginResponse.statusResponse.result.pwgToken);
                    }
                });
                initStartFragment(viewModel);
            }
            if (account == null) {
                viewModel.username.set("");
                viewModel.url.set("");
            }
        };
        userManager.getActiveAccount().observe(this, accountObserver);

        if (savedInstanceState == null) {
            initStartFragment(viewModel);
        }
    }

    private void initStartFragment(MainViewModel viewModel) {
        viewModel.title.set(getString(R.string.nav_albums));
        Bundle bndl = new Bundle();
        // TODO: make configurable which is the root album
        bndl.putInt("Category", 0);
        AlbumsFragment frag = new AlbumsFragment();
        frag.setArguments(bndl);

        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, frag)
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        refreshFAB(getCurrentCategoryId());
    }

    protected void refreshFAB(int categoryId) {
        speedDialView.close(true);
        speedDialView.clearActionItems();
        if (categoryId != 0) {
            speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_create_subalbum, R.drawable.ic_action_folder)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.material_white, getTheme()))
                    .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.piwigo_orange, getTheme()))
                    .setLabelColor(Color.BLACK)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.material_white, getTheme()))
                    .setLabelClickable(true)
                    .setLabel(R.string.fab_create_subalbum).create());
            speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_upload_photos, R.drawable.ic_action_cloud_upload)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.material_white, getTheme()))
                    .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.piwigo_orange, getTheme()))
                    .setLabelColor(Color.BLACK)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.material_white, getTheme()))
                    .setLabelClickable(true)
                    .setLabel(R.string.fab_upload_photos).create());
        } else
            speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_create_album, R.drawable.ic_action_folder)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.material_white, getTheme()))
                    .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.piwigo_orange, getTheme()))
                    .setLabelColor(Color.BLACK)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.material_white, getTheme()))
                    .setLabelClickable(true)
                    .setLabel(R.string.fab_create_album).create());
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);
        viewModel.navigationItemId.set(R.id.nav_albums);
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
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
            case R.id.nav_about:
                startActivity(new Intent(getApplicationContext(),
                        AboutActivity.class));
                break;
            case R.id.nav_privacy:
                startActivity(new Intent(getApplicationContext(),
                        PrivacyPolicyActivity.class));
                break;

            default:
                Toast.makeText(this, "not yet implemented", Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void setFABListener()
    {
        speedDialView.setOnActionSelectedListener(speedDialActionItem -> {
            switch (speedDialActionItem.getId()) {
                case R.id.fab_create_album:
                case R.id.fab_create_subalbum:
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.Theme_Piwigo_AlertDialog);
                    builder.setTitle(R.string.create_album_title);

                    final AppCompatEditText input = new AppCompatEditText(MainActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);
                    builder.setPositiveButton(R.string.button_ok, (dialog, which) -> createAlbum(input.getText().toString()));
                    builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.cancel());
                    builder.show();
                    return false;
                case R.id.fab_upload_photos:
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Permission is not granted
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            // Show an explanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.
                            Toast.makeText(this, R.string.storage_permission_explaination, Toast.LENGTH_LONG).show();
                        } else {
                            // No explanation needed; request the permission
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    } else {
                        // Permission has already been granted
                        selectPhoto();
                    }
                    return false;
                default:
                    return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectPhoto();
                }
            }
            break;
            default:
                break;
        }
    }

    private void selectPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                getResources().getString(R.string.title_select_image)), SELECT_PICTURES);
    }

    private int getCurrentCategoryId() {
        int catId = 0;
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content);
        if (f instanceof AlbumsFragment) {
            Integer cat = ((AlbumsFragment) f).getViewModel().getCategory();
            if (cat != null)
                catId = cat;
        }
        return (catId);
    }

    private void createAlbum(String catName) {
        int catId = getCurrentCategoryId();

        Intent intent = new Intent(this, AlbumService.class);
        intent.putExtra(AlbumService.KEY_CATEGORY_NAME, catName);
        intent.putExtra(AlbumService.KEY_ACCOUNT, userManager.getActiveAccount().getValue());
        intent.putExtra(AlbumService.KEY_PARENT_CATEGORY_ID, catId);

        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURES) {
            if (resultCode == RESULT_OK) {
                if (data.getData() != null) {

                    String imageName = "";
                    Uri targetUri = data.getData();
                    if (data.toString().contains("content:")) {
                        imageName = getRealPathFromURI(targetUri);
                    } else if (data.toString().contains("file:")) {
                        imageName = targetUri.getPath();
                    } else {
                        imageName = null;
                        /* TODO add proper error handling */
                    }

                    Intent intent = new Intent(this, UploadService.class);
                    intent.putExtra(UploadService.KEY_IMAGE_NAME, imageName);
                    intent.putExtra(UploadService.KEY_IMAGE_URI, targetUri);
                    intent.putExtra(UploadService.KEY_ACCOUNT, userManager.getActiveAccount().getValue());
                    intent.putExtra(UploadService.KEY_CATEGORY_ID, getCurrentCategoryId());

                    startService(intent);
                }
            }
        }
    }

    //-
    private String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(contentUri, null, null, null,
                    null);
            String alternative = contentUri.getLastPathSegment();
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int mediaDataIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            if (nameIndex > -1) {
                return cursor.getString(nameIndex);
            } else if (mediaDataIndex > -1) {
                return new File(cursor.getString(mediaDataIndex)).getName();
            } else {
                /* no usable column found, return the last Uri segment as name */
                return alternative;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}

