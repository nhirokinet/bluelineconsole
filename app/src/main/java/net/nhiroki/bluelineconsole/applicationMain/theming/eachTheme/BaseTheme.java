package net.nhiroki.bluelineconsole.applicationMain.theming.eachTheme;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.GravityInt;
import androidx.annotation.IdRes;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.BaseWindowActivity;
import net.nhiroki.bluelineconsole.applicationMain.theming.AppTheme;


public abstract class BaseTheme implements AppTheme {
    @CallSuper
    @Override
    public void apply(BaseWindowActivity activity) {
        activity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @CallSuper
    protected void registerExitListener(Activity activity, boolean iAmHomeActivity) {
        if (! iAmHomeActivity) {
            activity.findViewById(R.id.baseWindowMainLayoutRoot).setOnClickListener(new ExitOnClickListener(activity));
        }
    }

    protected boolean hasFooter() {
        return false;
    }

    @CallSuper
    protected void setFooterMargin(Activity activity) {
        if (this.hasFooter()) {
            // Decrease topMargin (which is already negative) by 1 physical pixel to fill the gap. See the comment in base_window_layout.xml .
            View mainFooterWrapper = activity.findViewById(R.id.baseWindowFooterWrapper);
            ViewGroup.MarginLayoutParams mainFooterWrapperLayoutParam = (ViewGroup.MarginLayoutParams) mainFooterWrapper.getLayoutParams();
            mainFooterWrapperLayoutParam.setMargins(
                    mainFooterWrapperLayoutParam.leftMargin,
                    mainFooterWrapperLayoutParam.topMargin - 1,
                    mainFooterWrapperLayoutParam.rightMargin,
                    mainFooterWrapperLayoutParam.bottomMargin
            );
            mainFooterWrapper.setLayoutParams(mainFooterWrapperLayoutParam);
        }
    }

    protected abstract void configureDarkMode();

    // Currently @activity is not used, but keeping interface so that this won't be a problem later
    public void beforeCreateActivity(BaseWindowActivity activity) {
        this.configureDarkMode();
    }

    @Override
    public double getWindowBodyAvailableHeight(BaseWindowActivity activity) {
        return activity.findViewById(R.id.baseWindowMainLayoutRoot).getHeight() - activity.findViewById(R.id.baseWindowHeaderWrapper).getHeight() * (this.hasFooter() ? 2.0 : 1.0);
    }

    @CallSuper
    @Override
    @SuppressLint("SetTextI18n")
    public void setHeaderFooterTexts(BaseWindowActivity activity, CharSequence headerText, CharSequence footerText) {
        if (this.hasFooter()) {
            ((TextView) activity.findViewById(R.id.baseWindowMainHeaderTextView)).setText(headerText);
            ((TextView) activity.findViewById(R.id.baseWindowMainFooterTextView)).setText(footerText == null ? headerText : footerText);
        } else {
            ((TextView) activity.findViewById(R.id.baseWindowMainHeaderTextView)).setText(footerText == null ? headerText : (headerText + " " + footerText));
        }
    }

    @CallSuper
    @Override
    public void enableWindowAnimationForElement(BaseWindowActivity activity) {
        activity.enableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowMainLayoutRoot));
        activity.enableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowRootLinearLayout));
        activity.enableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowHeaderWrapper));
        activity.enableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowMainLinearLayout));
        activity.enableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowFooterWrapper));
    }

    @CallSuper
    @Override
    public void disableWindowAnimationForElement(BaseWindowActivity activity) {
        activity.disableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowMainLayoutRoot));
        activity.disableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowRootLinearLayout));
        activity.disableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowHeaderWrapper));
        activity.disableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowMainLinearLayout));
        activity.disableWindowAnimationForEachViewGroup(activity.findViewById(R.id.baseWindowFooterWrapper));
    }

    @CallSuper
    @Override
    public void changeBaseWindowElementSizeForAnimation(BaseWindowActivity activity, boolean visible) {
        final boolean smallWindow = activity.isSmallWindow();
        LinearLayout centerLL = activity.findViewById(R.id.baseWindowMainLinearLayout);
        View mainLL = centerLL.getChildAt(0);
        LinearLayout.LayoutParams mainLP = (LinearLayout.LayoutParams) mainLL.getLayoutParams();

        // This is for animation, so if animation is disabled no reason to make invisible
        if (visible || !activity.getAnimationEnabledPreferenceValue()) {
            mainLP.width = LinearLayout.LayoutParams.MATCH_PARENT;
            mainLP.height = smallWindow ? LinearLayout.LayoutParams.WRAP_CONTENT : LinearLayout.LayoutParams.MATCH_PARENT;
            mainLL.setLayoutParams(mainLP);

            LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
            centerLP.height = smallWindow ? LinearLayout.LayoutParams.WRAP_CONTENT : LinearLayout.LayoutParams.MATCH_PARENT;
            centerLL.setLayoutParams(centerLP);

        } else {
            mainLP.width = (int) (200 * activity.getResources().getDisplayMetrics().density + 0.5);
            mainLP.height = 0;
            mainLL.setLayoutParams(mainLP);

            LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
            centerLP.height = 0;
            centerLL.setLayoutParams(centerLP);
        }
    }

    @CallSuper
    @Override
    public void setPayloadLayout(BaseWindowActivity activity, int layout) {
        ViewStub mainViewStub = activity.findViewById(R.id.baseWindowMainViewStub);
        mainViewStub.setLayoutResource(layout);
        mainViewStub.inflate();
    }

    @CallSuper
    @Override
    public void setWindowLocationGravity(BaseWindowActivity activity, @GravityInt int gravity) {
        ((LinearLayout)activity.findViewById(R.id.baseWindowRootLinearLayout)).setGravity(gravity);
    }

    @Override
    public View findVisibleRootView(BaseWindowActivity activity) {
        return activity.findViewById(R.id.baseWindowRootLinearLayout);
    }

    @Override
    public View findWholeDisplayView(BaseWindowActivity activity) {
        return activity.findViewById(R.id.baseWindowMainLayoutRoot);
    }

    @CallSuper
    @Override
    public void onCreateFinal(BaseWindowActivity activity) {
        ((LinearLayout)activity.findViewById(R.id.baseWindowMainLinearLayout)).getChildAt(0).setOnClickListener(null);
    }

    @CallSuper
    @Override
    public void setOnTouchListenerForTitleBar(BaseWindowActivity activity, View.OnTouchListener onTouchListenerForTitleBar) {
        activity.findViewById(R.id.baseWindowHeaderWrapper).setOnTouchListener(onTouchListenerForTitleBar);
    }

    @CallSuper
    @Override
    public void setWindowBoundarySize(BaseWindowActivity activity, int widthMode, int windowNestStep) {
        final int baseHorizontalMarginInPixels = (int)(8 * windowNestStep * activity.getResources().getDisplayMetrics().density);
        final int baseVerticalMarginInPixels = (int)(24 * windowNestStep * activity.getResources().getDisplayMetrics().density);

        if (widthMode == BaseWindowActivity.ROOT_WINDOW_FULL_WIDTH_ALWAYS) {
            this.findVisibleRootView(activity).setPadding(baseHorizontalMarginInPixels, baseVerticalMarginInPixels, baseHorizontalMarginInPixels, baseVerticalMarginInPixels);

        } else {
            final Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);

            final int maxPanelWidth;

            maxPanelWidth = (int) (600 * activity.getResources().getDisplayMetrics().density);

            final int panelWidth;

            if (widthMode == BaseWindowActivity.ROOT_WINDOW_ALWAYS_HORIZONTAL_MARGIN) {
                panelWidth = Math.min((int) (displaySize.x * ((displaySize.x < displaySize.y) ? 0.87 : 0.7) - baseHorizontalMarginInPixels), maxPanelWidth - baseHorizontalMarginInPixels);
            } else {
                panelWidth = Math.min(maxPanelWidth - baseHorizontalMarginInPixels, displaySize.x - baseHorizontalMarginInPixels);
            }

            final int horizontal = Math.max((displaySize.x - panelWidth) / 2, baseHorizontalMarginInPixels);
            this.findVisibleRootView(activity).setPadding(horizontal, baseVerticalMarginInPixels, horizontal, baseVerticalMarginInPixels);
        }
    }

    @Override
    public @IdRes int getLauncherWidgetRootLayoutID() {
        return R.id.widgetLauncherRootLinearLayout;
    }

    private static class ExitOnClickListener implements View.OnClickListener {
        private final Activity activity;

        ExitOnClickListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onClick(View v) {
            activity.finish();
        }
    }
}
