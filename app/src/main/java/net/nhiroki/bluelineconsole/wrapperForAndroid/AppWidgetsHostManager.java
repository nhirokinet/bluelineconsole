package net.nhiroki.bluelineconsole.wrapperForAndroid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.commandSearchers.lib.StringMatchStrategy;
import net.nhiroki.bluelineconsole.dataStore.deviceLocal.WidgetsSetting;


public final class AppWidgetsHostManager {
    public static final int MY_WIDGET_HOST_ID = 1001;

    private final Context context;
    private AppWidgetHost widgetHost = null;

    private final WidgetsSetting widgetsSetting;

    public static class HomeScreenWidgetInfo {
        public HomeScreenWidgetInfo(int id, AppWidgetProviderInfo appWidgetProviderInfo, int appWidgetId) {
            this.id = id;
            this.appWidgetId = appWidgetId;
            this.appWidgetProviderInfo = appWidgetProviderInfo;
        }
        public final int id;
        public final int appWidgetId;
        public final AppWidgetProviderInfo appWidgetProviderInfo;
        public int heightPx;
        public int afterDefaultItem;
    }

    public static class WidgetCommand {
        public WidgetCommand(int id, AppWidgetProviderInfo appWidgetProviderInfo, int appWidgetId) {
            this.id = id;
            this.appWidgetId = appWidgetId;
            this.appWidgetProviderInfo = appWidgetProviderInfo;
        }
        public final int id;
        public String command;
        public boolean abbreviation;
        public final int appWidgetId;
        public final AppWidgetProviderInfo appWidgetProviderInfo;
        public int heightPx = -1;

        public @StringRes
        int validate() {
            if (this.command.isEmpty()) {
                return R.string.error_invalid_command_name;
            }

            for (int i = 0; i < this.command.length(); ++i) {
                char c = this.command.charAt(i);
                if (c == ' ') {
                    return R.string.error_invalid_command_name;
                }
            }

            return 0;
        }
    }

    public static class HomeScreenWidgetViewItem {
        public int afterDefaultItem;
        public View widgetView;
    }


    public AppWidgetsHostManager(Context context) {
        this.context = context;
        this.widgetsSetting = WidgetsSetting.getInstance(context);
    }

    /**
     * TODO: this is unused because this crashes.
     *
     * Current memo to reproduce:
     *   - Android emulator Nexus 10 API 30
     *   - Set some widget command
     *   - Terminate app
     *   - Just open MainActivity and input the widget command to show widget
     *   - Make MainActivity invisible, by exiting or by going to Digital Assistance setting from config.
     *     - call stopListening() in MainActivity.onStop()
     *   - Detect crash via adb
     *     - Basic behavior is that Blue Line Console gets invisible, so normally it is not visible
     *
     * Besides, currently I found nothing bad for not calling stopListening.
     */
    /*
    public void stopListening() {
        if (this.widgetHost != null) {
            this.widgetHost.stopListening();
            this.widgetHost = null;
        }
    }
     */

    public void startAppWidgetConfigureActivityForResult(Activity baseActivity, int appWidgetId, int intentFlags, int requestCode, android.os.Bundle options) {
        new AppWidgetHost(this.context.getApplicationContext(), MY_WIDGET_HOST_ID).startAppWidgetConfigureActivityForResult(baseActivity, appWidgetId, intentFlags, requestCode, options);
    }

    public List<HomeScreenWidgetViewItem> createHomeScreenWidgets() {
        List<HomeScreenWidgetViewItem> ret = new ArrayList<>();

        List<HomeScreenWidgetInfo> homeScreenWidgetInfoList = this.widgetsSetting.getAllHomeScreenWidgets(this);

        for (HomeScreenWidgetInfo info: homeScreenWidgetInfoList) {
            HomeScreenWidgetViewItem item = new HomeScreenWidgetViewItem();
            item.afterDefaultItem = info.afterDefaultItem;
            item.widgetView = this.createView(info.appWidgetId, info.heightPx);
            ret.add(item);
        }
        return ret;
    }

    public List<View> createWidgetsForCommand(String command) {
        List<View> ret = new ArrayList<>();

        List<WidgetCommand> widgetCommandList = this.widgetsSetting.getAllWidgetCommands(this);

        for (WidgetCommand widgetCommand: widgetCommandList) {
            if (command.equals(widgetCommand.command) || (widgetCommand.abbreviation && StringMatchStrategy.match(this.context, command, widgetCommand.command, true) != -1)) {
                ret.add(this.createView(widgetCommand.appWidgetId, widgetCommand.heightPx));
            }
        }

        return ret;
    }

    public void garbageCollectForAppWidgetIds() {
        if (Build.VERSION.SDK_INT >= 26) {
            AppWidgetHost appWidgetHost = new AppWidgetHost(this.context.getApplicationContext(), MY_WIDGET_HOST_ID);

            Set<Integer> usedAppWidgetIds = new HashSet<>();

            for (HomeScreenWidgetInfo homeScreenWidgetInfo: this.fetchHomeScreenAppWidgets()) {
                usedAppWidgetIds.add(homeScreenWidgetInfo.appWidgetId);
            }

            for (WidgetCommand widgetCommand: WidgetsSetting.getInstance(this.context).getAllWidgetCommands(this)) {
                usedAppWidgetIds.add(widgetCommand.appWidgetId);
            }

            for (int appWidgetId: appWidgetHost.getAppWidgetIds()) {
                if (! usedAppWidgetIds.contains(appWidgetId)) {
                    appWidgetHost.deleteAppWidgetId(appWidgetId);
                }
            }
        }
    }

