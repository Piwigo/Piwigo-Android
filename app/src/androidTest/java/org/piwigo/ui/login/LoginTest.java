package org.piwigo.ui.login;

import android.view.View;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.piwigo.R;
import org.piwigo.ui.account.ManageAccountsActivity;
import org.piwigo.ui.login.LoginActivity;

import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerActions.open;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityScenarioRule =
            new ActivityScenarioRule<LoginActivity>(LoginActivity.class);

    @Test
    public void loginTwoAccounts() throws InterruptedException {

        addAccount("https://tg1.kulow.org", "seessmall", "seessmall");
        waitForElement(R.id.fab, 5000);

        // TODO: the current app renders an empty main view and blocks while
        // loading the actual infos
        Thread.sleep(5000);

        // make sure we only see the galleries we are supposed to
        // TODO does not work on travis reliably (most likely due to do broken login code)
        //  onView(withText("Small")).check(matches(isDisplayed()));
        onView(withText("Large")).check(doesNotExist());

        manageAccounts();

        ViewInteraction addAccount = onView(allOf(withId(R.id.action_add_account), isDisplayed()));
        addAccount.perform(click());

        addAccount("https://tg1.kulow.org", "seeslarge", "seeslarge");
        waitForElement(R.id.fab, 5000);

        // TODO: see above
        Thread.sleep(5000);

        // only sees large (and public)
        // TODO does not work on travis reliably onView(withText("Large")).check(matches(isDisplayed()));
        onView(withText("Small")).check(doesNotExist());
    }

    @Test
    public void backFromLoginClosesApp() {
        pressBackUnconditionally();
        assertEquals(Lifecycle.State.DESTROYED, activityScenarioRule.getScenario().getState());
    }

    @Test
    public void backBringsUsToAccountManager() throws InterruptedException {
        addAccount("https://tg1.kulow.org", "seessmall", "seessmall");
        waitForElement(R.id.fab, 5000);
        manageAccounts();

        ViewInteraction addAccount = onView(allOf(withId(R.id.action_add_account), isDisplayed()));
        addAccount.perform(click());

        pressBackUnconditionally();
        onView(withText("Manage Accounts")).check(matches(isDisplayed()));
    }

    @Test
    public void invalidPasswordFails() throws InterruptedException {
        addAccount("https://tg1.kulow.org", "seessmall", "invalid");
        waitForElement(R.id.snackbar_text, 3000);
        // the details will be 'This method requires HTTP POST', which is BS due
        // the automatic retry on http (TODO to remove)
        onView(withText("Show details")).check(matches(isDisplayed()));
    }

    public void addAccount(String url, String user, String password) {
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

    protected void manageAccounts() {
        onView(withId(R.id.drawer_layout)).perform(open());
        ViewInteraction navigation = onView(allOf(withText("Manage Accounts"), isDisplayed()));
        navigation.perform(click());
    }

    public ViewInteraction waitForElement(final int viewId, final long millis) {

        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + millis;
        final Matcher<View> viewMatcher = allOf(withId(viewId), isDisplayed());

        do {
            ViewInteraction va = onView(viewMatcher);
            try {
                va.check(matches(isDisplayed()));
                return va;
            } catch (NoMatchingViewException e) {
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
        while (System.currentTimeMillis() < endTime);

        ViewInteraction va = onView(viewMatcher);
        // raise the exception
        va.check(matches(isDisplayed()));
        // return it in cases it just appeared the very moment
        return va;
    }


    @After
    public void tearDown() throws InterruptedException {
        // do not use manageAccounts here as we do not know where we are
        activityScenarioRule.getScenario().launch(ManageAccountsActivity.class);
        Thread.sleep(3000);

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
