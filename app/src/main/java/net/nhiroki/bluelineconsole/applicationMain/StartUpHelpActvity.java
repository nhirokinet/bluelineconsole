package net.nhiroki.bluelineconsole.applicationMain;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nhiroki.bluelineconsole.R;

public class StartUpHelpActvity extends BaseWindowActivity {
    public StartUpHelpActvity() {
        super(R.layout.start_up_help_body);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((TextView) findViewById(R.id.baseWindowMainHeaderTextView)).setText(getString(R.string.title_for_notification));
        ((TextView) findViewById(R.id.baseWindowMainFooterTextView)).setText(getString(R.string.title_for_notification));

        setResult(RESULT_OK, new Intent(this, MainActivity.class));

        Point displaySize = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(displaySize);

        int maxPanelWidth = (int)(560.0 * getResources().getDisplayMetrics().density);

        LinearLayout l = findViewById(R.id.baseWindowRootLinearLayout);
        l.setGravity(Gravity.CENTER_VERTICAL);
        int panelWidth = Math.min((int)(displaySize.x * ((displaySize.x < displaySize.y) ? 0.8 : 0.67)), maxPanelWidth);

        int paddingHorizontal = (displaySize.x - panelWidth) / 2;
        l.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);

        findViewById(R.id.startUpOKButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (((CheckBox) findViewById(R.id.startUpCheckBoxNotToContinue)).isChecked()) {
                    SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(StartUpHelpActvity.this).edit();
                    prefEdit.putBoolean("pref_main_show_startup_help", false);
                    prefEdit.apply();
                }
                StartUpHelpActvity.this.finish();
            }
        });

        this.changeElementSize(false);

        this.enableBaseWindowAnimation();
        ((ViewGroup) findViewById(R.id.start_up_main_wrapper_layout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        this.changeElementSize(hasFocus);
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    private void changeElementSize(boolean visible) {
        LinearLayout mainLL = findViewById(R.id.start_up_main_wrapper_layout);
        LinearLayout.LayoutParams mainLP = (LinearLayout.LayoutParams) mainLL.getLayoutParams();
        if (visible) {
            mainLP.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            mainLL.setLayoutParams(mainLP);
        } else {
            mainLP.height = 0;
            mainLL.setLayoutParams(mainLP);
        }
    }
}