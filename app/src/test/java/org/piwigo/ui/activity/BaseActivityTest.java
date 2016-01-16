/*
 * Copyright 2016 Phil Bayfield https://philio.me
 * Copyright 2016 Piwigo Team http://piwigo.org
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.piwigo.PiwigoRobolectricTestRunner;
import org.piwigo.internal.di.component.ActivityComponent;
import org.robolectric.Robolectric;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(PiwigoRobolectricTestRunner.class)
public class BaseActivityTest {

    private BaseActivity baseActivity;

    @Before public void setUp() {
        baseActivity = Robolectric.setupActivity(TestBaseActivity.class);
    }

    @Test public void shouldInitialiseInjector() {
        assertThat(baseActivity.getActivityComponent()).isInstanceOf(ActivityComponent.class);
    }

    public static class TestBaseActivity extends BaseActivity {
    }

}