package org.piwigo;

import android.util.Log;

import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;

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
