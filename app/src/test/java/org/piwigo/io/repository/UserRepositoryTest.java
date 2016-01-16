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

package org.piwigo.io.repository;

import org.junit.Before;
import org.junit.Test;
import org.piwigo.PiwigoApplication;
import org.piwigo.internal.di.component.DaggerTestApplicationComponent;
import org.piwigo.internal.di.component.TestApplicationComponent;
import org.piwigo.internal.di.module.TestApplicationModule;
import org.piwigo.io.MockRestService;
import org.piwigo.io.SessionManager;
import org.piwigo.io.model.response.StatusResponse;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class UserRepositoryTest {

    @Inject SessionManager sessionManager;
    @Inject UserRepository userRepository;

    @Before public void setUp() {
        PiwigoApplication application = mock(PiwigoApplication.class);
        TestApplicationComponent applicationComponent = DaggerTestApplicationComponent.builder()
                .testApplicationModule(new TestApplicationModule(application))
                .build();
        applicationComponent.inject(this);
    }

    @Test public void shouldLogin() {
        StatusResponse statusResponse = userRepository
                .login("http://demo.piwigo.org", "test", "test")
                .toBlocking()
                .first();

        assertThat(statusResponse.stat).isEqualTo(MockRestService.STATUS_OK);
        assertThat(statusResponse.result.pwgToken).isEqualTo(MockRestService.TOKEN);
        assertThat(sessionManager.getCookie()).isEqualTo(MockRestService.COOKIE_PWG_ID);
    }

    @Test(expected = Throwable.class) public void shouldThrowOnFailure() {
        userRepository
                .login("http://demo.piwigo.org", "bad", "bad")
                .toBlocking()
                .first();
    }

}