package net.nhiroki.bluelineconsole.applicationMain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import net.nhiroki.bluelineconsole.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Before
    public void setUp() {
        SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getInstrumentation().getTargetContext()).edit();
        prefEdit.putBoolean(StartUpHelpActivity.PREF_KEY_SHOW_STARTUP_HELP, false);
        prefEdit.apply();
    }

    @Test
    public void testBasic() throws InterruptedException {
        ActivityScenario.launch(MainActivity.class);

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("config"));
        // First time, load may take a time
        Thread.sleep(5000);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(1)));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("random-str-which-should-match-nothing"));
        Thread.sleep(200);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(0)));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("date"));
        Thread.sleep(200);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(1)));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("help"));
        Thread.sleep(200);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(1)));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("ping "));
        Thread.sleep(200);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(2)));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("."));
        // Lots of app should match
        // Just wait and confirm no crash
        Thread.sleep(5000);
    }
}
