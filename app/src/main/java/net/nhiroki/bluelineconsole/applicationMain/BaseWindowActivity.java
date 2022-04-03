package net.nhiroki.bluelineconsole.applicationMain;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.theming.AppTheme;
import net.nhiroki.bluelineconsole.applicationMain.theming.AppThemeDirectory;


public class BaseWindowActivity extends AppCompatActivity {
    protected boolean iAmHomeActivity = false;

    private final @LayoutRes int mainLayoutResID;
    private final boolean smallWindow;
    private AppTheme currentTheme;

    private boolean animationHasBeenEnabled = false;

    public static final String PREF_NAME_ACCENT_COLOR = "pref_accent_color";
    public static final String PREF_VALUE_ACCENT_COLOR_THEME_DEFAULT = "theme_default";
    public static final String PREF_VALUE_ACCENT_COLOR_PREFIX_COLOR = "color";

    public static final String PREF_NAME_ANIMATION = "pref_appearance_animation";


    /**
     * @param mainLayoutResID Layout resource to be put in the window
     * @param smallWindow Layout will be WRAP_CONTENT if set. Only for changeBaseWindowElementSize. If you do not use changeBaseWindowElementSize, set arbitrary value.
     */
    protected BaseWindowActivity(@LayoutRes int mainLayoutResID, boolean smallWindow) {
        super();
        this.mainLayoutResID = mainLayoutResID;
        this.smallWindow = smallWindow;
    }

    public void finishIfNotHome() {
        if (! this.iAmHomeActivity) {
            this.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.currentTheme = AppThemeDirectory.loadAppTheme(this);

        this.currentTheme.configureDarkMode(this);

        super.onCreate(savedInstanceState);

        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        this.currentTheme.apply(this, this.iAmHomeActivity, this.smallWindow);

        ViewStub mainViewStub = this.findViewById(R.id.baseWindowMainViewStub);
        mainViewStub.setLayoutResource(this.mainLayoutResID);
        mainViewStub.inflate();

        if (! this.iAmHomeActivity) {
            this.findViewById(R.id.baseWindowMainLayoutRoot).setOnClickListener(new ExitOnClickListener());
        }
        ((LinearLayout)findViewById(R.id.baseWindowMainLinearLayout)).getChildAt(0).setOnClickListener(null);

        if (this.currentTheme.hasFooter()) {
            // Decrease topMargin (which is already negative) by 1 physical pixel to fill the gap. See the comment in base_window_layout.xml .
            View mainFooterWrapper = findViewById(R.id.baseWindowFooterWrapper);
            ViewGroup.MarginLayoutParams mainFooterWrapperLayoutParam = (ViewGroup.MarginLayoutParams) mainFooterWrapper.getLayoutParams();
            mainFooterWrapperLayoutParam.setMargins(
                    mainFooterWrapperLayoutParam.leftMargin,
                    mainFooterWrapperLayoutParam.topMargin - 1,
                    mainFooterWrapperLayoutParam.rightMargin,
                    mainFooterWrapperLayoutParam.bottomMargin
            );
            mainFooterWrapper.setLayoutParams(mainFooterWrapperLayoutParam);
        }

        // TitleBarDragOnTouchListener has some state, it is safer to create different instance
        this.findViewById(R.id.baseWindowHeaderWrapper).setOnTouchListener(new TitleBarDragOnTouchListener());

        this.currentTheme.onCreateFinal(this);
    }

    public class TitleBarDragOnTouchListener implements View.OnTouchListener {
        private float paddingLeftOffset = (float) 0.0;
        private float paddingTopOffset = (float) 0.0;
        private float paddingRightOffset = (float) 0.0;
        private float paddingBottomOffset = (float) 0.0;

        // Click never happens
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    View window = BaseWindowActivity.this.findViewById(R.id.baseWindowRootLinearLayout);
                    paddingLeftOffset = window.getPaddingLeft() - event.getRawX();
                    paddingTopOffset = window.getPaddingTop() - event.getRawY();
                    paddingRightOffset = window.getPaddingRight() + event.getRawX();
                    paddingBottomOffset = window.getPaddingBottom() + event.getRawY();

                    BaseWindowActivity.this.disableWindowAnimationForElements();
                    return true;

                }
                case MotionEvent.ACTION_MOVE: {
                    View window = BaseWindowActivity.this.findViewById(R.id.baseWindowRootLinearLayout);

                    window.setPadding((int) (paddingLeftOffset + event.getRawX()), (int) (paddingTopOffset + event.getRawY()),
                            (int) (paddingRightOffset - event.getRawX()), (int) (paddingBottomOffset - event.getRawY()));

                    return true;

                }
                case MotionEvent.ACTION_UP: {
                    View window = BaseWindowActivity.this.findViewById(R.id.baseWindowRootLinearLayout);

                    window.setPadding((int) (paddingLeftOffset + event.getRawX()), (int) (paddingTopOffset + event.getRawY()),
                            (int) (paddingRightOffset - event.getRawX()), (int) (paddingBottomOffset - event.getRawY()));

                    if (BaseWindowActivity.this.getAnimationEnabledPreferenceValue()) {
                        if (BaseWindowActivity.this.animationHasBeenEnabled) {
                            BaseWindowActivity.this.enableWindowAnimationForElements();
                        }
                    }
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        final boolean animationEnabledBySetting = this.getAnimationEnabledPreferenceValue();

        if (animationEnabledBySetting) {
            if (this.animationHasBeenEnabled) {
                this.enableWindowAnimationForElements();
            }
        } else {
            this.disableWindowAnimationForElements();
        }

        findViewById(R.id.baseWindowMainLayoutRoot).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousHeight = -1;

            @Override
            public void onGlobalLayout() {
                int rootHeight = findViewById(R.id.baseWindowMainLayoutRoot).getHeight();

                if (previousHeight == rootHeight) {
                    return;
                }

                previousHeight = rootHeight;

                BaseWindowActivity.this.onHeightChange();
            }
        });

