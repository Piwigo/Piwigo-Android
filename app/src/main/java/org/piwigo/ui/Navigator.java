/*
 * Copyright 2016 Phil Bayfield https://philio.me
 * Copyright 2016 Piwigo Team http://piwigo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.piwigo.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;

import org.piwigo.ui.activity.LoginActivity;
import org.piwigo.ui.activity.MainActivity;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Navigator {

    public static final int REQUEST_CODE_LOGIN = 1;

    @Inject public Navigator() {}

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
