/*
 * Copyright 2015 Phil Bayfield https://philio.me
 * Copyright 2015 Piwigo Team http://piwigo.org
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

package org.piwigo.ui.activity;

import android.view.MenuItem;
import android.widget.ImageView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.piwigo.BuildConfig;
import org.piwigo.R;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDrawable;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.android.appcompat.v7.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Robolectric.setupActivity;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MainActivityTest {

    private MainActivity activity;

    @Before public void setUp() {
        activity = setupActivity(MainActivity.class);
    }

    @Test public void hasToolbar() {
        assertThat(activity.hasActionBar()).isTrue();
        assertThat(activity.hasToolbar()).isTrue();
        assertThat(activity.getSupportActionBar()).isShowing();
    }

    @Test public void hasNavigationDrawer() {
        assertThat(activity.hasDrawer()).isTrue();
    }

    @Test public void featuresSettingsVisible() {
        assertThat(findMenuItem(R.id.nav_albums)).isVisible();
        assertThat(findMenuItem(R.id.nav_upload)).isVisible();
        assertThat(findMenuItem(R.id.nav_settings)).isVisible();
        assertThat(findMenuItem(R.id.nav_add_account)).isNotVisible();
    }

    @Test public void accountsVisibleOnHeaderClick() {
        activity.findViewById(R.id.drawer_header).performClick();

        assertThat(findMenuItem(R.id.nav_albums)).isNotVisible();
        assertThat(findMenuItem(R.id.nav_upload)).isNotVisible();
        assertThat(findMenuItem(R.id.nav_settings)).isNotVisible();
        assertThat(findMenuItem(R.id.nav_add_account)).isVisible();
    }

    @Test public void headerHasDownArrow() {
        int id = getResourceId(activity.headerBinding.arrow);
        assertThat(id).isEqualTo(R.drawable.ic_action_arrow_drop_down);
    }

    @Test public void toggleArrowOnHeaderClick() {
        activity.findViewById(R.id.drawer_header).performClick();

        int id = getResourceId(activity.headerBinding.arrow);
        assertThat(id).isEqualTo(R.drawable.ic_action_arrow_drop_up);
    }

    private MenuItem findMenuItem(int id) {
        return activity.getNavigationView().getMenu().findItem(id);
    }

    private int getResourceId(ImageView imageView) {
        ShadowDrawable shadowDrawable = Shadows.shadowOf(imageView.getDrawable());
        return shadowDrawable.getCreatedFromResId();
    }

}