        this.onAccentColorChanged();
    }

    protected boolean themeStateMatchesConfig() {
        return this.currentTheme.getThemeID().equals(AppThemeDirectory.loadAppTheme(this).getThemeID());
    }

    protected CharSequence getCurrentThemeName() {
        return this.currentTheme.getThemeTitle(this);
    }

    protected boolean themeSupportsAccentColorChange() {
        return this.currentTheme.supportsAccentColor();
    }

    protected int getAccentColor() {
        String accentColorPreference = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME_ACCENT_COLOR, PREF_VALUE_ACCENT_COLOR_THEME_DEFAULT);

        final int color;

        if (accentColorPreference.equals(PREF_VALUE_ACCENT_COLOR_THEME_DEFAULT)) {
            TypedValue accentColorFromTheme = new TypedValue();
            this.getTheme().resolveAttribute(R.attr.bluelineconsoleAccentColor, accentColorFromTheme, true);

            color = accentColorFromTheme.data;

        } else if(accentColorPreference.startsWith(PREF_VALUE_ACCENT_COLOR_PREFIX_COLOR + "-")) {
            String[] colorStringSplit = accentColorPreference.split("-");

            int red = Integer.parseInt(colorStringSplit[1]);
            int green = Integer.parseInt(colorStringSplit[2]);
            int blue = Integer.parseInt(colorStringSplit[3]);

            color = (255 << 24) | (red << 16) | (green << 8) | blue;

        } else {
            TypedValue accentColorFromTheme = new TypedValue();
            this.getTheme().resolveAttribute(R.attr.bluelineconsoleAccentColor, accentColorFromTheme, true);

            color = accentColorFromTheme.data;
        }

        return color;
    }

    @CallSuper
    protected void onAccentColorChanged() {
        this.applyAccentColor(this.getAccentColor());
    }

    @CallSuper
    protected void applyAccentColor(int color) {
        this.currentTheme.applyAccentColor(this, color);
    }

    protected void onHeightChange() {}

    protected static final int ROOT_WINDOW_FULL_WIDTH_IN_MOBILE = 1;
    protected static final int ROOT_WINDOW_ALWAYS_HORIZONTAL_MARGIN = 2;
    protected static final int ROOT_WINDOW_FULL_WIDTH_ALWAYS = 3;

    protected void setWindowBoundarySize(int widthMode, int windowNestStep) {
        final int baseHorizontalMarginInPixels = (int)(8 * windowNestStep * getResources().getDisplayMetrics().density);
        final int baseVerticalMarginInPixels = (int)(24 * windowNestStep * getResources().getDisplayMetrics().density);

        if (widthMode == ROOT_WINDOW_FULL_WIDTH_ALWAYS) {
            findViewById(R.id.baseWindowRootLinearLayout).setPadding(baseHorizontalMarginInPixels, baseVerticalMarginInPixels, baseHorizontalMarginInPixels, baseVerticalMarginInPixels);

        } else {
            final Point displaySize = new Point();
            this.getWindowManager().getDefaultDisplay().getSize(displaySize);

            final int maxPanelWidth;

            maxPanelWidth = (int) (600 * getResources().getDisplayMetrics().density);

            final int panelWidth;

            if (widthMode == ROOT_WINDOW_ALWAYS_HORIZONTAL_MARGIN) {
                panelWidth = Math.min((int) (displaySize.x * ((displaySize.x < displaySize.y) ? 0.87 : 0.7) - baseHorizontalMarginInPixels), maxPanelWidth - baseHorizontalMarginInPixels);
            } else {
                panelWidth = Math.min(maxPanelWidth - baseHorizontalMarginInPixels, displaySize.x - baseHorizontalMarginInPixels);
            }

            final int horizontal = Math.max((displaySize.x - panelWidth) / 2, baseHorizontalMarginInPixels);
            findViewById(R.id.baseWindowRootLinearLayout).setPadding(horizontal, baseVerticalMarginInPixels, horizontal, baseVerticalMarginInPixels);
        }
    }

    @SuppressLint("SetTextI18n")
    protected void setHeaderFooterTexts(CharSequence headerText, CharSequence footerText) {
        if (this.currentTheme.hasFooter()) {
            ((TextView) findViewById(R.id.baseWindowMainHeaderTextView)).setText(headerText);
            ((TextView) findViewById(R.id.baseWindowMainFooterTextView)).setText(footerText == null ? headerText : footerText);
        } else {
            ((TextView) findViewById(R.id.baseWindowMainHeaderTextView)).setText(footerText == null ? headerText : (headerText + " " + footerText));
        }
    }

    protected void setWindowLocationGravity(int gravity) {
        ((LinearLayout)findViewById(R.id.baseWindowRootLinearLayout)).setGravity(gravity);
    }

    protected void enableBaseWindowAnimation() {
        this.animationHasBeenEnabled = true;
        if (this.getAnimationEnabledPreferenceValue()) {
            this.enableWindowAnimationForElements();
        }
    }

    protected boolean getAnimationEnabledPreferenceValue() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_NAME_ANIMATION, true);
    }

    public void enableWindowAnimationForEachViewGroup(ViewGroup viewGroup) {
        LayoutTransition lt = viewGroup.getLayoutTransition();
        lt.enableTransitionType(LayoutTransition.CHANGING);
        viewGroup.setLayoutTransition(lt);
    }

    public void disableWindowAnimationForEachViewGroup(ViewGroup viewGroup) {
        LayoutTransition lt = viewGroup.getLayoutTransition();
        lt.disableTransitionType(LayoutTransition.CHANGING);
        viewGroup.setLayoutTransition(lt);
    }

    @CallSuper
    protected void enableWindowAnimationForElements() {
        enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowMainLayoutRoot));
        enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowRootLinearLayout));
        enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowHeaderWrapper));
        enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowMainLinearLayout));
        enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowFooterWrapper));
        this.currentTheme.enableWindowAnimationForElement(this);
    }

    @CallSuper
    protected void disableWindowAnimationForElements() {
        disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowMainLayoutRoot));
        disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowRootLinearLayout));
        disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowHeaderWrapper));
        disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowMainLinearLayout));
        disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowFooterWrapper));
        this.currentTheme.disableWindowAnimationForElement(this);
    }

    protected double getWindowBodyAvailableHeight() {
        return findViewById(R.id.baseWindowMainLayoutRoot).getHeight() - findViewById(R.id.baseWindowHeaderWrapper).getHeight() * (this.currentTheme.hasFooter() ? 2.0 : 1.0);
    }

    private class ExitOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }

    protected void changeBaseWindowElementSizeForAnimation(boolean visible) {
        LinearLayout centerLL = findViewById(R.id.baseWindowMainLinearLayout);
        View mainLL = centerLL.getChildAt(0);
        LinearLayout.LayoutParams mainLP = (LinearLayout.LayoutParams) mainLL.getLayoutParams();

        // This is for animation, so if animation is disabled no reason to make invisible
        if (visible || !this.getAnimationEnabledPreferenceValue()) {
            mainLP.width = LinearLayout.LayoutParams.MATCH_PARENT;
            mainLP.height = this.smallWindow ? LinearLayout.LayoutParams.WRAP_CONTENT : LinearLayout.LayoutParams.MATCH_PARENT;
            mainLL.setLayoutParams(mainLP);

            LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
            centerLP.height = this.smallWindow ? LinearLayout.LayoutParams.WRAP_CONTENT : LinearLayout.LayoutParams.MATCH_PARENT;
            centerLL.setLayoutParams(centerLP);

        } else {
            mainLP.width = (int) (200 * getResources().getDisplayMetrics().density + 0.5);
            mainLP.height = 0;
            mainLL.setLayoutParams(mainLP);

            LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
            centerLP.height = 0;
            centerLL.setLayoutParams(centerLP);
        }

        this.currentTheme.changeBaseWindowElementSizeForAnimation(this, visible || !this.getAnimationEnabledPreferenceValue(), this.smallWindow);
    }
}
