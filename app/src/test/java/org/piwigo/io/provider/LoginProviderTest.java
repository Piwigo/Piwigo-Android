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

package org.piwigo.io.provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.piwigo.BuildConfig;
import org.piwigo.PiwigoApplication;
import org.piwigo.RobolectricDataBindingTestRunner;
import org.piwigo.internal.di.component.DaggerTestApplicationComponent;
import org.piwigo.internal.di.component.TestApplicationComponent;
import org.piwigo.internal.di.module.ApplicationModule;
import org.piwigo.io.response.StatusResponse;
import org.piwigo.manager.SessionManager;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import javax.inject.Inject;

import rx.observables.BlockingObservable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(RobolectricDataBindingTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class LoginProviderTest {

    @Inject SessionManager sessionManager;
    @Inject LoginProvider loginProvider;

    @Before
    public void setUp() throws Exception {
        PiwigoApplication application = (PiwigoApplication) RuntimeEnvironment.application;

        TestApplicationComponent applicationComponent = DaggerTestApplicationComponent.builder()
                .applicationModule(new ApplicationModule(application))
                .build();
        applicationComponent.inject(this);

        application.setApplicationComponent(applicationComponent);

        // Needs a valid URL
        sessionManager.setUrl("http://test.piwigo.org");
    }

    @Test
    public void loginSuccess() {
        BlockingObservable<StatusResponse> observable = loginProvider
                .username("test")
                .password("test")
                .create()
                .toBlocking();
        StatusResponse statusResponse = observable.first();

        assertThat(statusResponse.stat, is("ok"));
        assertThat(sessionManager.getCookie(), is("1234567890"));
        assertThat(statusResponse.result.pwgToken, is("abcdefghijklmnop"));
    }

    @Test(expected = Throwable.class)
    public void loginFailure() {
        loginProvider
                .username("bad")
                .password("bad")
                .create()
                .toBlocking()
                .first();
    }

}