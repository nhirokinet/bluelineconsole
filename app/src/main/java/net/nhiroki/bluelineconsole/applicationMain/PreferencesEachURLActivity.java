package net.nhiroki.bluelineconsole.applicationMain;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.dataStore.persistent.URLEntry;
import net.nhiroki.bluelineconsole.dataStore.persistent.URLPreferences;

public class PreferencesEachURLActivity extends BaseWindowActivity {
    private int _entry_id = -1;

    public PreferencesEachURLActivity() {
        super(R.layout.preferences_custom_web_each_body, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setNestingPadding(3);

        this.changeBaseWindowElementSize(false);
        this.enableBaseWindowAnimation();

        findViewById(R.id.url_submit_button).setOnClickListener(new View.OnClickListener() {
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
                } else {
                    URLPreferences.getInstance(PreferencesEachURLActivity.this).update(entry);
                }
                PreferencesEachURLActivity.this.finish();
            }
        });

        findViewById(R.id.url_delete_button).setOnClickListener(new View.OnClickListener() {
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
            this.setHeaderFooterTexts(this.getString(R.string.pref_add_custom_urls_title_for_header_and_footer), this.getString(R.string.pref_add_custom_urls_title_for_header_and_footer));
            ((Button)findViewById(R.id.url_submit_button)).setText(R.string.add_button_text);
            findViewById(R.id.url_delete_button).setVisibility(View.GONE);

            ((EditText)findViewById(R.id.url_name)).setText("");
            ((EditText)findViewById(R.id.url_display_name)).setText("");
            ((EditText)findViewById(R.id.url_base_url)).setText("");
            ((SwitchCompat)findViewById(R.id.url_has_query)).setChecked(false);

        } else {
            this.setHeaderFooterTexts(this.getString(R.string.pref_edit_custom_urls_title_for_header_and_footer), this.getString(R.string.pref_edit_custom_urls_title_for_header_and_footer));
            ((Button)findViewById(R.id.url_submit_button)).setText(R.string.update_button_text);
            findViewById(R.id.url_delete_button).setVisibility(View.VISIBLE);

            URLEntry entry = URLPreferences.getInstance(this).getById(this._entry_id);
            ((EditText)findViewById(R.id.url_name)).setText(entry.name);
            ((EditText)findViewById(R.id.url_display_name)).setText(entry.display_name);
            ((EditText)findViewById(R.id.url_base_url)).setText(entry.url_base);
            ((SwitchCompat)findViewById(R.id.url_has_query)).setChecked(entry.has_query);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeBaseWindowElementSize(true);
    }
}
