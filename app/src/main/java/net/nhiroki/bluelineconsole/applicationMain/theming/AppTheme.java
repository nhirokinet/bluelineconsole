package net.nhiroki.bluelineconsole.applicationMain.theming;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewStub;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;

import net.nhiroki.bluelineconsole.applicationMain.BaseWindowActivity;


public interface AppTheme {
    void configureDarkMode(Context context);

    void apply(Activity activity, boolean iAmHomeActivity, boolean smallWindow);

    void applyAccentColor(Activity activity, int color);

    String getThemeID();

    CharSequence getThemeTitle(Context context);

    void onCreateFinal(BaseWindowActivity activity);

    void enableWindowAnimationForElement(BaseWindowActivity activity);

    void disableWindowAnimationForElement(BaseWindowActivity activity);

    boolean supportsAccentColor();

    void changeBaseWindowElementSizeForAnimation(BaseWindowActivity activity, boolean visible, boolean smallWindow);

    @LayoutRes int getLauncherWidgetLayoutID(Context context);

    @IdRes int getLauncherWidgetRootLayoutID();

    double getWindowBodyAvailableHeight(Activity activity);

    void setHeaderFooterTexts(Activity activity, CharSequence headerText, CharSequence footerText);

    ViewStub findMainViewStub(Activity activity);

    View findVisibleRootView(Activity activity);

    View findWholeDisplayView(Activity activity);

    void setWindowLocationGravity(int gravity, Activity activity);

    void setOnTouchListenerForTitleBar(View.OnTouchListener onTouchListenerForTitleBar, Activity activity);

    void setWindowBoundarySize(int widthMode, int windowNestStep, Activity activity);
}
