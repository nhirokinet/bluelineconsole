package net.nhiroki.bluelineconsole.applicationMain;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.CheckBox;

import net.nhiroki.bluelineconsole.R;

public class StartUpHelpActivity extends BaseWindowActivity {
    public static final String PREF_KEY_SHOW_STARTUP_HELP = "pref_main_show_startup_help";

    public StartUpHelpActivity() {
        super(R.layout.notification_start_up_help_body, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHeaderFooterTexts(getString(R.string.startup_help_title_for_header_and_footer), null);

        setResult(RESULT_OK, new Intent(this, MainActivity.class));

        this.setWindowLocationGravity(Gravity.CENTER_VERTICAL);

        // Window nest count is 1, but make margin larger because background window is smaller and hides
        this.setWindowBoundarySize(ROOT_WINDOW_ALWAYS_HORIZONTAL_MARGIN, 2);

        findViewById(R.id.startUpOKButton).setOnClickListener(v -> {
            if (((CheckBox) findViewById(R.id.startUpCheckBoxNotToContinue)).isChecked()) {
                SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(StartUpHelpActivity.this).edit();
                prefEdit.putBoolean(PREF_KEY_SHOW_STARTUP_HELP, false);
                prefEdit.apply();
            }
            StartUpHelpActivity.this.finish();
        });

        this.changeBaseWindowElementSizeForAnimation(false);
        this.enableBaseWindowAnimation();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeBaseWindowElementSizeForAnimation(hasFocus);
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onStop() {
        // This app should be as stateless as possible. When app disappears most activities should finish.
        super.onStop();
        this.finish();
    }
}