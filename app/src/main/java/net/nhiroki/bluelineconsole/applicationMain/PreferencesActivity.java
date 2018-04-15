package net.nhiroki.bluelineconsole.applicationMain;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.nhiroki.bluelineconsole.R;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceStates) {
        super.onCreate(savedInstanceStates);
        this.setContentView(R.layout.preferences_activity_view);

        ((LinearLayout)findViewById(R.id.mainLinearLayout)).setGravity(Gravity.CENTER_VERTICAL);

        ((ViewGroup) findViewById(R.id.mainLinearLayout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.preference_activity_fragment_wrapper)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.preference_center_linear_layout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        this.findViewById(R.id.mainLinearLayout).setOnClickListener(new ExitOnClickListener());

        PreferencesFragment preferenceFragment = new PreferencesFragmentWithOnChangeListener();
        PreferencesActivity.this.getFragmentManager().beginTransaction().replace(R.id.main_preference_fragment, preferenceFragment).commit();

        // Decrease topMargin (which is already negative) by 1 physical pixel to fill the gap. See the comment in prefecences_activity_view.xml .
        View versionOnMainFooterWrapper = findViewById(R.id.versionOnMainFooterWrapper);
        ViewGroup.MarginLayoutParams versionOnMainFooterWrapperLayoutParam = (ViewGroup.MarginLayoutParams) versionOnMainFooterWrapper.getLayoutParams();
        versionOnMainFooterWrapperLayoutParam.setMargins(
                versionOnMainFooterWrapperLayoutParam.leftMargin,
                versionOnMainFooterWrapperLayoutParam.topMargin - 1,
                versionOnMainFooterWrapperLayoutParam.rightMargin,
                versionOnMainFooterWrapperLayoutParam.bottomMargin
        );
        versionOnMainFooterWrapper.setLayoutParams(versionOnMainFooterWrapperLayoutParam);

        LinearLayout mainLL = findViewById(R.id.mainLinearLayout);
        LinearLayout.LayoutParams mainLP = (LinearLayout.LayoutParams) mainLL.getLayoutParams();
        mainLP.width =  (int) (200 * getResources().getDisplayMetrics().density + 0.5) ;
        mainLL.setLayoutParams(mainLP);

        setResult(RESULT_OK, new Intent(this, MainActivity.class));
    }

    public static class PreferencesFragmentWithOnChangeListener  extends PreferencesFragment {
        SharedPreferences.OnSharedPreferenceChangeListener preferenceChangedListener;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            preferenceChangedListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals("pref_main_always_show_notification")) {
                        AppNotification.update(PreferencesFragmentWithOnChangeListener.this.getContext());
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
        if (hasFocus) {
            LinearLayout mainLL = findViewById(R.id.mainLinearLayout);
            LinearLayout.LayoutParams mainLP = (LinearLayout.LayoutParams) mainLL.getLayoutParams();
            mainLP.width = LinearLayout.LayoutParams.MATCH_PARENT;
            mainLL.setLayoutParams(mainLP);

            LinearLayout centerLL = findViewById(R.id.preference_center_linear_layout);
            LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
            centerLP.height = LinearLayout.LayoutParams.MATCH_PARENT;
            centerLL.setLayoutParams(centerLP);

            ((LinearLayout)findViewById(R.id.mainLinearLayout)).setGravity(Gravity.TOP);
        } else {
            LinearLayout mainLL = findViewById(R.id.mainLinearLayout);
            LinearLayout.LayoutParams mainLP = (LinearLayout.LayoutParams) mainLL.getLayoutParams();
            mainLP.width =  (int) (200 * getResources().getDisplayMetrics().density + 0.5) ;
            mainLL.setLayoutParams(mainLP);

            LinearLayout centerLL = findViewById(R.id.preference_center_linear_layout);
            LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
            centerLP.height = 0;
            centerLL.setLayoutParams(centerLP);
            ((LinearLayout)findViewById(R.id.mainLinearLayout)).setGravity(Gravity.CENTER_VERTICAL);
        }
    }

    private class ExitOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }
}
