package net.nhiroki.bluelineconsole.applicationMain;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.wrapperForAndroid.AppWidgetsHostManager;

public class PreferencesHomeScreenEachWidgetActivity extends BaseWindowActivity {
    public static final String INTENT_EXTRA_WIDGET_ID = "widget_id";

    private AppWidgetsHostManager.HomeScreenWidgetInfo homeScreenWidgetInfo = null;
    private EditText heightEdit = null;

    private AppWidgetsHostManager appWidgetsHostManager = null;

    public PreferencesHomeScreenEachWidgetActivity() {
        super(R.layout.preferences_home_screen_each_widget, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.appWidgetsHostManager = new AppWidgetsHostManager(this);

        this.setHeaderFooterTexts(getString(R.string.preferences_title_for_header_and_footer_widget), null);
        this.setWindowBoundarySize(ROOT_WINDOW_FULL_WIDTH_IN_MOBILE, 3);

        this.changeBaseWindowElementSizeForAnimation(false);
        this.enableBaseWindowAnimation();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeBaseWindowElementSizeForAnimation(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent from_intent = this.getIntent();
        int widget_id = from_intent.getIntExtra(INTENT_EXTRA_WIDGET_ID, -1);

        this.homeScreenWidgetInfo = appWidgetsHostManager.fetchHomeScreenAppWidgetById(widget_id);

        this.heightEdit = this.findViewById(R.id.widget_edit_height);
        this.heightEdit.setText(String.valueOf(homeScreenWidgetInfo.heightPx));


        TextView widgetNameView = this.findViewById(R.id.widget_display_name);
        if (homeScreenWidgetInfo.appWidgetProviderInfo != null) {
            widgetNameView.setText(homeScreenWidgetInfo.appWidgetProviderInfo.loadLabel(this.getPackageManager()));

            ((TextView) this.findViewById(R.id.widget_minimum_height)).setText(String.format(this.getString(R.string.preferences_widget_minimum_height_show), this.homeScreenWidgetInfo.appWidgetProviderInfo.minResizeHeight));
            ((TextView) this.findViewById(R.id.widget_default_height)).setText(String.format(this.getString(R.string.preferences_widget_default_height_show), this.homeScreenWidgetInfo.appWidgetProviderInfo.minHeight));

            if (this.homeScreenWidgetInfo.appWidgetProviderInfo.configure != null) {
                this.findViewById(R.id.widget_each_configure).setVisibility(View.VISIBLE);
                this.findViewById(R.id.widget_each_configure).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PreferencesHomeScreenEachWidgetActivity.this.appWidgetsHostManager.startAppWidgetConfigureActivityForResult(PreferencesHomeScreenEachWidgetActivity.this, PreferencesHomeScreenEachWidgetActivity.this.homeScreenWidgetInfo.appWidgetId, 0, 0, null);
                    }
                });

            } else {
                this.findViewById(R.id.widget_each_configure).setVisibility(View.GONE);
            }

        } else {
            widgetNameView.setText(this.getString(R.string.error_failure_could_not_connect_to_the_widget));

            this.findViewById(R.id.widget_each_configure).setVisibility(View.GONE);
            ((TextView) this.findViewById(R.id.widget_minimum_height)).setText("");
            ((TextView) this.findViewById(R.id.widget_default_height)).setText("");
        }

        this.findViewById(R.id.widget_each_submit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PreferencesHomeScreenEachWidgetActivity.this.homeScreenWidgetInfo.heightPx = Integer.valueOf(PreferencesHomeScreenEachWidgetActivity.this.heightEdit.getText().toString());
                } catch (NumberFormatException e) {
                    // just not to update
                }
                PreferencesHomeScreenEachWidgetActivity.this.appWidgetsHostManager.saveHomeScreenWidgetInfo(PreferencesHomeScreenEachWidgetActivity.this.homeScreenWidgetInfo);
                PreferencesHomeScreenEachWidgetActivity.this.finish();
            }
        });

        this.findViewById(R.id.widget_each_delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesHomeScreenEachWidgetActivity.this.appWidgetsHostManager.deleteHomeScreenWidgetInfo(PreferencesHomeScreenEachWidgetActivity.this.homeScreenWidgetInfo);
                PreferencesHomeScreenEachWidgetActivity.this.finish();
            }
        });
    }
}
