package net.nhiroki.bluelineconsole.applicationMain;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.dataStore.deviceLocal.WidgetsSetting;
import net.nhiroki.bluelineconsole.wrapperForAndroid.AppWidgetsHostManager;

import java.util.ArrayList;
import java.util.List;

public class PreferencesWidgetCommandSelectWidget extends BaseWindowActivity {
    public static final String INTENT_EXTRA_APP_WIDGET_ID = "app_widget_id";

    private static final int REQUEST_APPWIDGET_BIND = 1;
    private static final int REQUEST_APPWIDGET_CONFIGURE = 2;

    private AppWidgetsHostManager appWidgetsHostManager = null;

    private int appWidgetId;


    public PreferencesWidgetCommandSelectWidget() {
        super(R.layout.preferences_home_screen_add_widget, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.appWidgetsHostManager = new AppWidgetsHostManager(this);

        this.setHeaderFooterTexts(getString(R.string.preferences_title_for_header_and_footer_widget_commands), null);
        this.setWindowBoundarySize(ROOT_WINDOW_FULL_WIDTH_IN_MOBILE, 3);

        this.changeBaseWindowElementSizeForAnimation(false);
        this.enableBaseWindowAnimation();

        final ListView customListView = findViewById(R.id.homeScreenSelectWidgetToAdd);
        final WidgetCandidatesListAdapter adapter = new WidgetCandidatesListAdapter(this, 0, new ArrayList<AppWidgetProviderInfo>());
        customListView.setAdapter(adapter);

        List<AppWidgetProviderInfo> wl = this.appWidgetsHostManager.getInstalledProviders();

        customListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean res = PreferencesWidgetCommandSelectWidget.this.appWidgetsHostManager.bindAppWidgetIdIfAllowed(appWidgetId, adapter.getItem(position));

                if (!res) {
                    Intent intent = PreferencesWidgetCommandSelectWidget.this.appWidgetsHostManager.createAppBindWidgetRequestIntent(appWidgetId, adapter.getItem(position));
                    PreferencesWidgetCommandSelectWidget.this.startActivityForResult(intent, REQUEST_APPWIDGET_BIND);
                    return;
                }

                AppWidgetProviderInfo info = AppWidgetManager.getInstance(PreferencesWidgetCommandSelectWidget.this.getApplicationContext()).getAppWidgetInfo(appWidgetId);

                WidgetsSetting.getInstance(PreferencesWidgetCommandSelectWidget.this).updateWidgetCommandHeightPxByAppWidgetId(appWidgetId, info.minHeight);

                if (info.configure != null) {
                    PreferencesWidgetCommandSelectWidget.this.appWidgetsHostManager.startAppWidgetConfigureActivityForResult(PreferencesWidgetCommandSelectWidget.this, appWidgetId, 0, REQUEST_APPWIDGET_CONFIGURE, null);

                } else {
                    PreferencesWidgetCommandSelectWidget.this.finish();
                }
            }
        });

        for (AppWidgetProviderInfo a: wl) {
            adapter.add(a);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int appWidgetId;

        switch (requestCode) {
            case REQUEST_APPWIDGET_BIND:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                        AppWidgetProviderInfo info = AppWidgetManager.getInstance(this.getApplicationContext()).getAppWidgetInfo(appWidgetId);

                        WidgetsSetting.getInstance(PreferencesWidgetCommandSelectWidget.this).updateWidgetCommandHeightPxByAppWidgetId(appWidgetId, info.minHeight);

                        if (info.configure != null) {
                            PreferencesWidgetCommandSelectWidget.this.appWidgetsHostManager.startAppWidgetConfigureActivityForResult(PreferencesWidgetCommandSelectWidget.this, appWidgetId, 0, REQUEST_APPWIDGET_CONFIGURE, null);
                        } else {
                            this.finish();
                        }
                        break;

                    case Activity.RESULT_CANCELED:
                        // It seems it happens that data is null.
                        // In this case appWidgetId is not known and cannot be deleted, although it should be deleted.
                        if (data != null) {
                            appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                            if (appWidgetId != -1) {
                                PreferencesWidgetCommandSelectWidget.this.appWidgetsHostManager.deleteAppWidgetId(appWidgetId);
                            }
                        }
                        break;

                    default:
                        // As long as reading the document on 2021/10/02, this seems not to happen
                        // https://developer.android.com/reference/android/appwidget/AppWidgetManager#ACTION_APPWIDGET_BIND
                        break;
                }
                break;

            case REQUEST_APPWIDGET_CONFIGURE:
                this.finish();
                break;

            default:
                // Unknown request, start normally
        }
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
        this.appWidgetId = from_intent.getIntExtra(INTENT_EXTRA_APP_WIDGET_ID, -1);

        if (this.appWidgetId == -1) {
            this.finish();
        }
    }
}
