package net.nhiroki.bluelineconsole.applicationMain;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.commandSearchers.lib.StringMatchStrategy;
import net.nhiroki.bluelineconsole.commands.urls.WebSearchEnginesDatabase;

import java.util.List;

import static android.provider.Settings.ACTION_VOICE_INPUT_SETTINGS;

public class PreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();

        List<Pair<String, String>> searchEngines = (new WebSearchEnginesDatabase(this.getActivity())).getEngineConfigList();

        CharSequence[] search_engine_entries = new CharSequence[searchEngines.size() + 1];
        search_engine_entries[0] = getString(R.string.preferences_item_default_search_option_none);

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

        findPreference("pref_default_assist_app").setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // Not a perfect behavior, main window disappears
                        // This config is not to be used everyday, it is enough if just not too confusing
                        ((PreferencesActivity)PreferencesFragment.this.getActivity()).setComingBackFlag();
                        Intent intent = new Intent(ACTION_VOICE_INPUT_SETTINGS);
                        PreferencesFragment.this.startActivity(intent);
                        return true;
                    }
                }
        );

        ((ListPreference) findPreference(BaseWindowActivity.PREF_NAME_THEME)).setEntries(BaseWindowActivity.getPrefThemeEntries(this.getContext()));
        ((ListPreference) findPreference(BaseWindowActivity.PREF_NAME_THEME)).setEntryValues(BaseWindowActivity.PREF_THEME_ENTRY_VALUES);

        findPreference(BaseWindowActivity.PREF_NAME_THEME).setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        PreferencesFragment.this.getActivity().finish();
                        return true;
                    }
                }
        );
    }
}
