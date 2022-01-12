package net.nhiroki.bluelineconsole.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.RemoteViews;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.BaseWindowActivity;
import net.nhiroki.bluelineconsole.applicationMain.MainActivity;


public class LauncherWidgetProvider extends AppWidgetProvider {
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        final String theme = BaseWindowActivity.determineActualTheme(context, BaseWindowActivity.readThemeFromConfigStatic(context));
        updateWidgetsForTheme(context, appWidgetManager, new int[]{appWidgetId}, theme);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        final String theme = BaseWindowActivity.determineActualTheme(context, BaseWindowActivity.readThemeFromConfigStatic(context));
        updateWidgetsForTheme(context, appWidgetManager, appWidgetIds, theme);
    }

    public static void updateTheme(Context context, String theme) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        int[] appWidgetIds;

        try {
            appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context.createPackageContext(context.getPackageName(), 0), LauncherWidgetProvider.class));
        } catch (PackageManager.NameNotFoundException e) {
            appWidgetIds = new int[0];
        }

        updateWidgetsForTheme(context, appWidgetManager, appWidgetIds, theme);
    }

    private static void updateWidgetsForTheme(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds, String theme) {
        theme = BaseWindowActivity.determineActualTheme(context, theme);
        final int layoutId;

        switch (theme) {
            case BaseWindowActivity.PREF_VALUE_THEME_DARK:
                layoutId = R.layout.widget_launcher_dark_theme;
                break;

            case BaseWindowActivity.PREF_VALUE_THEME_MARINE:
                layoutId = R.layout.widget_launcher_marine;
                break;

            case BaseWindowActivity.PREF_VALUE_THEME_OLD_COMPUTER:
                layoutId = R.layout.widget_launcher_old_computer;
                break;

            case BaseWindowActivity.PREF_VALUE_THEME_LIGHT:
            default:
                layoutId = R.layout.widget_launcher_default_theme;
                break;
        }

        for (int appWidgetId: appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivities(context, 0, new Intent[]{intent}, PendingIntent.FLAG_MUTABLE);
            remoteViews.setOnClickPendingIntent(R.id.widgetLauncherRootLinearLayout, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
