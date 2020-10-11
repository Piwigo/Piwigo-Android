/*
 * Piwigo for Android
 * Copyright (C) 2020 Stephan Kulow <stephan@kulow.org>
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

package org.piwigo;

import android.util.Log;

import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;

/*

 Idle resources are a rather new testing pattern on android. With this glue code within
 the application code, you tell the test framework when the application is busy and testing
 shouldn't proceed. This avoids explicit waits or random sleeps in testing code.

 The main entry point to the general idea is
 https://developer.android.com/training/testing/espresso/idling-resource

 For piwigo-android we went with a rather minimal approach by creating tags specifying the
 state. When we e.g. load an album we go with moreBusy('load album') and when we know it's
 done, we say lessBusy('load album'). If we have to load thumbnails while doing so, we just
 use moreBusy and lessBusy with that tag. This way we do not have one global state to worry
 about and if something goes wrong (either by never becoming idle [too few lessBusy calls]
 or by becoming less than idle [too many lessBusy calls]) we can see in the debug log which
 tag was used. The lessBusy calls also have a reason argument, so we can differ between the
 different paths the background job could take.

 Note that this code - while compiled into the production apk - does basically nothing
 out of testing, that extra dependency is the price you pay for workaround free test code.

 If you feel unsure when to use it, be assured that in general you can't use it too much
 if you are certain about the ways a background job can end. The worst that can happen that
 you forget to add a 'lessBusy' call to onError.

*/
public class EspressoIdlingResource {
    private static final String RESOURCE = "GLOBAL";

    private static CountingIdlingResource mCountingIdlingResource =
            new CountingIdlingResource(RESOURCE, true);

    public static void moreBusy(String name) {
        Log.d("EspressoIdlingResource", "More Busy!! " + name);
        mCountingIdlingResource.increment();
    }

    public static void lessBusy(String name, String reason) {
        Log.d("EspressoIdlingResource", "Less Busy!! " + name + " - " + reason);
        mCountingIdlingResource.decrement();
    }

    public static IdlingResource getIdlingResource() {
        return mCountingIdlingResource;
    }

}
