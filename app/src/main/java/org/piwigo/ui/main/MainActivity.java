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
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.tingyik90.snackprogressbar.SnackProgressBar;
import com.tingyik90.snackprogressbar.SnackProgressBarManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.piwigo.R;
import org.piwigo.bg.AlbumService;
import org.piwigo.bg.ImageUploadQueue;
import org.piwigo.bg.UploadService;
import org.piwigo.bg.action.UploadAction;
import org.piwigo.databinding.ActivityMainBinding;
import org.piwigo.databinding.DrawerHeaderBinding;
import org.piwigo.helper.DialogHelper;
import org.piwigo.helper.NetworkHelper;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.io.event.SimpleEvent;
import org.piwigo.io.event.SnackProgressEvent;
import org.piwigo.io.event.SnackbarShowEvent;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.io.repository.UserRepository;
import org.piwigo.ui.about.AboutActivity;
import org.piwigo.ui.about.PrivacyPolicyActivity;
import org.piwigo.ui.account.ManageAccountsActivity;
import org.piwigo.ui.shared.BaseActivity;

import java.io.File;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import rx.Observable;

public class MainActivity extends BaseActivity implements HasSupportFragmentInjector {
    private static final String TAG = MainActivity.class.getName();
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 184;
    int SELECT_PICTURES = 1;

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

    private SnackProgressBarManager snackProgressBarManager;

    private ActionBarDrawerToggle mDrawerToggle;

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

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                R.string.nav_drawer_open,
                R.string.nav_drawer_close
        );
        binding.drawerLayout.addDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

//        mDrawerToggle.setDrawerIndicatorEnabled(false);// TODO: do this whenever we are displaying album > 0
        mDrawerToggle.setDrawerIndicatorEnabled(true);// TODO: do this whenever we are displaying abum 0
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        snackProgressBarManager = new SnackProgressBarManager(findViewById(android.R.id.content), null);

        if (!NetworkHelper.INSTANCE.hasInternet(this))
            EventBus.getDefault().post(new SnackbarShowEvent(getResources().getString(R.string.no_internet), Snackbar.LENGTH_INDEFINITE));

        currentAccount = userManager.getActiveAccount().getValue();
        speedDialView = binding.fab;

        setFABListener();
        refreshFAB(0);

        final Observer<Account> accountObserver = account -> {
            // reload the albums on account changes
            if (account != null && !account.equals(currentAccount)) {
                currentAccount = account;
                viewModel.username.set(userManager.getUsername(account));
                viewModel.url.set(userManager.getSiteUrl(account));
                viewModel.displayFab.set(!userManager.isGuest(currentAccount));
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
        Bundle bndl = new Bundle();
        // TODO: make configurable which is the root album (See #44 option to select Default Album)
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
        speedDialView.setVisibility(viewModel.displayFab.get() ? View.VISIBLE : View.INVISIBLE);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        snackProgressBarManager.disable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SimpleEvent event) {
        if (event instanceof SnackbarShowEvent)
            Snackbar.make(findViewById(android.R.id.content), event.getMessage(), ((SnackbarShowEvent) event).getDuration()).show();
        if (event instanceof SnackProgressEvent) {
            SnackProgressBar bar = snackProgressBarManager.getSnackProgressBar(((SnackProgressEvent) event).getSnackbarId());
            if (bar != null && ((SnackProgressEvent) event).getAction().equals(SnackProgressEvent.SnackbarUpdateAction.REFRESH)) {
                bar.setMessage(((SnackProgressEvent) event).getSnackbarDesc());
                snackProgressBarManager.updateTo(((SnackProgressEvent) event).getSnackbarId());
            } else if (bar != null && ((SnackProgressEvent) event).getAction().equals(SnackProgressEvent.SnackbarUpdateAction.KILL))
                snackProgressBarManager.dismissAll();
            else {
                bar = new SnackProgressBar(((SnackProgressEvent) event).getSnackbarType(), ((SnackProgressEvent) event).getSnackbarDesc()).setIsIndeterminate(true);
                snackProgressBarManager.put(bar, ((SnackProgressEvent) event).getSnackbarId());
                snackProgressBarManager.show(bar, SnackProgressBarManager.LENGTH_INDEFINITE);
            }
        }
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
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                break;
            case R.id.nav_privacy:
                startActivity(new Intent(getApplicationContext(), PrivacyPolicyActivity.class));
                break;

            default:
                DialogHelper.INSTANCE.showErrorDialog(R.string.not_implemented_title, R.string.not_implemented_msg, this);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }else if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void setFABListener() {
        speedDialView.setOnActionSelectedListener(speedDialActionItem -> {
            switch (speedDialActionItem.getId()) {
                case R.id.fab_create_album:
                case R.id.fab_create_subalbum:
                    if (!hasAdminRights()) {
                        DialogHelper.INSTANCE.showErrorDialog(R.string.not_admin, R.string.not_admin_explanation, this);
                        return (false);
                    }
                    promptAlbumCreation();
                    return (false);
                case R.id.fab_upload_photos:
                    if (!hasAdminRights()) {
                        DialogHelper.INSTANCE.showErrorDialog(R.string.not_admin, R.string.not_admin_explanation, this);
                        return (false);
                    }
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        checkExternalStoragePermissions();
                    } else
                        selectPhoto(); // Permission has already been granted
                    return (false);
                default:
                    return (false);
            }
        });
    }

    private void checkExternalStoragePermissions() {
        // Permission is not granted
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            DialogHelper.INSTANCE.showErrorDialog(R.string.storage_permission_title, R.string.storage_permission_explanation, this);
        } else {
            // No explanation needed; request the permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
    }

    private boolean hasAdminRights() {
        return (!userManager.isGuest(currentAccount));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectPhoto();
            }
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

    private void promptAlbumCreation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Piwigo_AlertDialog);
        final AppCompatEditText input = new AppCompatEditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setTitle(R.string.create_album_title);
        builder.setView(input);
        builder.setPositiveButton(R.string.button_ok, (dialog, which) -> createAlbum(input.getText().toString()));
        builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void createAlbum(String catName) {
        int catId;
        Intent intent;

        if (catName == null)
            return;
        catId = getCurrentCategoryId();
        intent = new Intent(this, AlbumService.class);
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
                if (data.getClipData() != null) {
                    Intent intent = new Intent(this, UploadService.class);
                    ImageUploadQueue<UploadAction> imageUploadQueue = new ImageUploadQueue<>();
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        Uri targetUri = data.getClipData().getItemAt(i).getUri();
                        String imageName = getNameFromURI(data.getClipData().getItemAt(i), targetUri);
                        UploadAction uploadAction = new UploadAction(imageName);

                        uploadAction.getUploadData().setTargetUri(targetUri);
                        uploadAction.getUploadData().setCategoryId(getCurrentCategoryId());
                        if (!imageUploadQueue.offer(uploadAction))
                            Log.e("ImageUploadQueue", "Unable to offer UploadAction..");
                    }
                    intent.putExtra(UploadService.KEY_UPLOAD_QUEUE, imageUploadQueue);
                    EventBus.getDefault().post(new SnackProgressEvent(SnackProgressBar.TYPE_CIRCULAR, String.format("Uploading your photos.."), 100, SnackProgressEvent.SnackbarUpdateAction.REFRESH));
                    startService(intent);
                }
            }
        }
    }

    private String getNameFromURI(ClipData.Item item, Uri contentUri) {
        if (item.toString().contains("content:"))
            return (getRealPathFromURI(contentUri));
        else if (item.toString().contains("file:"))
            return (contentUri.getPath());
        else
            return (null);
        /* TODO add proper error handling */
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

