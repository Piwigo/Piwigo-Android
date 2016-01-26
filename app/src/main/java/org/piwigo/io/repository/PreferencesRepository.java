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

package org.piwigo.io.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Inject;

public class PreferencesRepository {

    private static final String KEY_DEFAULT_ACCOUNT = "default_account";

    SharedPreferences preferences;

    @Inject public PreferencesRepository(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setAccountName(String name) {
        set(KEY_DEFAULT_ACCOUNT, name);
    }

    public String getAccountName() {
        return preferences.getString(KEY_DEFAULT_ACCOUNT, null);
    }

    private void set(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

}
