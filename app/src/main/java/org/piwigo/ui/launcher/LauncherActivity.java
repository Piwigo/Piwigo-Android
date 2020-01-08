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

package org.piwigo.ui.launcher;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import org.piwigo.R;
import org.piwigo.databinding.ActivityLauncherBinding;
import org.piwigo.io.model.LoginResponse;
import org.piwigo.io.repository.UserRepository;
import org.piwigo.ui.login.LoginActivity;
import org.piwigo.ui.main.MainActivity;
import org.piwigo.ui.shared.BaseActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import rx.Subscriber;

public class LauncherActivity extends BaseActivity {

    public static final int REQUEST_CODE_LOGIN = 1;

    private static final String TAG = LauncherActivity.class.getName();
    private final Handler handler = new Handler();
    private ActivityLauncherBinding binding;

    @Inject
    UserRepository userRepository;

    @Override protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_launcher);

        if (userManager.hasAccounts()) {

            Account a = userManager.getActiveAccount().getValue();
            userRepository.login(a)
                    .subscribe(new Subscriber<LoginResponse>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
//                                Log.e(TAG, "Login failed: " + e.toString()); //getMessage());
                            // TODO: handle this properly... (can be triggered with empty password)
                        }

                        @Override
                        public void onNext(LoginResponse loginResponse) {
                            Log.i(TAG, "Login succeeded: " + loginResponse.pwgId);
                            userManager.setCookie(a, loginResponse.pwgId);
                            userManager.setToken(a, loginResponse.statusResponse.result.pwgToken);
                            userManager.setChunkSize(a, loginResponse.statusResponse.result.uploadFormChunkSize);
                        }
                    });

            handler.postDelayed(this::startMain, 1000);
        } else {
            handler.postDelayed(this::startLogin, 500);
        }
    }

    private void startMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void startLogin() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            startLoginActivity(this, binding.logo, getString(R.string.logo_transition_name));
        } else {
            startLoginActivity(this);
        }
        // do not call finish here as the animation is using elements
    }

    private void startLoginActivity(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivityForResult(intent, REQUEST_CODE_LOGIN);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startLoginActivity(Activity activity, View sharedElement, String sharedElementName) {
        Intent intent = new Intent(activity, LoginActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, sharedElement, sharedElementName);
        activity.startActivityForResult(intent, REQUEST_CODE_LOGIN, options.toBundle());
    }

}
