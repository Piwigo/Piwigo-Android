package org.piwigo.ui.settings;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.piwigo.PiwigoApplication;

public class SettingsPreferences {

    public static String KEY_THUMBNAIL_SIZE = "thumbnail_size";
    public static String KEY_NUMBER_ROW = "number_row";

    public static void setSettingPreference(String key, String value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(PiwigoApplication.getAppContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getSettingPreference(String key, String value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(PiwigoApplication.getAppContext());
        return settings.getString(key, value);
    }


}
