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
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import org.piwigo.R;
import org.piwigo.io.repository.PreferencesRepository;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import dagger.android.AndroidInjection;


public class SettingsActivity extends PreferenceActivity {

    @Inject
    PreferencesRepository preferences;

    ListPreference mPreferencePhotosPerRow;
    ListPreference mPreferenceThumbnailSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preferences);


        mPreferenceThumbnailSize = (ListPreference) getPreferenceScreen().findPreference(PreferencesRepository.KEY_PREF_THUMBNAIL_SIZE);
        mPreferencePhotosPerRow = (ListPreference) getPreferenceScreen().findPreference(PreferencesRepository.KEY_PREF_PHOTOS_PER_ROW);

        String photosPerRowValue = preferences.getString(PreferencesRepository.KEY_PREF_PHOTOS_PER_ROW, PreferencesRepository.DEFAULT_PREF_PHOTOS_PER_ROW);
        String thumbnailSizeValue = preferences.getString(PreferencesRepository.KEY_PREF_THUMBNAIL_SIZE, PreferencesRepository.DEFAULT_PREF_THUMBNAIL_SIZE);

        mPreferenceThumbnailSize.setSummary(thumbnailSizeValue);
        mPreferencePhotosPerRow.setSummary(photosPerRowValue);

        mPreferencePhotosPerRow.setOnPreferenceChangeListener((preference, value) -> {
            mPreferencePhotosPerRow.setSummary(value.toString());
            return true;
        });

        mPreferenceThumbnailSize.setOnPreferenceChangeListener((preference, value) -> {
            mPreferenceThumbnailSize.setSummary(value.toString());
            return true;
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar)LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        bar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
        root.addView(bar, 0);
        bar.setNavigationOnClickListener(v -> finish());
    }
}