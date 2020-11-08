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

package org.piwigo.ui.main;


import android.os.RemoteException;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.piwigo.EspressoIdlingResource;
import org.piwigo.R;
import org.piwigo.ui.account.ManageAccountsActivity;
import org.piwigo.ui.login.LoginActivity;

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
    public ActivityScenarioRule<LoginActivity> activityScenarioRule =
            new ActivityScenarioRule<LoginActivity>(LoginActivity.class);

    // Register your Idling Resource before any tests regarding this component
    @Before
    public void registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void fullScreenTest() throws RemoteException {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        addAccount("https://tg1.kulow.org", "seessmall", "seessmall");
        onView(withText("Small")).check(matches(isDisplayed()));

        // enter the album
        onView(withText("Small")).perform((click()));

        // switch to full screen
        onView(withContentDescription("caption1")).perform((click()));

        device.setOrientationLeft();
        device.setOrientationNatural();
        device.setOrientationRight();
    }

    private void addAccount(String url, String user, String password) {
        ViewInteraction editUser = onView(withId(R.id.username));
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
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        try {
            device.setOrientationNatural();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        activityScenarioRule.getScenario().launch(ManageAccountsActivity.class);
        onView(allOf(withContentDescription("More options"), isDisplayed())).perform(click());
        ViewInteraction viewInteraction = onView(allOf(withText("Remove account"), isDisplayed()));
        viewInteraction.check(matches(isDisplayed()));
        viewInteraction.perform(click());

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

}
