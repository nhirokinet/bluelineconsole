package net.nhiroki.bluelineconsole.applicationMain;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.dataStore.deviceLocal.WidgetsSetting;
import net.nhiroki.bluelineconsole.wrapperForAndroid.AppWidgetsHostManager;

public class PreferencesWidgetCommandEachActivity extends BaseWindowActivity {
    public static final String INTENT_EXTRA_WIDGET_COMMAND_ID = "widget_command_id";

    private AppWidgetsHostManager appWidgetsHostManager = null;
    private AppWidgetsHostManager.WidgetCommand widgetCommand = null;


    public PreferencesWidgetCommandEachActivity() {
        super(R.layout.preferences_command_widget_each, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.appWidgetsHostManager = new AppWidgetsHostManager(this);

        this.setHeaderFooterTexts(getString(R.string.preferences_title_for_header_and_footer_widget_commands), null);
        this.setWindowBoundarySize(ROOT_WINDOW_FULL_WIDTH_IN_MOBILE, 3);

        this.changeBaseWindowElementSizeForAnimation(false);
        this.enableBaseWindowAnimation();

        Button addButton = this.findViewById(R.id.widget_command_save_and_select_widget);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PreferencesWidgetCommandEachActivity.this.widgetCommand == null) {
                    int appWidgetId = PreferencesWidgetCommandEachActivity.this.appWidgetsHostManager.allocateAppWidgetId();

                    PreferencesWidgetCommandEachActivity.this.widgetCommand = new AppWidgetsHostManager.WidgetCommand(0, null, appWidgetId);

                    PreferencesWidgetCommandEachActivity.this.widgetCommand.command = ((EditText) PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_edit_command_name)).getText().toString();
                    PreferencesWidgetCommandEachActivity.this.widgetCommand.abbreviation = ((androidx.appcompat.widget.SwitchCompat) PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_command_abbreviation_enabled)).isChecked();

                    int err = PreferencesWidgetCommandEachActivity.this.widgetCommand.validate();

                    if (err != 0) {
                        Toast.makeText(PreferencesWidgetCommandEachActivity.this, err, Toast.LENGTH_LONG).show();
                        PreferencesWidgetCommandEachActivity.this.appWidgetsHostManager.deleteAppWidgetId(appWidgetId);
                        PreferencesWidgetCommandEachActivity.this.widgetCommand = null;
                        return;
                    }

                    WidgetsSetting.getInstance(PreferencesWidgetCommandEachActivity.this).addWidgetCommand(PreferencesWidgetCommandEachActivity.this.widgetCommand);

                    Intent intent = new Intent(PreferencesWidgetCommandEachActivity.this, PreferencesWidgetCommandSelectWidget.class);
                    intent.putExtra(PreferencesWidgetCommandSelectWidget.INTENT_EXTRA_APP_WIDGET_ID, appWidgetId);
                    PreferencesWidgetCommandEachActivity.this.startActivity(intent);

