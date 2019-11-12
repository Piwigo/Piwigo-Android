/*
 * Piwigo for Android
 * Copyright (C) 2019-2019 Piwigo Team http://piwigo.org
 * Copyright (C) 2019-2019 Radko Varchola
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
package org.piwigo.ui.settings;

import android.os.Bundle;
import android.util.Log;

import org.piwigo.R;
import org.piwigo.io.repository.PreferencesRepository;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private ListPreference mPreferenceThumbnailSize;
        private SeekBarPreference mPreferencePhotosPerRow;
        private ListPreference mPreferenceDarkTheme;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey);

            mPreferencePhotosPerRow = findPreference(PreferencesRepository.KEY_PREF_PHOTOS_PER_ROW);
            mPreferenceThumbnailSize = findPreference(PreferencesRepository.KEY_PREF_DOWNLOAD_SIZE);
            mPreferenceDarkTheme = findPreference(PreferencesRepository.KEY_PREF_COLOR_PALETTE);

            mPreferencePhotosPerRow.setOnPreferenceChangeListener((preference, value) -> true);

            mPreferenceThumbnailSize.setOnPreferenceChangeListener((preference, value) -> {
                mPreferenceThumbnailSize.setSummary(getString(R.string.settings_download_size_summary, value.toString()));
                return true;
            });

            mPreferenceDarkTheme.setOnPreferenceChangeListener(((preference, value) -> {
                switch (value.toString()) {
                    case "light":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case "dark":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                    case "auto":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        break;
                }
                return true;
            }));
        }
    }
}
