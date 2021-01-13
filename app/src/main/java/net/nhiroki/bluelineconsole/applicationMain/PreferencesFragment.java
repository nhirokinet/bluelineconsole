package net.nhiroki.bluelineconsole.applicationMain;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.util.Pair;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.commandSearchers.lib.StringMatchStrategy;
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

        List<Pair<String, String>> searchEngines = (new WebSearchEnginesDatabase(this.getActivity())).getEngineConfigList();

        CharSequence[] search_engine_entries = new CharSequence[searchEngines.size() + 1];
        search_engine_entries[0] = getString(R.string.pref_default_search_none);

        CharSequence[] search_engine_entry_values = new CharSequence[searchEngines.size() + 1];
        search_engine_entry_values[0] = "none";

        int searchEnginePos = 1;

        for (Pair<String, String> e: searchEngines) {
            search_engine_entry_values[searchEnginePos] = e.first;
            search_engine_entries[searchEnginePos] = e.second;
            ++searchEnginePos;
        }

        ((ListPreference) findPreference(WebSearchEnginesDatabase.PREF_NAME)).setEntries(search_engine_entries);
        ((ListPreference) findPreference(WebSearchEnginesDatabase.PREF_NAME)).setEntryValues(search_engine_entry_values);

        int stringMatchStrategySize = StringMatchStrategy.STRATEGY_LIST.length;

        CharSequence[]  string_match_strategy_entries = new CharSequence[stringMatchStrategySize];
        CharSequence[]  string_match_strategy_entry_values = new CharSequence[stringMatchStrategySize];

        for (int i = 0; i < stringMatchStrategySize; ++i) {
            string_match_strategy_entries[i] = StringMatchStrategy.getStrategyName(this.getActivity(), StringMatchStrategy.STRATEGY_LIST[i]);
            string_match_strategy_entry_values[i] = StringMatchStrategy.getStrategyPrefValue(StringMatchStrategy.STRATEGY_LIST[i]);
        }

        ((ListPreference) findPreference(StringMatchStrategy.PREF_NAME)).setEntries(string_match_strategy_entries);
        ((ListPreference) findPreference(StringMatchStrategy.PREF_NAME)).setEntryValues(string_match_strategy_entry_values);
    }


}
