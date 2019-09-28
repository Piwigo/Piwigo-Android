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
import androidx.appcompat.app.ActionBar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.piwigo.R;
import org.piwigo.ui.shared.BaseActivity;

public class PrivacyPolicyActivity extends BaseActivity {

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_privacy_policy);

        /*
        Uri data = getIntent().getData();
        so far we do not interpret the URI, because we only use
        org.piwigo.privacy_policy://show
        to open this activity
        */
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar bar = getSupportActionBar();
        if(bar != null) bar.setDisplayHomeAsUpEnabled(true);

        TextView policyText = findViewById(R.id.policyTextView);

        String mergedPolicyText = "<h1>" + getResources().getString(R.string.title_activity_privacy_policy) + "</h1>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_text) + "</p>";
        mergedPolicyText += "<h2>" + getResources().getString(R.string.privacy_intro_title) + "</h2>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_intro_text1) + "</p>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_intro_text2) + "</p>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_intro_text3) + "</p>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_intro_text4) + "</p>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_intro_text5) + "</p>";
        mergedPolicyText += "<h2>" + getResources().getString(R.string.privacy_what_title) + "</h2>";
        mergedPolicyText += "<h3>" + getResources().getString(R.string.privacy_what_subTitle1) + "</h3>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_what_subText1a) + "</p>";
        mergedPolicyText += "<h3>" + getResources().getString(R.string.privacy_what_subTitle2) + "</h3>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_what_subText2a) + "</p>";
        mergedPolicyText += "<h3>" + getResources().getString(R.string.privacy_what_subTitle3) + "</h3>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_what_subText3a) + "</p>";
        mergedPolicyText += "<h3>" + getResources().getString(R.string.privacy_what_subTitle4) + "</h3>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_what_subText4a) + "</p>";
        mergedPolicyText += "<h2>" + getResources().getString(R.string.privacy_why_title) + "</h2>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_why_text1) + "</p>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_why_text2) + "</p>";
        mergedPolicyText += "<h2>" + getResources().getString(R.string.privacy_how_title) + "</h2>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_how_text1) + "</p>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_how_text2) + "</p>";
        mergedPolicyText += "<h2>" + getResources().getString(R.string.privacy_security_title) + "</h2>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_security_text) + "</p>";
        mergedPolicyText += "<h2>" + getResources().getString(R.string.privacy_contact_title) + "</h2>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_contact_address) + "</p>";
        mergedPolicyText += "<p>" + getResources().getString(R.string.privacy_contact_email) + "</p>";

        if (Build.VERSION.SDK_INT >= 24) {
            policyText.setText(Html.fromHtml(mergedPolicyText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            policyText.setText(Html.fromHtml(mergedPolicyText));
        }

        policyText.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
