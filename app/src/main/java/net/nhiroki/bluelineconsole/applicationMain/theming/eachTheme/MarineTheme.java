package net.nhiroki.bluelineconsole.applicationMain.theming.eachTheme;

import android.app.Activity;
import android.content.Context;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.BaseWindowActivity;


public class MarineTheme extends BaseTheme {
    private static final String THEME_ID = "marine";
    private static final @StringRes int THEME_TITLE_STRING_RES = R.string.theme_name_marine;

    @Override
    protected void configureDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public void apply(Activity activity, boolean iAmHomeActivity, boolean smallWindow) {
        super.apply(activity, iAmHomeActivity, smallWindow);

        activity.setTheme(iAmHomeActivity ? R.style.AppThemeMarineHome : R.style.AppThemeMarine);
        activity.setContentView(R.layout.base_window_layout_marine);

        LinearLayout centerLL = activity.findViewById(R.id.baseWindowIntermediateWrapper);
        LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
        centerLP.height = smallWindow ? LinearLayout.LayoutParams.WRAP_CONTENT : LinearLayout.LayoutParams.MATCH_PARENT;
        centerLL.setLayoutParams(centerLP);

        this.setFooterMargin(activity);

        this.registerExitListener(activity, iAmHomeActivity);
    }

    @Override
    public void applyAccentColor(BaseWindowActivity activity, @ColorInt int color) {
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
        return true;
    }

    @Override
    public boolean supportsAccentColor() {
        return false;
    }

    @Override
    public int getLauncherWidgetLayoutID(Context context) {
        return R.layout.widget_launcher_marine;
    }

    @Override
    public @ColorInt int getDefaultAccentColor(Context context) {
        return 0;
    }
}
