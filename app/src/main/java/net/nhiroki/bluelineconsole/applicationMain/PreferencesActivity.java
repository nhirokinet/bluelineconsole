package net.nhiroki.bluelineconsole.applicationMain;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nhiroki.bluelineconsole.R;

public class PreferencesActivity extends BaseWindowActivity {
    public PreferencesActivity() {
        super(R.layout.preferences_activity_body);
    }

    @Override
    public void onCreate(Bundle savedInstanceStates) {
        super.onCreate(savedInstanceStates);

        ((TextView) findViewById(R.id.baseWindowMainHeaderTextView)).setText(getString(R.string.preference_title_for_header_and_footer));
        ((TextView) findViewById(R.id.baseWindowMainFooterTextView)).setText(getString(R.string.preference_title_for_header_and_footer));

        ((LinearLayout)findViewById(R.id.baseWindowRootLinearLayout)).setGravity(Gravity.CENTER_VERTICAL);

        PreferencesFragment preferenceFragment = new PreferencesFragmentWithOnChangeListener();
        PreferencesActivity.this.getFragmentManager().beginTransaction().replace(R.id.main_preference_fragment, preferenceFragment).commit();

        findViewById(R.id.baseWindowRootLinearLayout).setPadding(
                (int)(8 * getResources().getDisplayMetrics().density),
                (int)(24 * getResources().getDisplayMetrics().density),
                (int)(8 * getResources().getDisplayMetrics().density),
                (int)(24 * getResources().getDisplayMetrics().density));

        setResult(RESULT_OK, new Intent(this, MainActivity.class));

        this.changeElementSize(false);
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
        setResult(RESULT_CANCELED, new Intent(this, MainActivity.class));
        super.onUserLeaveHint();
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        this.changeElementSize(hasFocus);
    }

    private void changeElementSize(boolean visible) {
        LinearLayout mainLL = findViewById(R.id.preference_activity_fragment_wrapper);
        LinearLayout.LayoutParams mainLP = (LinearLayout.LayoutParams) mainLL.getLayoutParams();
        View centerLL = findViewById(R.id.baseWindowMainLinearLayout);
        View centerLLOuter = findViewById(R.id.baseWindowMainLinearLayoutOuter);

        if (visible) {
            mainLP.width = LinearLayout.LayoutParams.MATCH_PARENT;
            mainLP.height = LinearLayout.LayoutParams.MATCH_PARENT;
            mainLL.setLayoutParams(mainLP);

            LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
            centerLP.height = LinearLayout.LayoutParams.MATCH_PARENT;
            centerLL.setLayoutParams(centerLP);

            LinearLayout.LayoutParams centerLPOuter = (LinearLayout.LayoutParams) centerLLOuter.getLayoutParams();
            centerLPOuter.height = LinearLayout.LayoutParams.MATCH_PARENT;
            centerLLOuter.setLayoutParams(centerLPOuter);

            ((LinearLayout)findViewById(R.id.baseWindowRootLinearLayout)).setGravity(Gravity.TOP);

        } else {
            mainLP.width = (int) (200 * getResources().getDisplayMetrics().density + 0.5);
            mainLP.height = 0;
            mainLL.setLayoutParams(mainLP);

            LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
            centerLP.height = 0;
            centerLL.setLayoutParams(centerLP);

            LinearLayout.LayoutParams centerLPOuter = (LinearLayout.LayoutParams) centerLLOuter.getLayoutParams();
            centerLPOuter.height = 0;
            centerLLOuter.setLayoutParams(centerLPOuter);

            ((LinearLayout)findViewById(R.id.baseWindowRootLinearLayout)).setGravity(Gravity.CENTER_VERTICAL);
        }
    }
}
