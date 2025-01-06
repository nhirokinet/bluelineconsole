package net.nhiroki.bluelineconsole.applicationMain;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PreferencesActivityTest {
    @Test
    public void testBasic() throws InterruptedException {
        // Currently just test no crash due to library problem
        ActivityScenario<PreferencesActivity> activity = ActivityScenario.launch(PreferencesActivity.class);
        Thread.sleep(5000);
    }
}
