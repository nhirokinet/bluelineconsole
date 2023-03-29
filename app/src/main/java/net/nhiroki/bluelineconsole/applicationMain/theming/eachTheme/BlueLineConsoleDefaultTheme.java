package net.nhiroki.bluelineconsole.applicationMain.theming.eachTheme;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.drawable.DrawableCompat;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.BaseWindowActivity;

public class BlueLineConsoleDefaultTheme extends BaseTheme {
    private static final String THEME_ID = "default";
    private static final @StringRes int THEME_TITLE_STRING_RES = R.string.theme_name_default;


    @Override
    public void configureDarkMode() {
        // AppCompatDelegate.setLocalNightMode, behaves strangely; sometimes activity does not come up.
        // AppCompatDelegate.setDefaultNightMode(), on the other hand, seems to crash other activities already launched;
        // Currently only the solution I could find was finishing all activities before calling setDefaultNightMode.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    @Override
    public void apply(Activity activity, boolean iAmHomeActivity, boolean smallWindow) {
        super.apply(activity, iAmHomeActivity, smallWindow);

        if (this.isDarkMode(activity)) {
            activity.setTheme(iAmHomeActivity ? R.style.AppThemeBlueLineConsoleDarkHome : R.style.AppThemeBlueLineConsoleDark);
        } else {
            activity.setTheme(iAmHomeActivity ? R.style.AppThemeBlueLineConsoleHome : R.style.AppThemeBlueLineConsole);
        }
        activity.setContentView(R.layout.base_window_layout_default);

        this.setFooterMargin(activity);

        this.registerExitListener(activity, iAmHomeActivity);
    }

    @Override
    public void applyAccentColor(Activity activity, @ColorInt int color) {
        DrawableCompat.setTint(activity.findViewById(R.id.baseWindowDefaultThemeHeaderAccent).getBackground(), color);
        DrawableCompat.setTint(activity.findViewById(R.id.baseWindowDefaultThemeFooterAccent).getBackground(), color);
        activity.findViewById(R.id.baseWindowDefaultThemeHeaderStartAccent).setBackgroundColor(color);
        activity.findViewById(R.id.baseWindowDefaultThemeFooterEndAccent).setBackgroundColor(color);
        activity.findViewById(R.id.baseWindowDefaultThemeMainLinearLayoutOuter).setBackgroundColor(color);
        activity.findViewById(R.id.baseWindowDefaultThemeMainLayoutTopEdge).setBackgroundColor(color);
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
    public void onCreateFinal(BaseWindowActivity activity) {
        super.onCreateFinal(activity);

        activity.findViewById(R.id.baseWindowDefaultThemeMainLayoutTopEdge).setOnTouchListener(activity.new TitleBarDragOnTouchListener());

        // Make setTint() in onResume() to work
        activity.findViewById(R.id.baseWindowDefaultThemeHeaderAccent).getBackground().mutate();
        activity.findViewById(R.id.baseWindowDefaultThemeFooterAccent).getBackground().mutate();
    }

    @Override
    public void enableWindowAnimationForElement(BaseWindowActivity activity) {
        super.enableWindowAnimationForElement(activity);
        activity.enableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowDefaultThemeMainLinearLayoutOuter));
        activity.enableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowDefaultThemeHeaderStartAccent));
        activity.enableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowDefaultThemeFooterEndAccent));
        activity.enableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowDefaultThemeHeaderImagePart));
        activity.enableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowDefaultThemeFooterImagePart));
    }

    @Override
    public void disableWindowAnimationForElement(BaseWindowActivity activity) {
        super.disableWindowAnimationForElement(activity);
        activity.disableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowDefaultThemeMainLinearLayoutOuter));
        activity.disableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowDefaultThemeHeaderStartAccent));
        activity.disableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowDefaultThemeFooterEndAccent));
        activity.disableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowDefaultThemeHeaderImagePart));
        activity.disableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowDefaultThemeFooterImagePart));
    }

    @Override
    public boolean supportsAccentColor() {
        return true;
    }

    @Override
    public void changeBaseWindowElementSizeForAnimation(BaseWindowActivity activity, boolean visible, boolean smallWindow) {
        super.changeBaseWindowElementSizeForAnimation(activity, visible, smallWindow);

        final View centerLLOuter = activity.findViewById(R.id.baseWindowDefaultThemeMainLinearLayoutOuter);
        final LinearLayout.LayoutParams centerLPOuter = (LinearLayout.LayoutParams) centerLLOuter.getLayoutParams();
        if (visible) {
            centerLPOuter.height = smallWindow ? LinearLayout.LayoutParams.WRAP_CONTENT : LinearLayout.LayoutParams.MATCH_PARENT;
        } else {
            centerLPOuter.height = 0;
        }
        centerLLOuter.setLayoutParams(centerLPOuter);
    }

    @Override
    public int getLauncherWidgetLayoutID(Context context) {
        return this.isDarkMode(context) ? R.layout.widget_launcher_dark_theme : R.layout.widget_launcher_default_theme;
    }

    @Override
    public @ColorInt int getDefaultAccentColor(Context context) {
        TypedValue accentColorFromTheme = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.bluelineconsoleAccentColor, accentColorFromTheme, true);

        return accentColorFromTheme.data;
    }

    protected static boolean isSystemCurrentlyNightMode(Context context) {
        return Build.VERSION.SDK_INT >= 29 && (context.getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    protected boolean isDarkMode(Context context) {
        return isSystemCurrentlyNightMode(context);
    }
}
