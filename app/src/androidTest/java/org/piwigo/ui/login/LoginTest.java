package org.piwigo.ui.login;

import android.os.Build;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.piwigo.EspressoIdlingResource;
import org.piwigo.R;
import org.piwigo.ui.account.ManageAccountsActivity;

import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.IdlingRegistry;
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

    // Register your Idling Resource before any tests regarding this component
    @Before
    public void registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void loginTwoAccounts() {

        addAccount("https://tg1.kulow.org", "seessmall", "seessmall");

        // make sure we only see the galleries we are supposed to
        onView(withText("Small")).check(matches(isDisplayed()));
        onView(withText("Large")).check(doesNotExist());

        manageAccounts();

        ViewInteraction addAccount = onView(allOf(withId(R.id.action_add_account), isDisplayed()));
        addAccount.perform(click());

        addAccount("https://tg1.kulow.org", "seeslarge", "seeslarge");

        // only sees large (and public)
        onView(withText("Large")).check(matches(isDisplayed()));
        onView(withText("Small")).check(doesNotExist());
    }

    @Test
    public void backFromLoginClosesApp() {
        pressBackUnconditionally();
        assertEquals(Lifecycle.State.DESTROYED, activityScenarioRule.getScenario().getState());
    }

    @Test
    public void backBringsUsToAccountManager() {
        addAccount("https://tg1.kulow.org", "seessmall", "seessmall");
        manageAccounts();

        ViewInteraction addAccount = onView(allOf(withId(R.id.action_add_account), isDisplayed()));
        addAccount.perform(click());

        pressBackUnconditionally();
        onView(withText("Manage Accounts")).check(matches(isDisplayed()));
    }

    @Test
    public void invalidPasswordFails() {
        addAccount("https://tg1.kulow.org", "seessmall", "invalid");
        onView(withId(R.id.snackbar_text)).check(matches(withText("Login failed for given username 'seessmall'")));
    }

    @Test
    public void urlIsExpanded() {
        addAccount("tg1.kulow.org", "seessmall", "invalid");
        onView(withId(R.id.url)).check(matches(withText("https://tg1.kulow.org/")));
    }

    @Test
    public void httpsUrlShowsRedirect() throws InterruptedException {
        addAccount("tg1.kulow.org:81", "", "");
        onView(withId(R.id.snackbar_text)).check(matches(withText("Encrypted connection (SSL) to 'tg1.kulow.org' cannot be established")));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
          // snackbar actions are not supported in 5.0
          return;
        }
        onView(allOf(withText("Use insecure http?"), isDisplayed())).perform((click()));
        // we're in
        onView(withText("Public")).check(matches(isDisplayed()));
    }

    @Test
    public void changeGuestToLogin() {
        addAccount("https://tg1.kulow.org", "", "");
        onView(withText("Public")).check(matches(isDisplayed()));
        manageAccounts();

        onView(allOf(withContentDescription("More options"), isDisplayed())).perform(click());
        onView(allOf(withText("Edit account"), isDisplayed())).perform(click());

        // only editing here
        addAccount("https://tg1.kulow.org", "seessmall", "seessmall");
        onView(withText("Small")).check(matches(isDisplayed()));
    }

    private void addAccount(String url, String user, String password) {
        ViewInteraction editUser = onView(allOf(withId(R.id.username), isDisplayed()));
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

    @After
    public void tearDown() {
        // do not use manageAccounts here as we do not know where we are
        activityScenarioRule.getScenario().launch(ManageAccountsActivity.class);

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

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
   }
}
