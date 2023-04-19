package net.nhiroki.bluelineconsole.applicationMain.theming;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.GravityInt;
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

    /** Apply theme. Called from onCreate() soon after normal onCreate() is called
     *
     * @param activity BaseWindowActivity to apply theme
     */
    void apply(BaseWindowActivity activity);
    void beforeCreateActivity(BaseWindowActivity activity);
    void onCreateFinal(BaseWindowActivity activity);
    void enableWindowAnimationForElement(BaseWindowActivity activity);
    void disableWindowAnimationForElement(BaseWindowActivity activity);
    /** Called each time an Activity has to know accent color
     * (whether Activity is resumed or accent color is changed)
     *
     * @param activity BaseWindowActivity to apply theme
     * @param color New accent color
     */
    void applyAccentColor(BaseWindowActivity activity, @ColorInt int color);
    void changeBaseWindowElementSizeForAnimation(BaseWindowActivity activity, boolean visible);
    void setHeaderFooterTexts(BaseWindowActivity activity, CharSequence headerText, CharSequence footerText);
    void setWindowBoundarySize(BaseWindowActivity activity, int widthMode, int windowNestStep);
    void setPayloadLayout(BaseWindowActivity activity, @LayoutRes int layout);


    // Maybe need some cleanup
    double getWindowBodyAvailableHeight(BaseWindowActivity activity);
    View findVisibleRootView(BaseWindowActivity activity);
    View findWholeDisplayView(BaseWindowActivity activity);
    void setWindowLocationGravity(BaseWindowActivity activity, @GravityInt int gravity);
    void setOnTouchListenerForTitleBar(BaseWindowActivity activity, View.OnTouchListener onTouchListenerForTitleBar);


    // Behavior should be reconsidered
    @LayoutRes int getLauncherWidgetLayoutID(Context context);
    @IdRes int getLauncherWidgetRootLayoutID();
}
