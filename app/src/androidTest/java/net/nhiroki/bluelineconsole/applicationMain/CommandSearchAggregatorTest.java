package net.nhiroki.bluelineconsole.applicationMain;


import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import androidx.preference.PreferenceManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import net.nhiroki.bluelineconsole.commandSearchers.CommandSearchAggregator;
import net.nhiroki.bluelineconsole.commandSearchers.eachSearcher.ContactSearchCommandSearcher;
import net.nhiroki.bluelineconsole.commands.urls.WebSearchEnginesDatabase;
import net.nhiroki.bluelineconsole.dataStore.cache.ApplicationInformationCache;
import net.nhiroki.bluelineconsole.dataStore.persistent.URLEntry;
import net.nhiroki.bluelineconsole.dataStore.persistent.URLPreferences;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class CommandSearchAggregatorTest extends AndroidTestCase {
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.setContext(InstrumentationRegistry.getInstrumentation().getTargetContext());

        SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(this.getContext()).edit();
        prefEdit.putBoolean(ContactSearchCommandSearcher.PREF_CONTACT_SEARCH_ENABLED_KEY, false);
        prefEdit.apply();

        ApplicationInformationCache.destroyFilesForCleanTest(this.getContext());
    }

    @Test
    public void commandSearchAggregatorLeastBasicFunctionTest() {
        this.setUpWebSettings();

        CommandSearchAggregator commandSearchAggregator = new CommandSearchAggregator(this.getContext());
        commandSearchAggregator.waitUntilPrepared();

        {
            List<CandidateEntry> candidateEntryList = commandSearchAggregator.searchCandidateEntries("test_sit queryabc", this.getContext());
            assertEquals(1, candidateEntryList.size());
            assertTrue(candidateEntryList.get(0).getTitle().contains("Test Site 3"));
            assertTrue(candidateEntryList.get(0).getTitle().contains("queryabc"));
        }

        commandSearchAggregator.close();
    }

    private void setUpWebSettings() {
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
