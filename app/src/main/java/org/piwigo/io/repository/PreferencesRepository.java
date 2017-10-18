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

package org.piwigo.io.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Inject;

public class PreferencesRepository {

    private static final String KEY_ACTIVE_ACCOUNT = "active_account";

    private final SharedPreferences preferences;

    @Inject PreferencesRepository(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setActiveAccount(String name) {
        set(KEY_ACTIVE_ACCOUNT, name);
    }

    public String getActiveAccount() {
        return preferences.getString(KEY_ACTIVE_ACCOUNT, null);
    }

    private void set(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
