package net.nhiroki.bluelineconsole.applicationMain;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;

import androidx.fragment.app.Fragment;

import net.nhiroki.bluelineconsole.R;

public class PreferencesActivity extends BaseWindowActivity {
    public PreferencesActivity() {
        super(R.layout.preferences_activity_body, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceStates) {
        super.onCreate(savedInstanceStates);

        this.setHeaderFooterTexts(getString(R.string.preferences_title_for_header_and_footer), null);
        this.setWindowBoundarySize(ROOT_WINDOW_FULL_WIDTH_IN_MOBILE, 1);

        this.setWindowLocationGravity(Gravity.CENTER_VERTICAL);

        Fragment preferenceFragment = new PreferencesFragmentWithOnChangeListener();
        PreferencesActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.main_preference_fragment, preferenceFragment).commit();

        setResult(RESULT_OK, new Intent(this, MainActivity.class));

        this.changeBaseWindowElementSizeForAnimation(false);
        this.enableBaseWindowAnimation();
    }

    public static class PreferencesFragmentWithOnChangeListener extends PreferencesFragment {
        SharedPreferences.OnSharedPreferenceChangeListener preferenceChangedListener;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            preferenceChangedListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals("pref_main_always_show_notification")) {
                        AppNotification.update(PreferencesFragmentWithOnChangeListener.this.getActivity());
                    }
                }
            };
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangedListener);
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangedListener);
            super.onPause();
        }
    }

    @Override
    public void onUserLeaveHint() {
        setResult(RESULT_OK, new Intent(this, MainActivity.class));
        super.onUserLeaveHint();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        this.changeBaseWindowElementSizeForAnimation(true);
    }
}
