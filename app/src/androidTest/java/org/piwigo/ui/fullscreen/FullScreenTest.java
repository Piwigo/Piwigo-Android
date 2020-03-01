/*
 * Piwigo for Android
 * Copyright (C) 2016-2020 Piwigo Team http://piwigo.org
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

package org.piwigo.ui.fullscreen;


import android.content.pm.ActivityInfo;
import android.os.RemoteException;
import android.view.View;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.piwigo.R;
import org.piwigo.ui.account.ManageAccountsActivity;
import org.piwigo.ui.main.MainActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FullScreenTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void fullScreenTest() {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        try {
            device.setOrientationNatural();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        addAccount("https://tg1.kulow.org", "seessmall", "seessmall");
        waitForElement(R.id.albumRecycler, 8000);
        sleepUninterrupted(3000);
        // enter the album
        onView(withText("Small")).perform((click()));
        waitForElement(R.id.photoRecycler, 8000);

        onView(withContentDescription("caption1")).perform((click()));

        waitForElement(R.id.imgDisplay, 5000);

        try {
            device.setOrientationLeft();
            sleepUninterrupted(3000);
            device.setOrientationNatural();
            sleepUninterrupted(2000);
            device.setOrientationRight();
            sleepUninterrupted(1000);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sleepUninterrupted(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {

        }
    }

    public ViewInteraction waitForElement(final int viewId, final long millis) {

        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + millis;
        final Matcher<View> viewMatcher = CoreMatchers.allOf(withId(viewId), isDisplayed());

        do {
            ViewInteraction va = onView(viewMatcher);
            try {
                va.check(matches(isDisplayed()));
                return va;
            } catch (NoMatchingViewException e) {
            }
            sleepUninterrupted((100));
        }
        while (System.currentTimeMillis() < endTime);

        ViewInteraction va = onView(viewMatcher);
        // raise the exception
        va.check(matches(isDisplayed()));
        // return it in cases it just appeared the very moment
        return va;
    }

    private void addAccount(String url, String user, String password) {
        ViewInteraction editUser = waitForElement(R.id.username, 5000);
        editUser.check(matches(withText("")));

        ViewInteraction editURL = onView(withId(R.id.url));
        editURL.perform(replaceText(url));

        editUser.perform(replaceText(user));
        editUser.perform(pressImeActionButton());

        ViewInteraction editPassword = onView(withId(R.id.password));
        editPassword.perform(replaceText(password));
        editPassword.perform(pressImeActionButton());

        ViewInteraction loginButton = onView(withId(R.id.login_button));
        loginButton.perform(scrollTo(), click());
    }
    @After
    public void tearDown() {
        // do not use manageAccounts here as we do not know where we are
        activityScenarioRule.getScenario().launch(ManageAccountsActivity.class);
        waitForElement(R.id.account_recycler, 3000);

        onView(allOf(withContentDescription("More options"), isDisplayed())).perform(click());
        ViewInteraction viewInteraction = onView(allOf(withText("Remove account"), isDisplayed()));
        viewInteraction.check(matches(isDisplayed()));
        viewInteraction.perform(click());

        // maximum 2 accounts
        try {
            onView(allOf(withContentDescription("More options"), isDisplayed())).perform(click());
            onView(allOf(withText("Remove account"), isDisplayed())).perform(click());
        } catch (NoMatchingViewException e) {
            // no crash teardown if there were less accounts
        }
    }

}
