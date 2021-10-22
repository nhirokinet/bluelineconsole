package net.nhiroki.bluelineconsole.commandSearchers;

import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import androidx.preference.PreferenceManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import net.nhiroki.bluelineconsole.commands.urls.WebSearchEnginesDatabase;
import net.nhiroki.bluelineconsole.dataStore.persistent.URLEntry;
import net.nhiroki.bluelineconsole.dataStore.persistent.URLPreferences;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SearchEngineCommandSearcherTest extends AndroidTestCase {
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.setContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @Test
    public void commandSearchAggregatorLeastBasicFunctionTest() {
        this.setUpBasicWebSettings();

        SearchEngineCommandSearcher searchEngineCommandSearcher = new SearchEngineCommandSearcher(this.getContext());
        searchEngineCommandSearcher.waitUntilPrepared();

        {
            List<CandidateEntry> candidateEntryList = searchEngineCommandSearcher.searchCandidateEntries("test_site_for_test", this.getContext());
            assertEquals(1, candidateEntryList.size());
            assertTrue(candidateEntryList.get(0).getTitle().indexOf("Test Site 1") != -1);
        }

        {
            List<CandidateEntry> candidateEntryList = searchEngineCommandSearcher.searchCandidateEntries("test_site", this.getContext());
            assertEquals(2, candidateEntryList.size());
            int testSite1Count = 0;
            int testSite2Count = 0;

            for (CandidateEntry entry : candidateEntryList) {
                if (entry.getTitle().indexOf("Test Site 1") != -1) {
                    testSite1Count += 1;
                }
                if (entry.getTitle().indexOf("Test Site 2") != -1) {
                    testSite2Count += 1;
                }
            }
            assertEquals(1, testSite1Count);
            assertEquals(1, testSite2Count);
        }

        {
            List<CandidateEntry> candidateEntryList = searchEngineCommandSearcher.searchCandidateEntries("test_site queryabc", this.getContext());
            assertEquals(1, candidateEntryList.size());
            assertTrue(candidateEntryList.get(0).getTitle().indexOf("Test Site 3") != -1);
            assertTrue(candidateEntryList.get(0).getTitle().indexOf("queryabc") != -1);
        }
    }

    private void setUpBasicWebSettings() {
        SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(this.getContext()).edit();
        prefEdit.remove(WebSearchEnginesDatabase.PREF_KEY_DISABLED_URLS);
        prefEdit.remove(WebSearchEnginesDatabase.PREF_KEY_DEFAULT_SEARCH);
        prefEdit.apply();

        URLPreferences.destroyFilesForCleanTest(this.getContext());

        URLPreferences urlPreferences = URLPreferences.getInstance(this.getContext());

        List<URLEntry> entries = new ArrayList<>();

        URLEntry testEntry = new URLEntry();
        testEntry.id = 0;
        testEntry.name = "test_site_for_test";
        testEntry.display_name = "Test Site 1";
        testEntry.url_base = "https://example.com/test-site/";
        testEntry.has_query = false;
        entries.add(testEntry);

        URLEntry testEntry2 = new URLEntry();
        testEntry2.name = "test_site2";
        testEntry2.display_name = "Test Site 2";
        testEntry2.url_base = "https://example.com/test-site-2/";
        testEntry2.has_query = false;
        entries.add(testEntry2);

        URLEntry testEntry3 = new URLEntry();
        testEntry3.name = "test_site3";
        testEntry3.display_name = "Test Site 3";
        testEntry3.url_base = "https://example.com/test-site-3/";
        testEntry3.has_query = true;
        entries.add(testEntry3);

        for (URLEntry e: entries) {
            urlPreferences.add(e);
        }

        urlPreferences.close();
    }
}