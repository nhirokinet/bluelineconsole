package net.nhiroki.bluelineconsole.applicationMain;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.commands.urls.WebSearchEngine;
import net.nhiroki.bluelineconsole.commands.urls.WebSearchEnginesDatabase;
import net.nhiroki.bluelineconsole.dataStore.persistent.URLEntry;
import net.nhiroki.bluelineconsole.dataStore.persistent.URLPreferences;

import java.util.Locale;

import static android.view.inputmethod.EditorInfo.IME_FLAG_FORCE_ASCII;

public class PreferencesEachURLActivity extends BaseWindowActivity {
    private String _entry_id = null;
    private boolean _entry_enabled_when_started = false;

    public PreferencesEachURLActivity() {
        super(R.layout.preferences_custom_web_each_body, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setWindowBoundarySize(ROOT_WINDOW_FULL_WIDTH_IN_MOBILE, 3);

        this.changeBaseWindowElementSizeForAnimation(false);
        this.enableBaseWindowAnimation();

        findViewById(R.id.url_each_submit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean enabled_choice = ((Switch)findViewById(R.id.url_each_enabled)).isChecked();

                if (PreferencesEachURLActivity.this._entry_id != null && enabled_choice != PreferencesEachURLActivity.this._entry_enabled_when_started) {
                    new WebSearchEnginesDatabase(PreferencesEachURLActivity.this).setEntryEnabledById(PreferencesEachURLActivity.this, PreferencesEachURLActivity.this._entry_id, enabled_choice);
                }

                if (PreferencesEachURLActivity.this._entry_id != null && ! PreferencesEachURLActivity.this._entry_id.startsWith("custom-web-")) {
                    PreferencesEachURLActivity.this.finish();
                    return;
                }
                URLEntry entry = new URLEntry();
                entry.id = PreferencesEachURLActivity.this._entry_id ==null ? 0 : Integer.parseInt(PreferencesEachURLActivity.this._entry_id.split("-")[2]);
                entry.name = ((EditText)findViewById(R.id.url_each_name)).getText().toString();
                entry.display_name = ((EditText)findViewById(R.id.url_each_display_name)).getText().toString();
                entry.url_base = ((EditText)findViewById(R.id.url_each_base_url)).getText().toString();
                entry.has_query = ((Switch)findViewById(R.id.url_each_has_query)).isChecked();

                int err = entry.validate();

                if (err != 0) {
                    Toast.makeText(PreferencesEachURLActivity.this, err, Toast.LENGTH_LONG).show();
                    return;
                }

                if (PreferencesEachURLActivity.this._entry_id == null) {
                    long id = URLPreferences.getInstance(PreferencesEachURLActivity.this).add(entry);
                    if (!enabled_choice) {
                        new WebSearchEnginesDatabase(PreferencesEachURLActivity.this).setEntryEnabledById(PreferencesEachURLActivity.this, "custom-web-" + id, enabled_choice);
                    }
                } else {
                    URLPreferences.getInstance(PreferencesEachURLActivity.this).update(entry);
                }
                PreferencesEachURLActivity.this.finish();
            }
        });

        findViewById(R.id.url_each_delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PreferencesEachURLActivity.this._entry_id == null || ! PreferencesEachURLActivity.this._entry_id.startsWith("custom-web-")) {
                    return;
                }
                int id = Integer.parseInt(PreferencesEachURLActivity.this._entry_id.split("-")[2]);
                URLPreferences.getInstance(PreferencesEachURLActivity.this).deleteById(id);
                new WebSearchEnginesDatabase(PreferencesEachURLActivity.this).unsetEntryEnabledById(PreferencesEachURLActivity.this, PreferencesEachURLActivity.this._entry_id);
                PreferencesEachURLActivity.this.finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent from_intent = this.getIntent();

        this._entry_id = from_intent.getStringExtra("id_for_preference_value");

        if (this._entry_id == null) {
            // new
            this.setHeaderFooterTexts(this.getString(R.string.preferences_title_for_header_and_footer_add_custom_urls), null);
            ((Button)findViewById(R.id.url_each_submit_button)).setText(R.string.button_add);
            this.findViewById(R.id.url_each_name).setEnabled(true);
            this.findViewById(R.id.url_each_display_name).setEnabled(true);
            this.findViewById(R.id.url_each_base_url).setEnabled(true);
            this.findViewById(R.id.url_each_has_query).setEnabled(true);
            this.findViewById(R.id.url_each_submit_button).setVisibility(View.VISIBLE);
            this.findViewById(R.id.url_each_delete_button).setVisibility(View.GONE);

            ((EditText)findViewById(R.id.url_each_name)).setText("");
            ((EditText)findViewById(R.id.url_each_display_name)).setText("");
            ((EditText)findViewById(R.id.url_each_base_url)).setText("");
            ((Switch)findViewById(R.id.url_each_has_query)).setChecked(false);
            ((Switch)findViewById(R.id.url_each_enabled)).setChecked(true);
            this._entry_enabled_when_started = true;

            findViewById(R.id.url_each_varies_with_locale).setVisibility(View.GONE);

        } else {
            final WebSearchEngine entry = new WebSearchEnginesDatabase(this).getURLByIdForPreferences(this._entry_id, this.getResources().getConfiguration().locale);

            this.setHeaderFooterTexts(this.getString(R.string.preferences_title_for_header_and_footer_edit_custom_urls), null);
            ((Button)findViewById(R.id.url_each_submit_button)).setText(R.string.button_update);
            findViewById(R.id.url_each_delete_button).setVisibility(View.VISIBLE);
            this.findViewById(R.id.url_each_name).setEnabled(!entry.preset);
            this.findViewById(R.id.url_each_display_name).setEnabled(!entry.preset);
            this.findViewById(R.id.url_each_base_url).setEnabled(!entry.preset);
            this.findViewById(R.id.url_each_has_query).setEnabled(!entry.preset);
            this.findViewById(R.id.url_each_delete_button).setVisibility(entry.preset ? View.GONE : View.VISIBLE);

            ((EditText)findViewById(R.id.url_each_name)).setText(entry.name);
            ((EditText)findViewById(R.id.url_each_display_name)).setText(entry.display_name);
            ((EditText)findViewById(R.id.url_each_base_url)).setText(entry.url_base);
            ((Switch)findViewById(R.id.url_each_has_query)).setChecked(entry.has_query);
            ((Switch)findViewById(R.id.url_each_enabled)).setChecked(entry.enabled);
            this._entry_enabled_when_started = entry.enabled;

            findViewById(R.id.url_each_varies_with_locale).setVisibility(entry.varies_with_locale ? View.VISIBLE : View.GONE);
        }

        final EditText mainInputText = findViewById(R.id.url_each_name);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(MainActivity.PREF_KEY_MAIN_EDITTEXT_FLAG_FORCE_ASCII, false)) {
            mainInputText.setImeOptions(mainInputText.getImeOptions() | IME_FLAG_FORCE_ASCII);
        } else {
            mainInputText.setImeOptions(mainInputText.getImeOptions() & ~IME_FLAG_FORCE_ASCII);
        }

        if (Build.VERSION.SDK_INT >= 24) {
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(MainActivity.PREF_KEY_MAIN_EDITTEXT_HINT_LOCALE_ENGLISH, false)) {
                mainInputText.setImeHintLocales(new LocaleList(new Locale("en")));
            } else {
                mainInputText.setImeHintLocales(null);
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeBaseWindowElementSizeForAnimation(true);
    }
}
