package net.nhiroki.bluelineconsole.applicationMain;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nhiroki.bluelineconsole.R;

public class StartUpHelpActvity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.activity_start_up_help_actvity);
        setResult(RESULT_OK, new Intent(this, MainActivity.class));
        this.findViewById(R.id.mainLinearLayout).setOnClickListener(new ExitOnClickListener());

        ((TextView)findViewById(R.id.startUpHelpingCommandTextView)).setTypeface(Typeface.MONOSPACE);

        // Decrease topMargin (which is already negative) by 1 physical pixel to fill the gap. See the comment in xml .
        View versionOnMainFooterWrapper = findViewById(R.id.versionOnMainFooterWrapper);
        ViewGroup.MarginLayoutParams versionOnMainFooterWrapperLayoutParam = (ViewGroup.MarginLayoutParams) versionOnMainFooterWrapper.getLayoutParams();
        versionOnMainFooterWrapperLayoutParam.setMargins(
                versionOnMainFooterWrapperLayoutParam.leftMargin,
                versionOnMainFooterWrapperLayoutParam.topMargin - 1,
                versionOnMainFooterWrapperLayoutParam.rightMargin,
                versionOnMainFooterWrapperLayoutParam.bottomMargin
        );
        versionOnMainFooterWrapper.setLayoutParams(versionOnMainFooterWrapperLayoutParam);

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

        ((ViewGroup) findViewById(R.id.mainLinearLayout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            LinearLayout mainLL = findViewById(R.id.start_up_help_main_linear_layout);
            LinearLayout.LayoutParams mainLP = (LinearLayout.LayoutParams) mainLL.getLayoutParams();
            mainLP.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            mainLL.setLayoutParams(mainLP);
        } else {
            LinearLayout mainLL = findViewById(R.id.start_up_help_main_linear_layout);
            LinearLayout.LayoutParams mainLP = (LinearLayout.LayoutParams) mainLL.getLayoutParams();
            mainLP.height = 0;
            mainLL.setLayoutParams(mainLP);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    private class ExitOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (((CheckBox) findViewById(R.id.startUpCheckBoxNotToContinue)).isChecked()) {
                SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(StartUpHelpActvity.this).edit();
                prefEdit.putBoolean("pref_main_show_startup_help", false);
                prefEdit.apply();
            }
            finish();
        }
    }
}
