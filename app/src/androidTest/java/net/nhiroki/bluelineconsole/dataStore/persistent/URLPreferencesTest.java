package net.nhiroki.bluelineconsole.dataStore.persistent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.List;


@RunWith(AndroidJUnit4.class)
public class URLPreferencesTest extends AndroidTestCase {
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.setContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
        URLPreferences.destroyFilesForCleanTest(this.getContext());
    }

    @Test
    public void urlPreferencesBasicFunctionTest() throws Exception {
        URLPreferences oldVersionInstance = URLPreferences.getInstance(this.getContext());

        List<URLEntry> entries = new ArrayList<>();

        URLEntry testEntry = new URLEntry();
        testEntry.id = 0;
        testEntry.name = "test_site";
        testEntry.display_name = "Test Site";
        testEntry.url_base = "https://example.com/test-site/";
        testEntry.has_query = true;
        entries.add(testEntry);

        URLEntry testEntry2 = new URLEntry();
        testEntry2.name = "test_site2";
        testEntry2.display_name = "Test Site 2";
        testEntry2.url_base = "https://example.com/test-site-2/";
        testEntry2.has_query = false;
        entries.add(testEntry2);

        for (URLEntry e: entries) {
            oldVersionInstance.add(e);
        }

        this.entriesLoadTest(entries, this.getContext());
    }

    @Test
    public void upgradeFrom_1_2_5_Test() {
        net.nhiroki.bluelineconsole.dataStore.persistent.oldVersions.URLPreferences_1_2_5 oldVersionInstance =
                net.nhiroki.bluelineconsole.dataStore.persistent.oldVersions.URLPreferences_1_2_5.getInstance(this.getContext());

        List<URLEntry> entries = new ArrayList<>();

        URLEntry testEntry = new URLEntry();
        testEntry.id = 0;
        testEntry.name = "test_site";
        testEntry.display_name = "Test Site";
        testEntry.url_base = "https://example.com/test-site/";
        testEntry.has_query = true;
        entries.add(testEntry);

        URLEntry testEntry2 = new URLEntry();
        testEntry2.name = "test_site2";
        testEntry2.display_name = "Test Site 2";
        testEntry2.url_base = "https://example.com/test-site-2/";
        testEntry2.has_query = false;
        entries.add(testEntry2);

        for (URLEntry e: entries) {
            oldVersionInstance.add(e);
        }
        oldVersionInstance.close();

        this.entriesLoadTest(entries, this.getContext());
    }

    private static void entriesLoadTest(List<URLEntry> urlEntries, Context context) {
        URLPreferences urlPreferences = URLPreferences.getInstance(context);

        List<URLEntry> loadedData = urlPreferences.getAllEntries();
        assertEquals(urlEntries.size(), loadedData.size());

        for (int i = 0; i < urlEntries.size(); ++i) {
            assertEquals(urlEntries.get(i).name, loadedData.get(i).name);
            assertEquals(urlEntries.get(i).display_name, loadedData.get(i).display_name);
            assertEquals(urlEntries.get(i).url_base, loadedData.get(i).url_base);
            assertEquals(urlEntries.get(i).has_query, loadedData.get(i).has_query);
        }
    }
}
