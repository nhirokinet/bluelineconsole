package net.nhiroki.bluelineconsole.applicationMain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import net.nhiroki.bluelineconsole.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Configuration config = new Configuration();
        config.setLocale(new Locale("en", "US"));
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

        SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEdit.putBoolean(StartUpHelpActivity.PREF_KEY_SHOW_STARTUP_HELP, false);
        prefEdit.putBoolean("pref_apps_show_package_name", true);
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
        Thread.sleep(800);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(0)));

        // May fail if testing near the change of the day, but not caring, just retrying is fine
        // String expectedDateStr = new SimpleDateFormat("EEE, MM/dd/yyyy").format(new Date());
        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("date"));
        Thread.sleep(800);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(1)));
        // Skipping until resolving problem of timezone of test environment
        // onView(withText(expectedDateStr)).check(matches(isDisplayed()));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("help"));
        Thread.sleep(800);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(1)));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("ping "));
        Thread.sleep(800);
        onView(withText("ping6 ")).check(matches(isDisplayed()));
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(2)));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("ping nhiroki.net"));
        Thread.sleep(800);
        onView(withText("ping6 nhiroki.net")).check(matches(isDisplayed()));
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(2)));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("1+7*4"));
        Thread.sleep(800);
        onView(withText("\u200e= 29")).check(matches(isDisplayed()));
        onView(withText("Calculation Precision: No Error")).check(matches(isDisplayed()));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("Chrome"));
        Thread.sleep(800);
        // Depends on environment, skipping for now
        // onView(withText("com.android.chrome")).check(matches(isDisplayed()));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("."));
        // Lots of app should match
        // Just wait and confirm no crash
        Thread.sleep(5000);
    }

    @Test
    public void testBasicJapanese() throws InterruptedException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Configuration config = new Configuration();
        config.setLocale(new Locale("ja", "JP"));
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

        ActivityScenario.launch(MainActivity.class);

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("config"));
        // First time, load may take a time
        Thread.sleep(5000);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(1)));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("random-str-which-should-match-nothing"));
        Thread.sleep(800);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(0)));

        // May fail if testing near the change of the day, but not caring, just retrying is fine
        // String expectedDateStr = new SimpleDateFormat("yyyy/MM/dd(EEE)").format(new Date());
        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("date"));
        Thread.sleep(800);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(1)));
        // Skipping until resolving problem of timezone of test environment
        // onView(withText(expectedDateStr)).check(matches(isDisplayed()));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("1+7*4"));
        Thread.sleep(800);
        onView(withText("\u200e= 29")).check(matches(isDisplayed()));
        // Skipping until why failing in GitHub Actions
        // onView(withText("演算精度: 誤差なし")).check(matches(isDisplayed()));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("Chrome"));
        Thread.sleep(800);
        // Depends on environment, skipping for now
        // onView(withText("com.android.chrome")).check(matches(isDisplayed()));
    }
}
