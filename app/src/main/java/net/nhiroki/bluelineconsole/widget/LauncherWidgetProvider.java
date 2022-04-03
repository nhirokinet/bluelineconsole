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

import androidx.annotation.LayoutRes;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.MainActivity;
import net.nhiroki.bluelineconsole.applicationMain.theming.AppTheme;
import net.nhiroki.bluelineconsole.applicationMain.theming.AppThemeDirectory;


public class LauncherWidgetProvider extends AppWidgetProvider {
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        updateWidgetsForTheme(context, appWidgetManager, new int[]{appWidgetId}, null);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        updateWidgetsForTheme(context, appWidgetManager, appWidgetIds, null);
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

    private static void updateWidgetsForTheme(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds, String themeID) {
        final AppTheme appTheme = themeID != null ? AppThemeDirectory.loadAppTheme(themeID) : AppThemeDirectory.loadAppTheme(context);
        final @LayoutRes int layoutId = appTheme.getLauncherWidgetLayoutID(context);

        for (int appWidgetId: appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivities(context, 0, new Intent[]{intent}, PendingIntent.FLAG_MUTABLE);
            remoteViews.setOnClickPendingIntent(R.id.widgetLauncherRootLinearLayout, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
