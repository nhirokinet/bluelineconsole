package net.nhiroki.bluelineconsole.applicationMain;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.commands.urls.WebSearchEngine;
import net.nhiroki.bluelineconsole.commands.urls.WebSearchEnginesDatabase;

import java.util.ArrayList;
import java.util.List;

public class PreferencesCustomWebActivity extends BaseWindowActivity {
    private URLListAdapter _urlListAdapter;

    public PreferencesCustomWebActivity() {
        super(R.layout.preferences_custom_web_body, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHeaderFooterTexts(getString(R.string.preferences_title_for_header_and_footer_url), null);
        this.setWindowBoundarySize(ROOT_WINDOW_FULL_WIDTH_IN_MOBILE, 2);

        this.changeBaseWindowElementSizeForAnimation(false);
        this.enableBaseWindowAnimation();

        Button addButton = findViewById(R.id.customURLListAddButton);
        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                PreferencesCustomWebActivity.this.startActivity(new Intent(PreferencesCustomWebActivity.this, PreferencesEachURLActivity.class));
            }
        }
        );

        this._urlListAdapter = new URLListAdapter(this, 0, new ArrayList<WebSearchEngine>());

        ListView customListView = findViewById(R.id.customURLList);
        customListView.setAdapter(this._urlListAdapter);
        customListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PreferencesCustomWebActivity.this, PreferencesEachURLActivity.class);
                intent.putExtra("id_for_preference_value", PreferencesCustomWebActivity.this._urlListAdapter.getItem(position).id_for_preference_value);
                PreferencesCustomWebActivity.this.startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        this._urlListAdapter.clear();
        this._urlListAdapter.addAll(new WebSearchEnginesDatabase(this).getURLListForLocale(this.getResources().getConfiguration().locale, false));
        this._urlListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeBaseWindowElementSizeForAnimation(true);
    }

    private static class URLListAdapter extends ArrayAdapter<WebSearchEngine> {
        public URLListAdapter(@NonNull Context context, int resource, @NonNull List<WebSearchEngine> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.url_entry_view, parent, false);
            }
            ((TextView)convertView.findViewById(R.id.urlNameOnEntryView)).setText(this.getItem(position).name);
            ((TextView)convertView.findViewById(R.id.urlDisplayNameOnEntryView)).setText(this.getItem(position).display_name);
            convertView.findViewById(R.id.urlPresetOnEntryView).setVisibility(this.getItem(position).preset ? View.VISIBLE : View.GONE);
            convertView.findViewById(R.id.urlDisabledOnEntryView).setVisibility(this.getItem(position).enabled ? View.GONE : View.VISIBLE);

            return convertView;
        }
    }
}