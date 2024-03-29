package net.nhiroki.bluelineconsole.applicationMain.theming.eachTheme;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.widget.RemoteViews;

import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.BaseWindowActivity;


public class OldComputerTheme extends BaseTheme {
    private static final String THEME_ID = "old_computer";
    private static final @StringRes int THEME_TITLE_STRING_RES = R.string.theme_name_old_computer;


    @Override
    public void apply(BaseWindowActivity activity) {
        super.apply(activity);
        activity.setTheme(activity.isHomeActivity() ? R.style.AppThemeOldComputerHome : R.style.AppThemeOldComputer);
        activity.setContentView(R.layout.base_window_layout_old_computer);

        this.registerExitListener(activity, activity.isHomeActivity());
    }

    @Override
    public void applyAccentColor(BaseWindowActivity activity, @ColorInt int color) {
    }

    @Override
    protected void configureDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    @Override
    public String getThemeID() {
        return THEME_ID;
    }

    @Override
    public CharSequence getThemeTitle(Context context) {
        return context.getString(THEME_TITLE_STRING_RES);
    }

    @Override
    protected boolean hasFooter() {
        return false;
    }

    @Override
    public boolean supportsAccentColor() {
        return false;
    }

    @Override
    public RemoteViews createRemoteViewsForWidget(Context context, PendingIntent pendingIntent) {
        int layoutId = R.layout.widget_launcher_old_computer;
        RemoteViews ret =  new RemoteViews(context.getPackageName(), layoutId);
        ret.setOnClickPendingIntent(R.id.widgetLauncherRootLinearLayout, pendingIntent);
        return ret;
    }

    @Override
    public int getDefaultAccentColor(Context context) {
        return 0;
    }
}
