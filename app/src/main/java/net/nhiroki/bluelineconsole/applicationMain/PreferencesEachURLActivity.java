package net.nhiroki.bluelineconsole.applicationMain;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.data_store.persistent.URLEntry;
import net.nhiroki.bluelineconsole.data_store.persistent.URLPreferences;

public class PreferencesEachURLActivity extends BaseWindowActivity {
    private int _entry_id = -1;

    public PreferencesEachURLActivity() {
        super(R.layout.preferences_custom_web_each_body);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.baseWindowRootLinearLayout).setPadding(
                (int)(24 * getResources().getDisplayMetrics().density),
                (int)(72 * getResources().getDisplayMetrics().density),
                (int)(24 * getResources().getDisplayMetrics().density),
                (int)(72 * getResources().getDisplayMetrics().density));

        this.changeElementSize(false);
        this.enableBaseWindowAnimation();

        ((Button) findViewById(R.id.url_submit_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                URLEntry entry = new URLEntry();
                entry.id = PreferencesEachURLActivity.this._entry_id;
                entry.name = ((EditText)findViewById(R.id.url_name)).getText().toString();
                entry.display_name = ((EditText)findViewById(R.id.url_display_name)).getText().toString();
                entry.url_base = ((EditText)findViewById(R.id.url_base_url)).getText().toString();
                entry.has_query = ((Switch)findViewById(R.id.url_has_query)).isChecked();

                int err = entry.validate();

                if (err != 0) {
                    Toast.makeText(PreferencesEachURLActivity.this, err, Toast.LENGTH_LONG).show();
                    return;
                }

                if (PreferencesEachURLActivity.this._entry_id == 0) {
                    URLPreferences.getInstance(PreferencesEachURLActivity.this).add(entry);
                    PreferencesEachURLActivity.this.finish();
                } else {
                    URLPreferences.getInstance(PreferencesEachURLActivity.this).update(entry);
                    PreferencesEachURLActivity.this.finish();
                }
            }
        });

        ((Button) findViewById(R.id.url_delete_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                URLPreferences.getInstance(PreferencesEachURLActivity.this).deleteById(PreferencesEachURLActivity.this._entry_id);
                PreferencesEachURLActivity.this.finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent from_intent = this.getIntent();

        this._entry_id = from_intent.getIntExtra("id", 0);

        if (this._entry_id == 0) {
            // new
            ((TextView) findViewById(R.id.baseWindowMainHeaderTextView)).setText(this.getString(R.string.pref_add_custom_urls_title_for_header_and_footer));
            ((TextView) findViewById(R.id.baseWindowMainFooterTextView)).setText(this.getString(R.string.pref_add_custom_urls_title_for_header_and_footer));
            ((Button)findViewById(R.id.url_submit_button)).setText(R.string.add_button_text);
            findViewById(R.id.url_delete_button).setVisibility(View.GONE);

            ((EditText)findViewById(R.id.url_name)).setText("");
            ((EditText)findViewById(R.id.url_display_name)).setText("");
            ((EditText)findViewById(R.id.url_base_url)).setText("");
            ((Switch)findViewById(R.id.url_has_query)).setChecked(false);

        } else {
            ((TextView) findViewById(R.id.baseWindowMainHeaderTextView)).setText(this.getString(R.string.pref_edit_custom_urls_title_for_header_and_footer));
            ((TextView) findViewById(R.id.baseWindowMainFooterTextView)).setText(this.getString(R.string.pref_edit_custom_urls_title_for_header_and_footer));
            ((Button)findViewById(R.id.url_submit_button)).setText(R.string.update_button_text);
            findViewById(R.id.url_delete_button).setVisibility(View.VISIBLE);

            URLEntry entry = URLPreferences.getInstance(this).getById(this._entry_id);
            ((EditText)findViewById(R.id.url_name)).setText(entry.name);
            ((EditText)findViewById(R.id.url_display_name)).setText(entry.display_name);
            ((EditText)findViewById(R.id.url_base_url)).setText(entry.url_base);
            ((Switch)findViewById(R.id.url_has_query)).setChecked(entry.has_query);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeElementSize(true);
    }

    private void changeElementSize(boolean visible) {
        ScrollView mainLL = findViewById(R.id.each_url_main_scroll_view);
        LinearLayout.LayoutParams mainLP = (LinearLayout.LayoutParams) mainLL.getLayoutParams();
        View centerLL = findViewById(R.id.baseWindowMainLinearLayout);
        View centerLLOuter = findViewById(R.id.baseWindowMainLinearLayoutOuter);

        if (visible) {
            mainLP.width = LinearLayout.LayoutParams.MATCH_PARENT;
            mainLP.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            mainLL.setLayoutParams(mainLP);

            LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
            centerLP.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            centerLL.setLayoutParams(centerLP);

            LinearLayout.LayoutParams centerLPOuter = (LinearLayout.LayoutParams) centerLLOuter.getLayoutParams();
            centerLPOuter.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            centerLLOuter.setLayoutParams(centerLPOuter);

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
        }
    }
}
