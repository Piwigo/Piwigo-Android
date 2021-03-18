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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
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
import org.piwigo.io.WebServiceFactory;
import org.piwigo.io.event.SimpleEvent;
import org.piwigo.io.event.SnackProgressEvent;
import org.piwigo.io.event.SnackbarShowEvent;
import org.piwigo.io.restrepository.RestUserRepository;
import org.piwigo.ui.about.AboutActivity;
import org.piwigo.ui.about.PrivacyPolicyActivity;
import org.piwigo.ui.account.ManageAccountsActivity;
import org.piwigo.ui.login.LoginActivity;
import org.piwigo.ui.settings.SettingsActivity;
import org.piwigo.ui.shared.BaseActivity;
import static org.piwigo.ui.main.MainViewModel.STAT_STATUS_FETCHED;

import java.io.File;
import java.util.ArrayList;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

public class MainActivity extends BaseActivity implements HasAndroidInjector {
    private static final String TAG = MainActivity.class.getName();
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 184;
    int SELECT_PICTURES = 1;

    @Inject
    DispatchingAndroidInjector<Object> androidInjector;
    @Inject
    MainViewModelFactory viewModelFactory;
    @Inject
    WebServiceFactory webServiceFactory;
    @Inject
    RestUserRepository userRepository;

    private final Handler handler = new Handler();

    private MainViewModel viewModel;

    private SpeedDialView speedDialView;

    private SnackProgressBarManager snackProgressBarManager;

    private ActionBarDrawerToggle mDrawerToggle;
    private Observable.OnPropertyChangedCallback mDrawerCallBack;
    private ActivityMainBinding mBinding;

    private boolean checkLoginRequired() {
       if (!userManager.hasAccounts()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
       }
       return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        DrawerHeaderBinding headerBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.drawer_header, mBinding.navigationView, false);

        viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);
        mBinding.setViewModel(viewModel);
        headerBinding.setViewModel(viewModel);
        mBinding.navigationView.addHeaderView(headerBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mBinding.drawerLayout,
                R.string.nav_drawer_open,
                R.string.nav_drawer_close
        );
        mBinding.drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.setDrawerIndicatorEnabled(viewModel.showingRootAlbum.get());

        mDrawerCallBack = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                mDrawerToggle.setDrawerIndicatorEnabled(((ObservableBoolean) sender).get());
            }
        };

        snackProgressBarManager = new SnackProgressBarManager(findViewById(android.R.id.content), null);

        if (checkLoginRequired()) {
            finish();
            return;
        }

        if (!NetworkHelper.INSTANCE.hasInternet(this)) {
            EventBus.getDefault().post(new SnackbarShowEvent(getResources().getString(R.string.no_internet), Snackbar.LENGTH_INDEFINITE));
        }

        speedDialView = mBinding.fab;

        setFABListener();
        refreshFAB(0);

        viewModel.loginStatus.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                Log.d("MainActivity", "login status changed to " + viewModel.loginStatus.get());
                if (viewModel.loginStatus.get() == STAT_STATUS_FETCHED) {

                    Fragment f = getSupportFragmentManager().findFragmentById(R.id.content);
                    if (f instanceof AlbumsFragment) {
                        Log.d("MainActivity", "fragment " + f.toString());
                        Integer cat = ((AlbumsFragment) f).getViewModel().getCategory();
                        Log.d("MainActivity", "category " + cat);
                        if (cat != null) {
                            ((AlbumsFragment) f).getViewModel().loadAlbums(cat);
                        } else {
                            ((AlbumsFragment) f).getViewModel().loadAlbums(0);
                        }
                        ((AlbumsFragment) f).getViewModel().forcedLoadAlbums();
                    }
                }
            }
        });

        final Observer<Account> accountObserver = account -> {
            Log.d("MainActivity", "accounts changed " + account.toString());
            // reload the albums on account changes
            initStartFragment();
            viewModel.changeAccount(account);
            viewModel.getError().observe(this, this::showError);
        };
        userManager.getActiveAccount().observe(this, accountObserver);

        if (savedInstanceState == null) {
            initStartFragment();
        }

        NavigationView nav = findViewById(R.id.navigation_view);
        nav.getMenu().getItem(0).setChecked(true);
        nav.setNavigationItemSelectedListener(menuItem -> {
            viewModel.drawerState.set(false);
            itemSelected(menuItem.getItemId());
            return true;
        });
    }

    private void initStartFragment() {
        Log.d("mainActivity", "initStartFragment");
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
        if (getCurrentCategoryId() != 0 && mDrawerToggle.isDrawerIndicatorEnabled()) {
            MainViewModel viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);
            viewModel.drawerState.set(false);
        } else {
            super.onBackPressed();
        }
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
        MainViewModel viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);
        viewModel.showingRootAlbum.addOnPropertyChangedCallback(mDrawerCallBack);
        speedDialView.setVisibility(viewModel.displayFab.get() ? View.VISIBLE : View.INVISIBLE);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding.drawerLayout.removeDrawerListener(mDrawerToggle);
        snackProgressBarManager.disable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainViewModel viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);
        viewModel.showingRootAlbum.removeOnPropertyChangedCallback(mDrawerCallBack);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SimpleEvent event) {
        if (event instanceof SnackbarShowEvent)
            Snackbar.make(findViewById(android.R.id.content), event.getMessage(), ((SnackbarShowEvent) event).getDuration()).show();
        if (event instanceof SnackProgressEvent) {
            SnackProgressEvent progressEvent = (SnackProgressEvent) event;
            SnackProgressBar bar = snackProgressBarManager.getSnackProgressBar(progressEvent.getSnackbarId());

            if (bar != null) {
                bar.setMessage(progressEvent.getSnackbarDesc());
                snackProgressBarManager.setProgress(progressEvent.getSnackbarProgress());
                if (progressEvent.getAction() == (SnackProgressEvent.SnackbarUpdateAction.KILL)) {
                    bar.setType(SnackProgressBar.TYPE_NORMAL);
                    bar.setAction(getResources().getString(R.string.button_ok), () -> snackProgressBarManager.dismiss());
                    snackProgressBarManager.show(progressEvent.getSnackbarId(), SnackProgressBarManager.LENGTH_LONG);
                }
                snackProgressBarManager.updateTo(progressEvent.getSnackbarId());
            } else {
                if (progressEvent.getAction().equals(SnackProgressEvent.SnackbarUpdateAction.KILL)) {
                    return;
                }
                bar = new SnackProgressBar(progressEvent.getSnackbarType(), progressEvent.getSnackbarDesc()).setIsIndeterminate(false);
                bar.setProgressMax(progressEvent.getSnackbarProgressMax());
                snackProgressBarManager.put(bar, progressEvent.getSnackbarId());
                snackProgressBarManager.show(bar, progressEvent.getSnackbarDuration());
            }
        }
    }

    /**
     * Returns an {@link AndroidInjector}.
     */
    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
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
            case R.id.nav_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
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
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void setFABListener() {
        speedDialView.setOnActionSelectedListener(speedDialActionItem -> {
            switch (speedDialActionItem.getId()) {
                case R.id.fab_create_album:
                case R.id.fab_create_subalbum:
                    if (isGuest()) {
                        DialogHelper.INSTANCE.showErrorDialog(R.string.not_admin, R.string.not_admin_explanation, this);
                        return (false);
                    }
                    promptAlbumCreation();
                    return (false);
                case R.id.fab_upload_photos:
                    if (isGuest()) {
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

    private boolean isGuest() {
        Account account = userManager.getActiveAccount().getValue();
        return userManager.isGuest(account);
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
        intent.putExtra(AlbumService.KEY_PARENT_CATEGORY_ID, catId);

        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURES) {
            if (resultCode == RESULT_OK) {
                ArrayList<UploadAction> images = new ArrayList<>();
                Intent intent = new Intent(this, UploadService.class);
                ImageUploadQueue<UploadAction> imageUploadQueue = new ImageUploadQueue<>();

                if (data.getClipData() != null) {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        UploadAction uploadAction = new UploadAction(getNameFromURI(
                                data.getClipData().getItemAt(i).toString(),
                                data.getClipData().getItemAt(i).getUri()));

                        uploadAction.getUploadData().setImageData(data.getClipData().getItemAt(i).toString());
                        uploadAction.getUploadData().setTargetUri(data.getClipData().getItemAt(i).getUri());
                        images.add(uploadAction);
                    }
                } else if (data.getData() != null) {
                    UploadAction uploadAction = new UploadAction(getNameFromURI(
                            data.toString(),
                            data.getData()));

                    uploadAction.getUploadData().setImageData(data.toString());
                    uploadAction.getUploadData().setTargetUri(data.getData());
                    images.add(uploadAction);
                }
                for (int i = 0; i < images.size(); i++) {
                    UploadAction uploadAction = images.get(i);

                    uploadAction.getUploadData().setImageName(uploadAction.getFileName());
                    uploadAction.getUploadData().setCategoryId(getCurrentCategoryId());
                    if (!imageUploadQueue.offer(uploadAction))
                        Log.e("ImageUploadQueue", "Unable to offer UploadAction..");
                }
                intent.putExtra(UploadService.KEY_UPLOAD_QUEUE, imageUploadQueue);
                startService(intent);
            }
        }
    }

    private String getNameFromURI(String item, Uri contentUri) {
        if (item.contains("content:"))
            return (getRealPathFromURI(contentUri));
        else if (item.contains("file:"))
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

    private void showError(Throwable throwable) {
        Snackbar.make(mBinding.getRoot(), throwable.getMessage(), Snackbar.LENGTH_LONG).setAction(R.string.show_details, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.INSTANCE.showLogDialog(getResources().getString(R.string.gen_error), throwable.getMessage(), throwable, "REASON: MainActivity.showError, LOGIN_STATUS: " + viewModel.loginStatus.get() + ", PIWIGO_VERSION = " + viewModel.piwigoVersion.get() + ", URL = " + viewModel.url.get(), mBinding.getRoot().getContext());
            }
        }).show();
    }
}