    public List<HomeScreenWidgetInfo> fetchHomeScreenAppWidgets() {
        return this.widgetsSetting.getAllHomeScreenWidgets(this);
    }

    @Nullable
    public HomeScreenWidgetInfo fetchHomeScreenAppWidgetById(int id) {
        return this.widgetsSetting.getHomeScreenById(this, id);
    }

    @Nullable
    public AppWidgetProviderInfo getAppWidgetInfo(int appWidgetId) {
        // In cases like backup restore occurred, it seems to return null.
        // Returning null in case no access is written in document.
        return android.appwidget.AppWidgetManager.getInstance(this.context.getApplicationContext()).getAppWidgetInfo(appWidgetId);
    }

    // Handle widget config totally here because they cannot be synced among devices
    public void saveHomeScreenWidgetInfo(HomeScreenWidgetInfo homeScreenWidgetInfo) {
        if (homeScreenWidgetInfo.appWidgetProviderInfo != null) {
            homeScreenWidgetInfo.heightPx = Math.max(homeScreenWidgetInfo.heightPx, homeScreenWidgetInfo.appWidgetProviderInfo.minResizeHeight);
        }
        this.widgetsSetting.updateHomeScreenWidgetInfo(homeScreenWidgetInfo);
    }

    public void deleteAppWidgetId(int appWidgetId) {
        new AppWidgetHost(this.context.getApplicationContext(), MY_WIDGET_HOST_ID).deleteAppWidgetId(appWidgetId);
    }

    public void deleteHomeScreenWidgetInfo(HomeScreenWidgetInfo homeScreenWidgetInfo) {
        new AppWidgetHost(this.context.getApplicationContext(), MY_WIDGET_HOST_ID).deleteAppWidgetId(homeScreenWidgetInfo.appWidgetId);
        this.widgetsSetting.deleteHomeScreenWidgetById(homeScreenWidgetInfo.id);
    }

    public void deleteWidgetCommand(WidgetCommand widgetCommand) {
        new AppWidgetHost(this.context.getApplicationContext(), MY_WIDGET_HOST_ID).deleteAppWidgetId(widgetCommand.appWidgetId);
        this.widgetsSetting.deleteWidgetCommandById(widgetCommand.id);
    }

    public void addHomeScreenAppWidget(int appWidgetId, int afterDefaultItem) {
        AppWidgetProviderInfo info = android.appwidget.AppWidgetManager.getInstance(this.context.getApplicationContext()).getAppWidgetInfo(appWidgetId);
        HomeScreenWidgetInfo homeScreenWidgetInfo = new HomeScreenWidgetInfo(0, info, appWidgetId);
        /* AppWidgetProviderInfo.min(Resize?)(Width|Height) is documented as expressed in dp unit,
         * https://developer.android.com/reference/android/appwidget/AppWidgetProviderInfo#maxResizeHeight
         * but is it actually in pixels?
         */
        homeScreenWidgetInfo.heightPx = info.minHeight;
        homeScreenWidgetInfo.afterDefaultItem = afterDefaultItem;

        this.widgetsSetting.addWidgetToHomeScreen(homeScreenWidgetInfo);
    }

    public List<AppWidgetProviderInfo> getInstalledProviders() {
        return AppWidgetManager.getInstance(this.context.getApplicationContext()).getInstalledProviders();
    }

    public boolean bindAppWidgetIdIfAllowed(int appWidgetId, AppWidgetProviderInfo appWidgetProviderInfo) {
        return AppWidgetManager.getInstance(this.context.getApplicationContext()).bindAppWidgetIdIfAllowed(appWidgetId, appWidgetProviderInfo.provider);
    }

    public Intent createAppBindWidgetRequestIntent(int appWidgetId, AppWidgetProviderInfo appWidgetProviderInfo) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, appWidgetProviderInfo.provider);

        return intent;
    }

    public int allocateAppWidgetId() {
        if (this.widgetHost == null) {
            this.widgetHost = new AppWidgetHost(this.context.getApplicationContext(), MY_WIDGET_HOST_ID);
        }
        return this.widgetHost.allocateAppWidgetId();
    }

    private View createView(int appWidgetId, int heightPx) {
        if (this.widgetHost == null) {
            this.widgetHost = new AppWidgetHost(this.context.getApplicationContext(), MY_WIDGET_HOST_ID);
            this.widgetHost.startListening();
        }

        AppWidgetProviderInfo info = android.appwidget.AppWidgetManager.getInstance(this.context.getApplicationContext()).getAppWidgetInfo(appWidgetId);

        if (info != null) {
            AppWidgetHostView hostView = widgetHost.createView(this.context.getApplicationContext(), appWidgetId, info);

            ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx);
            hostView.setLayoutParams(params);

            return hostView;

        } else {
            TextView errorView = new TextView(this.context);
            errorView.setText(this.context.getString(R.string.error_failure_could_not_connect_to_the_widget));
            errorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            int titleViewPadding = (int)(errorView.getTextSize() * 0.3);
            errorView.setPadding(titleViewPadding, titleViewPadding, titleViewPadding, titleViewPadding);

            ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx);
            errorView.setLayoutParams(params);

            return errorView;
        }
    }
}
