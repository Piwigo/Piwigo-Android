/*
 * Piwigo for Android
 * Copyright (C) 2016-2019 Piwigo Team http://piwigo.org
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

package org.piwigo.io;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class PreferencesRepository {

    /* For adding new preferences, don't forget to add
     * - add a KEY String
     * - add a DEFAULT value
     * - add the default to the defaults Map
     * - add a setting in settings_preference.xml
     * - add a setOnPreferenceChangeListener in SettingsActivity to adjust the summary
     * */

    public static final String KEY_ACTIVE_ACCOUNT = "active_account";
    public static final String KEY_PREF_PHOTOS_PER_ROW = "photos_per_row";
    public static final String KEY_PREF_DOWNLOAD_SIZE = "download_size";
    public static final String KEY_PREF_COLOR_PALETTE = "color_palette";
    public static final String KEY_PREF_EXPOSE_PHOTOS = "expose_photos_to_device";

    public static final int DEFAULT_PREF_PHOTOS_PER_ROW = 3;
    public static final String DEFAULT_PREF_DOWNLOAD_SIZE = "medium";
    public static final String DEFAULT_PREF_COLOR_PALETTE = "light";
    public static final Boolean DEFAULT_PREF_EXPOSE_PHOTOS = false;

    private static final Map<String, Object> defaults;
    static {
        Map<String, Object> mutableMap = new HashMap<>();
        mutableMap.put(KEY_PREF_PHOTOS_PER_ROW, DEFAULT_PREF_PHOTOS_PER_ROW);
        mutableMap.put(KEY_PREF_DOWNLOAD_SIZE, DEFAULT_PREF_DOWNLOAD_SIZE);
        mutableMap.put(KEY_PREF_COLOR_PALETTE, DEFAULT_PREF_COLOR_PALETTE);
        mutableMap.put(KEY_PREF_EXPOSE_PHOTOS, DEFAULT_PREF_EXPOSE_PHOTOS);
        defaults = Collections.unmodifiableMap(mutableMap);
    }

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

    public String getString(String key) {
        Object def = defaults.get(key);
        if(def instanceof String) {
            return preferences.getString(key, (String) def);
        } else {
            /* no default string configured */
            return preferences.getString(key, null);
        }
    }

    public int getInt(String key) {
        Object def = defaults.get(key);
        if(def instanceof Integer) {
            return preferences.getInt(key, (Integer) def);
        } else {
            /* no default int configured */
            return preferences.getInt(key, -1);
        }
    }

    public boolean getBool(String key){
        Object def = defaults.get(key);
        if(def instanceof Boolean) {
            return preferences.getBoolean(key, (Boolean) def);
        } else {
            /* no default boolean configured */
            return preferences.getBoolean(key, false);
        }
    }
}
