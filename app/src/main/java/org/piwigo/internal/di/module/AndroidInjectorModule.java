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

package org.piwigo.internal.di.module;

import org.piwigo.bg.AlbumService;
import org.piwigo.bg.UploadService;
import org.piwigo.internal.di.scope.PerActivity;
import org.piwigo.internal.di.scope.PerFragment;
import org.piwigo.internal.di.scope.PerService;
import org.piwigo.ui.account.ManageAccountsActivity;
import org.piwigo.ui.launcher.LauncherActivity;
import org.piwigo.ui.login.LoginActivity;
import org.piwigo.ui.login.LoginActivityModule;
import org.piwigo.ui.main.AlbumsFragment;
import org.piwigo.ui.main.AlbumsFragmentModule;
import org.piwigo.ui.main.MainActivity;
import org.piwigo.ui.main.MainActivityModule;
import org.piwigo.ui.photoviewer.PhotoViewerDialogFragment;
import org.piwigo.ui.photoviewer.PhotoViewerDialogFragmentModule;
import org.piwigo.ui.settings.SettingsActivity;
import org.piwigo.ui.settings.SettingsActivityModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Module(includes = AndroidSupportInjectionModule.class)
public abstract class AndroidInjectorModule {

    @PerActivity @ContributesAndroidInjector() abstract LauncherActivity launcherActivity();

    @PerActivity @ContributesAndroidInjector(modules = LoginActivityModule.class) abstract LoginActivity loginActivity();

    @PerActivity @ContributesAndroidInjector(modules = MainActivityModule.class) abstract MainActivity mainActivity();

    @PerActivity @ContributesAndroidInjector(modules = MainActivityModule.class) abstract ManageAccountsActivity accountActivity();

    @PerFragment @ContributesAndroidInjector(modules = AlbumsFragmentModule.class) abstract AlbumsFragment albumsFragment();

    @PerFragment @ContributesAndroidInjector(modules = SettingsActivityModule.class) abstract SettingsActivity settingsActivity();

    @PerFragment @ContributesAndroidInjector(modules = PhotoViewerDialogFragmentModule.class) abstract PhotoViewerDialogFragment photoViewerDialogFragment();

    @PerService @ContributesAndroidInjector() abstract UploadService uploadService();

    @PerService @ContributesAndroidInjector() abstract AlbumService albumService();

}
