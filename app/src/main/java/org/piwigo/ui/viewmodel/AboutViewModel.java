/*
 * Copyright 2017 Raphael Mack
 * Copyright 2017 Piwigo Team http://piwigo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.piwigo.ui.viewmodel;

import android.content.res.Resources;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.piwigo.BuildConfig;
import org.piwigo.R;
import org.piwigo.ui.activity.AboutActivity;

public class AboutViewModel extends BaseViewModel {
    private AboutActivity aboutActivity;

    public void setView(AboutActivity act) {
        this.aboutActivity = act;
    }
    public Spanned getAboutText() {
        Resources res = aboutActivity.getResources();
        String appName = res.getString(R.string.app_name);
        String versionName = BuildConfig.VERSION_NAME;

        ((TextView)aboutActivity.findViewById(R.id.aboutTextView)).setMovementMethod(LinkMovementMethod.getInstance());

        String mergedAboutText = res.getString(R.string.about_text, appName, versionName);
        return Html.fromHtml(mergedAboutText);

    }
}
