package org.piwigo.helper;

import android.support.v4.util.ArrayMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.piwigo.BuildConfig;
import org.piwigo.RobolectricDataBindingTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import retrofit.client.Header;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(RobolectricDataBindingTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CookieHelperTest {

    List<Header> headers = new ArrayList<>();

    @Before
    public void setUp() {
        headers.add(new Header("Not-Cookie", "aValue"));
        headers.add(new Header("Set-Cookie", "pwg_id=asdfghjklqwertyuiop"));
        headers.add(new Header("Also-Not-Cookie", "anotherValue"));
        headers.add(new Header("Set-Cookie", "something=else"));
    }

    @Test
    public void extractAll() {
        ArrayMap<String, String> cookies = CookieHelper.extractAll(headers);

        assertThat(cookies, hasEntry(equalTo("pwg_id"), equalTo("asdfghjklqwertyuiop")));
        assertThat(cookies, hasEntry(equalTo("something"), equalTo("else")));
    }

    @Test
    public void extract() {
        String value = CookieHelper.extract("pwg_id", headers);

        assertThat(value, is("asdfghjklqwertyuiop"));
    }

}