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
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class PiwigoApplicationTest {

    private PiwigoApplication application;

    @Before
    public void setUp() {
        application = (PiwigoApplication) RuntimeEnvironment.application;
    }

    @Test
    public void shouldInitialiseInjector() {
        assertThat(application.getApplicationComponent(), instanceOf(ApplicationComponent.class));
    }

}