package net.nhiroki.bluelineconsole.applicationMain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.LocaleManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.LocaleList;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import net.nhiroki.bluelineconsole.R;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    private Context context = null;

    @Before
    public void setUp() {
        this.context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager.class).setApplicationLocales(new LocaleList(new Locale("en", "US")));
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(new Locale("en", "US")));
        }
        Locale.setDefault(new Locale("en", "US"));

        SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEdit.putBoolean(StartUpHelpActivity.PREF_KEY_SHOW_STARTUP_HELP, false);
        prefEdit.putBoolean("pref_apps_show_package_name", true);
        prefEdit.apply();

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
    }

    @Test
    public void testBasic() throws InterruptedException {
        ActivityScenario<MainActivity> activity = ActivityScenario.launch(MainActivity.class);
        // Wait until ready
        Thread.sleep(2000);

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("config"));
        // First time, load may take a time
        Thread.sleep(5000);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(1)));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("random-str-which-should-match-nothing"));
        Thread.sleep(800);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(0)));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("date"));
        Thread.sleep(800);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(1)));
        // May fail if testing near the change of the day, but not caring, just retrying is fine
        String expectedDateStr = new SimpleDateFormat("EEE, MM/dd/yyyy", new Locale("en", "US")).format(new Date());
        onView(withText(expectedDateStr)).check(matches(isDisplayed()));

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
        onView(withId(R.id.mainInputText)).perform(typeText("Settings"));
        Thread.sleep(800);
        onView(withText("com.android.settings")).check(matches(isDisplayed()));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("Blue Line Console"));
        Thread.sleep(800);
        onView(withText("net.nhiroki.bluelineconsole")).check(matches(isDisplayed()));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("."));
        // Lots of app should match
        // Just wait and confirm no crash
        Thread.sleep(5000);
    }

    @Test
    public void testBasicJapanese() throws InterruptedException {
        // Looks like the way of setting language has changed in SDK24 (N).
        // Older version does not change output even if we run the following language setting code.
        // Therefore, in this case, skipping test for non-English version.
        Assume.assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager.class).setApplicationLocales(new LocaleList(new Locale("ja", "JP")));
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(new Locale("ja", "JP")));
        }
        Locale.setDefault(new Locale("ja", "JP"));

        ActivityScenario<MainActivity> activity = ActivityScenario.launch(MainActivity.class);
        // Wait until ready
        Thread.sleep(2000);

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("config"));
        // First time, load may take a time
        Thread.sleep(5000);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(1)));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("random-str-which-should-match-nothing"));
        Thread.sleep(800);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(0)));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("date"));
        Thread.sleep(800);
        onView(withId(R.id.candidateListView)).check(matches(hasChildCount(1)));
        // May fail if testing near the change of the day, but not caring, just retrying is fine
        try {
            String expectedDateStr = new SimpleDateFormat("yyyy/MM/dd(EEE)", new Locale("ja", "JP")).format(new Date());
            onView(withText(expectedDateStr)).check(matches(isDisplayed()));
        } catch (Exception e) {
            // Why? even in this setting, there looks like the case that only EEE is displayed in English in test machine (not real machine)
            String expectedDateStr = new SimpleDateFormat("yyyy/MM/dd(EEE)", new Locale("en", "US")).format(new Date());
            onView(withText(expectedDateStr)).check(matches(isDisplayed()));
        }

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("1+7*4"));
        Thread.sleep(800);
        onView(withText("\u200e= 29")).check(matches(isDisplayed()));
        onView(withText("演算精度: 誤差なし")).check(matches(isDisplayed()));

        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("Blue Line Console"));
        Thread.sleep(800);
        onView(withText("net.nhiroki.bluelineconsole")).check(matches(isDisplayed()));
        
        onView(withId(R.id.mainInputText)).perform(clearText());
        onView(withId(R.id.mainInputText)).perform(typeText("Chrome"));
        Thread.sleep(800);
        // Depends on environment, skipping for now
        // onView(withText("com.android.chrome")).check(matches(isDisplayed()));
    }

    @Test
    public void testStartUpHelp() throws InterruptedException {
        SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEdit.putBoolean(StartUpHelpActivity.PREF_KEY_SHOW_STARTUP_HELP, true);
        prefEdit.putBoolean("pref_apps_show_package_name", false);
        prefEdit.apply();

        ActivityScenario<MainActivity> activity = ActivityScenario.launch(MainActivity.class);
        Thread.sleep(800);
        onView(withId(R.id.startUpOKButton)).check(matches(isDisplayed()));
    }
}
