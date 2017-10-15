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
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.piwigo.BuildConfig;
import org.piwigo.R;

public class AboutViewModel extends ViewModel {
    private AboutActivity aboutActivity;
    private final Resources resources;

    public AboutViewModel(Resources resources) {
        this.resources = resources;
    }

    public void setView(AboutActivity act) {
        /* TODO: check whether this is wanted */
        this.aboutActivity = act;
    }
    public Spanned getAboutText() {
        Resources res = aboutActivity.getResources();
        /* todo replace by resources */
        String appName = res.getString(R.string.app_name);
        String versionName = BuildConfig.VERSION_NAME;

        ((TextView)aboutActivity.findViewById(R.id.aboutTextView)).setMovementMethod(LinkMovementMethod.getInstance());

        String mergedAboutText = res.getString(R.string.about_text, appName, versionName);
        return Html.fromHtml(mergedAboutText);

    }
}
