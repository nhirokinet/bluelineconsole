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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.dataStore.persistent.URLEntry;
import net.nhiroki.bluelineconsole.dataStore.persistent.URLPreferences;

import java.util.ArrayList;
import java.util.List;

public class PreferencesCustomWebActivity extends BaseWindowActivity {
    private URLListAdapter _urlListAdapter;
    private URLPreferences _urlPreferences;

    public PreferencesCustomWebActivity() {
        super(R.layout.preferences_custom_web_body);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.baseWindowRootLinearLayout).setPadding(
                (int)(16 * getResources().getDisplayMetrics().density),
                (int)(48 * getResources().getDisplayMetrics().density),
                (int)(16 * getResources().getDisplayMetrics().density),
                (int)(48 * getResources().getDisplayMetrics().density));

        ((TextView) findViewById(R.id.baseWindowMainHeaderTextView)).setText(getString(R.string.url_preference_title_for_header_and_footer));
        ((TextView) findViewById(R.id.baseWindowMainFooterTextView)).setText(getString(R.string.url_preference_title_for_header_and_footer));

        this.changeElementSize(false);
        this.enableBaseWindowAnimation();

        Button addButton = (Button) findViewById(R.id.customURLListAddButton);
        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                PreferencesCustomWebActivity.this.startActivity(new Intent(PreferencesCustomWebActivity.this, PreferencesEachURLActivity.class));
            }
        }
        );

        this._urlPreferences = URLPreferences.getInstance(this);

        this._urlListAdapter = new URLListAdapter(this, 0, new ArrayList<URLEntry>());

        ListView customListView = (ListView)findViewById(R.id.customURLList);
        customListView.setAdapter(this._urlListAdapter);
        customListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PreferencesCustomWebActivity.this, PreferencesEachURLActivity.class);
                intent.putExtra("id", PreferencesCustomWebActivity.this._urlListAdapter.getItem(position).id);
                PreferencesCustomWebActivity.this.startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        this._urlListAdapter.clear();
        this._urlListAdapter.addAll(this._urlPreferences.getAllEntries());
        this._urlListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeElementSize(true);
    }

    private class URLListAdapter extends ArrayAdapter<URLEntry> {
        public URLListAdapter(@NonNull Context context, int resource, @NonNull List<URLEntry> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.url_entry_view, null);
            }
            TextView nameTextView = (TextView) (convertView.findViewById(R.id.urlNameOnEntryView));
            nameTextView.setText(this.getItem(position).name);
            TextView displayNameTextView = (TextView) (convertView.findViewById(R.id.urlDisplayNameOnEntryView));
            displayNameTextView.setText(this.getItem(position).display_name);
            return convertView;
        }
    }

    private void changeElementSize(boolean visible) {
        LinearLayout mainLL = findViewById(R.id.preference_urls_main_linear_layout);
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