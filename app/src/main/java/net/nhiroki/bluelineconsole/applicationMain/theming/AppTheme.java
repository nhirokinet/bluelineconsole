package net.nhiroki.bluelineconsole.applicationMain.theming;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.LayoutRes;

import net.nhiroki.bluelineconsole.applicationMain.BaseWindowActivity;


public interface AppTheme {
    void configureDarkMode(Context context);

    void apply(Activity activity, boolean iAmHomeActivity, boolean smallWindow);

    void applyAccentColor(Activity activity, int color);

    String getThemeID();

    CharSequence getThemeTitle(Context context);

    boolean hasFooter();

    void onCreateFinal(BaseWindowActivity activity);

    void enableWindowAnimationForElement(BaseWindowActivity activity);

    void disableWindowAnimationForElement(BaseWindowActivity activity);

    boolean supportsAccentColor();

    void changeBaseWindowElementSizeForAnimation(Activity activity, boolean visible, boolean smallWindow);

    @LayoutRes int getLauncherWidgetLayoutID(Context context);
}
