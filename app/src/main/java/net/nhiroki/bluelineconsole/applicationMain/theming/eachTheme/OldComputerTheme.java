package net.nhiroki.bluelineconsole.applicationMain.theming.eachTheme;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;

import net.nhiroki.bluelineconsole.R;


public class OldComputerTheme extends BaseTheme {
    private static final String THEME_ID = "old_computer";
    private static final @StringRes int THEME_TITLE_STRING_RES = R.string.theme_name_old_computer;


    @Override
    public void apply(Activity activity, boolean iAmHomeActivity, boolean smallWindow) {
        super.apply(activity, iAmHomeActivity, smallWindow);
        activity.setTheme(iAmHomeActivity ? R.style.AppThemeOldComputerHome : R.style.AppThemeOldComputer);
        activity.setContentView(R.layout.base_window_layout_old_computer);

        this.registerExitListener(activity, iAmHomeActivity);
    }

    @Override
    public void applyAccentColor(Activity activity, int color) {
    }

    @Override
    public void configureDarkMode() {
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
    public int getLauncherWidgetLayoutID(Context context) {
        return R.layout.widget_launcher_old_computer;
    }

    @Override
    public int getDefaultAccentColor(Context context) {
        return 0;
    }
}
