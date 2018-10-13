/*
 * Piwigo for Android
 * Copyright (C) 2016-2018 Piwigo Team http://piwigo.org
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

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.piwigo.BuildConfig;
import org.piwigo.R;
import org.piwigo.ui.shared.BaseActivity;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(this);

        View contentView = inflater.inflate(R.layout.activity_about, null, false);

        setContentView(contentView);
        TextView aboutText = (TextView)findViewById(R.id.aboutTextView);

        String appName = getResources().getString(R.string.app_name);
        String contributors = getResources().getString(R.string.contributors);
        String libraries = getResources().getString(R.string.libraries);
        String versionName = BuildConfig.VERSION_NAME;

        String mergedAboutText = "<h1>" + appName + "</h1>";
        mergedAboutText += getResources().getString(R.string.about_text_version, versionName);
        mergedAboutText += "<h1>" + getResources().getString(R.string.about_text_licence_h) + "</h1>";
        mergedAboutText += "<p>" + getResources().getString(R.string.about_text_licence, appName) + "</p>";
        mergedAboutText += "<p>" + getResources().getString(R.string.about_text_license_2) + "</p>";

        mergedAboutText += "<h1>" + getResources().getString(R.string.about_text_contact_h) + "</h1>";
        mergedAboutText += "<p>" + getResources().getString(R.string.about_text_contact) + "</p>";
        mergedAboutText += "<p>" + getResources().getString(R.string.about_text_contact_2) + "</p>";

        mergedAboutText += "<h1>" + getResources().getString(R.string.about_text_support_h) + "</h1>";
        mergedAboutText += "<p>" + getResources().getString(R.string.about_text_support, appName) + "</p>";

        mergedAboutText += "<h1>" + getResources().getString(R.string.about_text_cont_h) + "</h1>";
        mergedAboutText += "<p>" + contributors + "</p>";
        mergedAboutText += "<p>" + getResources().getString(R.string.about_text_cont_2) + "</p>";

        mergedAboutText += "<h1>" + getResources().getString(R.string.about_text_lib_h) + "</h1>";
        mergedAboutText += "<p>" + getResources().getString(R.string.about_text_lib) + "</p>";
        mergedAboutText += "<p>" + libraries + "</p>";

        if (Build.VERSION.SDK_INT >= 24) {
            aboutText.setText(Html.fromHtml(mergedAboutText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            aboutText.setText(Html.fromHtml(mergedAboutText));
        }

        aboutText.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
