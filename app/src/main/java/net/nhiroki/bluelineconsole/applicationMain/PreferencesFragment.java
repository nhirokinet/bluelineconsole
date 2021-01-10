package net.nhiroki.bluelineconsole.applicationMain;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.util.Pair;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.commands.urls.WebSearchEngine;
import net.nhiroki.bluelineconsole.commands.urls.WebSearchEnginesDatabase;

import java.util.List;

public class PreferencesFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();

        WebSearchEnginesDatabase db = new WebSearchEnginesDatabase(this.getActivity());

        List<Pair<String, String>> searchEngines = db.getEngineConfigList();

        CharSequence[] entries = new CharSequence[searchEngines.size() + 1];
        entries[0] = getString(R.string.pref_default_search_none);

        CharSequence[] entry_values = new CharSequence[searchEngines.size() + 1];
        entry_values[0] = "none";

        int i = 1;

        for (Pair<String, String> e: searchEngines) {
            entry_values[i] = e.first;
            entries[i] = e.second;
            ++i;
        }

        ((ListPreference) findPreference("pref_default_search")).setEntries(entries);
        ((ListPreference) findPreference("pref_default_search")).setEntryValues(entry_values);

    }
}
