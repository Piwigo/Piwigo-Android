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

    public static final String KEY_ACTIVE_ACCOUNT = "active_account";
    public static final String KEY_PREF_PHOTOS_PER_ROW = "photos_per_row";
    public static final String KEY_PREF_THUMBNAIL_SIZE = "thumbnail_size";

    public static final String DEFAULT_PREF_PHOTOS_PER_ROW = "3";
    public static final String DEFAULT_PREF_THUMBNAIL_SIZE = "medium";

    private final SharedPreferences preferences;

    @Inject
    public PreferencesRepository(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /* double check whether this is really what you want.
     * Maybe UserManager.setActiveAccount() is what you want */
    public void setActiveAccount(String name) {
        set(KEY_ACTIVE_ACCOUNT, name);
    }

    public String getActiveAccountName() {
        return preferences.getString(KEY_ACTIVE_ACCOUNT,null);
    }

    private void set(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key, String value) {
        return preferences.getString(key, value);
    }

    public int getInteger(String key, String value) {
        return Integer.parseInt(preferences.getString(key, value));
    }

}
