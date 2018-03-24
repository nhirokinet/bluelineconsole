package net.nhiroki.bluelineconsole.applicationMain;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import net.nhiroki.bluelineconsole.R;

public class PreferencesFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
