package net.nhiroki.bluelineconsole.applicationMain;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.dataStore.deviceLocal.WidgetsSetting;
import net.nhiroki.bluelineconsole.wrapperForAndroid.AppWidgetsHostManager;

import java.util.ArrayList;


public class PreferencesWidgetCommandsActivity extends BaseWindowActivity {
    private WidgetCommandAdapter listViewAdapter;
    private AppWidgetsHostManager appWidgetsHostManager = null;

    public PreferencesWidgetCommandsActivity() {
        super(R.layout.preferences_command_widget_list, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.appWidgetsHostManager = new AppWidgetsHostManager(this);

        this.setHeaderFooterTexts(getString(R.string.preferences_title_for_header_and_footer_widget_commands), null);
        this.setWindowBoundarySize(ROOT_WINDOW_FULL_WIDTH_IN_MOBILE, 2);

        this.changeBaseWindowElementSizeForAnimation(false);
        this.enableBaseWindowAnimation();

        Button addButton = this.findViewById(R.id.widget_command_add_button);
        addButton.setOnClickListener(v -> PreferencesWidgetCommandsActivity.this.startActivity(new Intent(PreferencesWidgetCommandsActivity.this, PreferencesWidgetCommandEachActivity.class)));

        ListView listView = this.findViewById(R.id.widget_command_list);
        this.listViewAdapter = new WidgetCommandAdapter(this);
        listView.setAdapter(listViewAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            AppWidgetsHostManager.WidgetCommand widgetCommand = PreferencesWidgetCommandsActivity.this.listViewAdapter.getItem(position);

            Intent intent = new Intent(PreferencesWidgetCommandsActivity.this, PreferencesWidgetCommandEachActivity.class);
            intent.putExtra(PreferencesWidgetCommandEachActivity.INTENT_EXTRA_WIDGET_COMMAND_ID, widgetCommand.id);
            PreferencesWidgetCommandsActivity.this.startActivity(intent);
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeBaseWindowElementSizeForAnimation(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.appWidgetsHostManager.garbageCollectForAppWidgetIds();

        listViewAdapter.clear();
        listViewAdapter.addAll(WidgetsSetting.getInstance(this).getAllWidgetCommands(this.appWidgetsHostManager));
    }

    private static class WidgetCommandAdapter extends ArrayAdapter<AppWidgetsHostManager.WidgetCommand> {
        private final Context context;

        public WidgetCommandAdapter(@NonNull Context context) {
            super(context,0, new ArrayList<>());
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            AppWidgetsHostManager.WidgetCommand item = this.getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.preferences_command_widget_each_on_list, parent, false);
            }

            ((TextView)convertView.findViewById(R.id.widgetCommandNameView)).setText(item.command);
            if (item.appWidgetProviderInfo != null) {
                ((TextView) convertView.findViewById(R.id.widgetCommandWidgetNameView)).setText(item.appWidgetProviderInfo.loadLabel(this.context.getPackageManager()));
            } else {
                ((TextView) convertView.findViewById(R.id.widgetCommandWidgetNameView)).setText(context.getString(R.string.error_failure_could_not_connect_to_the_widget));
            }

            convertView.findViewById(R.id.widgetCommandAbbreviation).setVisibility(item.abbreviation ? View.VISIBLE : View.GONE);

            return convertView;
        }

        @Override
        public boolean isEnabled (int position) {
            return true;
        }

    }
}
