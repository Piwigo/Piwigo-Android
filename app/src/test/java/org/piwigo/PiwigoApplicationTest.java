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

package org.piwigo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.piwigo.internal.di.component.ApplicationComponent;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricDataBindingTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class PiwigoApplicationTest {

    private PiwigoApplication application;

    @Before
    public void setUp() {
        application = (PiwigoApplication) RuntimeEnvironment.application;
    }

    @Test
    public void initialiseInjector() {
        assertThat(application.getApplicationComponent(), instanceOf(ApplicationComponent.class));
    }

}