                    PreferencesWidgetCommandEachActivity.this.finish();
                }
            }
        });

        Button updateButton = this.findViewById(R.id.widget_each_submit_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PreferencesWidgetCommandEachActivity.this.widgetCommand != null) {
                    PreferencesWidgetCommandEachActivity.this.widgetCommand.command = ((EditText) PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_edit_command_name)).getText().toString();
                    PreferencesWidgetCommandEachActivity.this.widgetCommand.abbreviation = ((androidx.appcompat.widget.SwitchCompat) PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_command_abbreviation_enabled)).isChecked();
                    try {
                        PreferencesWidgetCommandEachActivity.this.widgetCommand.heightPx = Integer.valueOf(((EditText) PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_edit_height)).getText().toString());
                    } catch (NumberFormatException e) {
                        // just not to update
                    }

                    if (PreferencesWidgetCommandEachActivity.this.widgetCommand.appWidgetProviderInfo != null) {
                        PreferencesWidgetCommandEachActivity.this.widgetCommand.heightPx = Math.max(PreferencesWidgetCommandEachActivity.this.widgetCommand.heightPx, PreferencesWidgetCommandEachActivity.this.widgetCommand.appWidgetProviderInfo.minResizeHeight);
                    }

                    int err = PreferencesWidgetCommandEachActivity.this.widgetCommand.validate();

                    if (err != 0) {
                        Toast.makeText(PreferencesWidgetCommandEachActivity.this, err, Toast.LENGTH_LONG).show();
                        return;
                    }

                    WidgetsSetting.getInstance(PreferencesWidgetCommandEachActivity.this).updateWidgetCommand(PreferencesWidgetCommandEachActivity.this.widgetCommand);
                    PreferencesWidgetCommandEachActivity.this.finish();
                }
            }
        });

        Button deleteButton = this.findViewById(R.id.widget_each_delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PreferencesWidgetCommandEachActivity.this.widgetCommand != null) {
                    PreferencesWidgetCommandEachActivity.this.appWidgetsHostManager.deleteWidgetCommand(PreferencesWidgetCommandEachActivity.this.widgetCommand);
                    PreferencesWidgetCommandEachActivity.this.finish();
                }
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeBaseWindowElementSizeForAnimation(true);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onStop() {
        super.originalOnStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent from_intent = this.getIntent();
        int widget_command_id = from_intent.getIntExtra(INTENT_EXTRA_WIDGET_COMMAND_ID, -1);

        if (widget_command_id != -1) {
            this.widgetCommand = WidgetsSetting.getInstance(this).getWidgetCommandById(this.appWidgetsHostManager, widget_command_id);

            if (this.widgetCommand.appWidgetProviderInfo != null) {
                ((TextView) this.findViewById(R.id.widget_display_name)).setText(this.widgetCommand.appWidgetProviderInfo.loadLabel(this.getPackageManager()));
                PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_each_configure).setVisibility(this.widgetCommand.appWidgetProviderInfo.configure != null ? View.VISIBLE : View.GONE);

                if (this.widgetCommand.appWidgetProviderInfo.configure != null) {
                    this.findViewById(R.id.widget_each_configure).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PreferencesWidgetCommandEachActivity.this.appWidgetsHostManager.startAppWidgetConfigureActivityForResult(PreferencesWidgetCommandEachActivity.this, PreferencesWidgetCommandEachActivity.this.widgetCommand.appWidgetId, 0, 0, null);
                        }
                    });
                }
                ((TextView) this.findViewById(R.id.widget_minimum_height)).setText(String.format(this.getString(R.string.preferences_widget_minimum_height_show), this.widgetCommand.appWidgetProviderInfo.minResizeHeight));
                ((TextView) this.findViewById(R.id.widget_default_height)).setText(String.format(this.getString(R.string.preferences_widget_default_height_show), this.widgetCommand.appWidgetProviderInfo.minHeight));

            } else {
                ((TextView) this.findViewById(R.id.widget_display_name)).setText(R.string.error_failure_could_not_connect_to_the_widget);
                PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_each_configure).setVisibility(View.GONE);

                ((TextView) this.findViewById(R.id.widget_minimum_height)).setText("");
                ((TextView) this.findViewById(R.id.widget_default_height)).setText("");

            }
            ((EditText) this.findViewById(R.id.widget_edit_command_name)).setText(this.widgetCommand.command);
            ((androidx.appcompat.widget.SwitchCompat) PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_command_abbreviation_enabled)).setChecked(this.widgetCommand.abbreviation);
            ((EditText) PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_edit_height)).setText(String.valueOf(this.widgetCommand.heightPx));

            PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_edit_height_area).setVisibility(View.VISIBLE);
            PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_each_submit_button).setVisibility(View.VISIBLE);
            PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_each_delete_button).setVisibility(View.VISIBLE);
            PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_command_save_and_select_widget).setVisibility(View.GONE);

        } else {
            ((TextView) this.findViewById(R.id.widget_display_name)).setText(R.string.preferences_command_widget_command_new_label);
            ((EditText) this.findViewById(R.id.widget_edit_command_name)).setText("");
            ((androidx.appcompat.widget.SwitchCompat) PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_command_abbreviation_enabled)).setChecked(false);

            PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_edit_height_area).setVisibility(View.GONE);
            PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_each_configure).setVisibility(View.GONE);
            PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_each_submit_button).setVisibility(View.GONE);
            PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_each_delete_button).setVisibility(View.GONE);
            PreferencesWidgetCommandEachActivity.this.findViewById(R.id.widget_command_save_and_select_widget).setVisibility(View.VISIBLE);
        }
    }
}
