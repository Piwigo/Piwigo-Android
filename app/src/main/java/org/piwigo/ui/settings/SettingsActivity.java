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

import android.os.Build;
import android.os.Bundle;

import org.piwigo.PiwigoApplication;
import org.piwigo.R;
import org.piwigo.helper.DialogHelper;
import org.piwigo.io.PreferencesRepository;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

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

        private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 815;
        PiwigoApplication piwigo;

        private ListPreference mPreferenceThumbnailSize;
        private SeekBarPreference mPreferencePhotosPerRow;
        private ListPreference mPreferenceDarkTheme;
// TODO: #222 private SwitchPreferenceCompat mPreferenceExposePhotos;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            piwigo = (PiwigoApplication)getActivity().getApplication();
            setPreferencesFromResource(R.xml.settings_preferences, rootKey);

            mPreferencePhotosPerRow = findPreference(PreferencesRepository.KEY_PREF_PHOTOS_PER_ROW);
            mPreferenceThumbnailSize = findPreference(PreferencesRepository.KEY_PREF_DOWNLOAD_SIZE);
            mPreferenceDarkTheme = findPreference(PreferencesRepository.KEY_PREF_COLOR_PALETTE);
// TODO: #222             mPreferenceExposePhotos = findPreference(PreferencesRepository.KEY_PREF_EXPOSE_PHOTOS);

            mPreferencePhotosPerRow.setOnPreferenceChangeListener((preference, value) -> true);

            mPreferenceThumbnailSize.setOnPreferenceChangeListener((preference, value) -> {
                mPreferenceThumbnailSize.setSummary(getString(R.string.settings_download_size_summary, value.toString()));
                return true;
            });

            mPreferenceDarkTheme.setOnPreferenceChangeListener(((preference, value) -> {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    DialogHelper.INSTANCE.showErrorDialog(R.string.restart_needed, R.string.restart_needed_explaination, getContext());
                } else {
                    piwigo.applyColorPalette((String)value);
                }

                return true;
            }));
/* TODO: #222
            mPreferenceExposePhotos.setOnPreferenceChangeListener((preference, value) -> {
                boolean set = Boolean.parseBoolean(value.toString());
                mPreferenceExposePhotos.setSummary(getString(set ? R.string.settings_expose_to_device_summary_pos :R.string.settings_expose_to_device_summary_neg));
                if(set){
                    int permissionCheck = ContextCompat.checkSelfPermission(this.getContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                            Toast.makeText(this.getContext(), R.string.perm_write_external_storage_xplain, Toast.LENGTH_LONG).show();
                        }
                        ActivityCompat.requestPermissions(this.getActivity(),
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                }
                return true;
            });
 */
        }
    }


}
