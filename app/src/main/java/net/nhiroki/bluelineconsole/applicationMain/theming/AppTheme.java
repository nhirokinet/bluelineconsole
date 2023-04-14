package net.nhiroki.bluelineconsole.applicationMain.theming;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;

import net.nhiroki.bluelineconsole.applicationMain.BaseWindowActivity;


public interface AppTheme {
    // Theme metadata
    String getThemeID();
    boolean supportsAccentColor();

    // Theme metadata using Context
    CharSequence getThemeTitle(Context context);
    @ColorInt int getDefaultAccentColor(Context context);


    // Handlers
    void beforeCreateActivity(BaseWindowActivity activity);
    void onCreateFinal(BaseWindowActivity activity);
    void enableWindowAnimationForElement(BaseWindowActivity activity);
    void disableWindowAnimationForElement(BaseWindowActivity activity);

    /** Called each time an A    void applyAccentColor(BaseWindowActivity activity, @ColorInt int color);ctivity has to know accent color
     * (whether Activity is resumed or accent color is changed)
     */
    void applyAccentColor(BaseWindowActivity activity, @ColorInt int color);


    // Unclassified yet    void applyAccentColor(BaseWindowActivity activity, @ColorInt int color);

    void apply(Activity activity, boolean iAmHomeActivity, boolean smallWindow);

    void changeBaseWindowElementSizeForAnimation(BaseWindowActivity activity, boolean visible, boolean smallWindow);

    @LayoutRes int getLauncherWidgetLayoutID(Context context);

    @IdRes int getLauncherWidgetRootLayoutID();

    double getWindowBodyAvailableHeight(Activity activity);

    void setHeaderFooterTexts(Activity activity, CharSequence headerText, CharSequence footerText);

    void setPayloadLayout(@LayoutRes int layout, Activity activity);

    View findVisibleRootView(Activity activity);

    View findWholeDisplayView(Activity activity);

    void setWindowLocationGravity(int gravity, Activity activity);

    void setOnTouchListenerForTitleBar(View.OnTouchListener onTouchListenerForTitleBar, Activity activity);

    void setWindowBoundarySize(int widthMode, int windowNestStep, Activity activity);
}
