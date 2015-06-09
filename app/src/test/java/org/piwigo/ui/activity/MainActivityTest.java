package org.piwigo.ui.activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.piwigo.BuildConfig;
import org.piwigo.RobolectricDataBindingTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.robolectric.Robolectric.setupActivity;

@RunWith(RobolectricDataBindingTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class MainActivityTest {

    private MainActivity activity;

    @Before
    public void setUp() {
        activity = setupActivity(MainActivity.class);
    }

    // TODO a dummy test to prevent no tests found error
    @Test
    public void dummyTest() {
        assertThat(true, is(true));
    }

}