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

package org.piwigo.ui.about;

import android.arch.lifecycle.ViewModel;
import android.content.res.Resources;
import android.text.Html;
import android.text.Spanned;

import org.piwigo.BuildConfig;
import org.piwigo.R;

public class AboutViewModel extends ViewModel {
    private final Resources resources;

    public AboutViewModel(Resources resources) {
        this.resources = resources;
    }

    public Spanned getAboutText() {
        String appName = resources.getString(R.string.app_name);
        String versionName = BuildConfig.VERSION_NAME;

        String mergedAboutText = resources.getString(R.string.about_text, appName, versionName);
        return Html.fromHtml(mergedAboutText);
    }
}
