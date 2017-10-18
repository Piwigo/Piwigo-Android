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

package org.piwigo.ui.shared;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;

import org.piwigo.ui.launcher.LauncherActivity;
import org.piwigo.ui.login.LoginActivity;
import org.piwigo.ui.main.MainActivity;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Navigator {

    public static final int REQUEST_CODE_LOGIN = 1;

    @Inject Navigator() {}

    public void startLauncher(Context context) {
        Intent intent = new Intent(context, LauncherActivity.class);
        context.startActivity(intent);
    }

    public void startLogin(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivityForResult(intent, REQUEST_CODE_LOGIN);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startLogin(Activity activity, View sharedElement, String sharedElementName) {
        Intent intent = new Intent(activity, LoginActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, sharedElement, sharedElementName);
        activity.startActivityForResult(intent, REQUEST_CODE_LOGIN, options.toBundle());
    }

    public void startMain(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

}
