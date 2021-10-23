package net.nhiroki.bluelineconsole.applicationMain;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.drawable.DrawableCompat;

import net.nhiroki.bluelineconsole.R;


public class BaseWindowActivity extends AppCompatActivity {
    protected boolean _iAmHomeActivity = false;

    private final @LayoutRes int _mainLayoutResID;
    private final boolean _smallWindow;
    private boolean _comingBack = false;
    private String _currentTheme;
    private boolean _isDefaultLayOut = true;
    private boolean _hasFooter = true;

    private boolean _animationHasBeenEnabled = false;

    public static final String PREF_NAME_THEME = "pref_appearance_theme";
    public static final String PREF_VALUE_THEME_DEFAULT = "default";
    public static final String PREF_VALUE_THEME_LIGHT = "light";
    public static final String PREF_VALUE_THEME_DARK = "dark";
    public static final String PREF_VALUE_THEME_OLD_COMPUTER = "old_computer";
    public static final String PREF_VALUE_THEME_MARINE = "marine";

    public static final CharSequence[] PREF_THEME_ENTRY_VALUES = { PREF_VALUE_THEME_DEFAULT, PREF_VALUE_THEME_LIGHT, PREF_VALUE_THEME_DARK, PREF_VALUE_THEME_MARINE, PREF_VALUE_THEME_OLD_COMPUTER };
    public static CharSequence[] getPrefThemeEntries(Context context) {
        return new CharSequence[]{ context.getString(R.string.theme_name_default), context.getString(R.string.theme_name_light), context.getString(R.string.theme_name_dark),
                                   context.getString(R.string.theme_name_marine), context.getString(R.string.theme_name_old_computer), };
    }

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
        this._mainLayoutResID = mainLayoutResID;
        this._smallWindow = smallWindow;
    }

    public void finishIfNotHome() {
        if (! this._iAmHomeActivity) {
            this.finish();
        }
    }

    public static boolean isSystemCurrentlyNightMode(Context context) {
        return Build.VERSION.SDK_INT >= 29 && (context.getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    // preferenceThemeName can be modified for case onPreferenceChange() which is called just before th change
    public static String determineActualTheme(Context context, String preferenceThemeName) {
        String ret = preferenceThemeName;

        if (ret.equals(PREF_VALUE_THEME_DEFAULT)) {
            ret = isSystemCurrentlyNightMode(context) ? PREF_VALUE_THEME_DARK : PREF_VALUE_THEME_LIGHT;
        }

        return ret;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this._currentTheme = this.readThemeFromConfig();

        // AppCompatDelegate.setLocalNightMode, behaves strangely; sometimes activity does not come up.
        // AppCompatDelegate.setDefaultNightMode(), on the other hand, seems to crash other activities already launched;
        // Currently only the solution I could find was finishing all activities before calling setDefaultNightMode.
        switch (this._currentTheme) {
            case PREF_VALUE_THEME_DARK:
            case PREF_VALUE_THEME_OLD_COMPUTER:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case PREF_VALUE_THEME_LIGHT:
            case PREF_VALUE_THEME_MARINE:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case PREF_VALUE_THEME_DEFAULT:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        final String actualTheme = determineActualTheme(this, this._currentTheme);

        super.onCreate(savedInstanceState);


        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        switch (actualTheme) {
            case PREF_VALUE_THEME_DARK:
                this.setTheme(this._iAmHomeActivity ? R.style.AppThemeDarkHome : R.style.AppThemeDark);
                this.setContentView(R.layout.base_window_layout_default);
                break;
            case PREF_VALUE_THEME_MARINE:
                this.setTheme(this._iAmHomeActivity ? R.style.AppThemeMarineHome : R.style.AppThemeMarine);
                this.setContentView(R.layout.base_window_layout_marine);
                this._isDefaultLayOut = false;

                LinearLayout centerLL = findViewById(R.id.baseWindowIntermediateWrapper);
                LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
                centerLP.height = this._smallWindow ? LinearLayout.LayoutParams.WRAP_CONTENT : LinearLayout.LayoutParams.MATCH_PARENT;
                centerLL.setLayoutParams(centerLP);
                break;
            case PREF_VALUE_THEME_OLD_COMPUTER:
                this.setTheme(this._iAmHomeActivity ? R.style.AppThemeOldComputerHome : R.style.AppThemeOldComputer);
                this.setContentView(R.layout.base_window_layout_old_computer);
                this._isDefaultLayOut = false;
                this._hasFooter = false;
                break;
            case PREF_VALUE_THEME_LIGHT:
            default:
                this.setTheme(this._iAmHomeActivity ? R.style.AppThemeHome : R.style.AppTheme);
                this.setContentView(R.layout.base_window_layout_default);
                break;
        }

        ViewStub mainViewStub = this.findViewById(R.id.baseWindowMainViewStub);
        mainViewStub.setLayoutResource(this._mainLayoutResID);
        mainViewStub.inflate();

        if (! this._iAmHomeActivity) {
            this.findViewById(R.id.baseWindowMainLayoutRoot).setOnClickListener(new ExitOnClickListener());
        }
        ((LinearLayout)findViewById(R.id.baseWindowMainLinearLayout)).getChildAt(0).setOnClickListener(null);

        if (this._hasFooter) {
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
        if (this._isDefaultLayOut) {
            this.findViewById(R.id.baseWindowDefaultThemeMainLayoutTopEdge).setOnTouchListener(new TitleBarDragOnTouchListener());

            // Make setTint() in onResume() to work
            this.findViewById(R.id.baseWindowDefaultThemeHeaderAccent).getBackground().mutate();
            this.findViewById(R.id.baseWindowDefaultThemeFooterAccent).getBackground().mutate();
        }
    }

    private class TitleBarDragOnTouchListener implements View.OnTouchListener {
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
                        if (BaseWindowActivity.this._animationHasBeenEnabled) {
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

        this._comingBack = false;

        final boolean animationEnabledBySetting = this.getAnimationEnabledPreferenceValue();

        if (animationEnabledBySetting) {
            if (this._animationHasBeenEnabled) {
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

    @Override
    protected void onStop() {
        // This app should be as stateless as possible. When app disappears most activities should finish.
        super.onStop();
        if (! this._comingBack) {
            this.finish();
        }
    }

    protected String readThemeFromConfig() {
        return readThemeFromConfigStatic(this);
    }

    public static String readThemeFromConfigStatic(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_NAME_THEME, PREF_VALUE_THEME_DEFAULT);
    }

    protected String getCurrentTheme() {
        return this._currentTheme;
    }

    protected CharSequence getCurrentThemeName() {
        for (int i = 0; i < PREF_THEME_ENTRY_VALUES.length; ++i) {
            if (PREF_THEME_ENTRY_VALUES[i].equals(this._currentTheme)) {
                return BaseWindowActivity.getPrefThemeEntries(this)[i];
            }
        }
        return this.getString(R.string.theme_name_default);
    }

    protected void setComingBackFlag() {
        this._comingBack = true;
    }

    protected boolean themeSupportsAccentColorChange() {
        return this.getCurrentTheme().equals(PREF_VALUE_THEME_DARK) || this.getCurrentTheme().equals(PREF_VALUE_THEME_LIGHT) || this.getCurrentTheme().equals(PREF_VALUE_THEME_DEFAULT);
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
        if (this._isDefaultLayOut) {
            DrawableCompat.setTint(this.findViewById(R.id.baseWindowDefaultThemeHeaderAccent).getBackground(), color);
            DrawableCompat.setTint(this.findViewById(R.id.baseWindowDefaultThemeFooterAccent).getBackground(), color);
            this.findViewById(R.id.baseWindowDefaultThemeHeaderStartAccent).setBackgroundColor(color);
            this.findViewById(R.id.baseWindowDefaultThemeFooterEndAccent).setBackgroundColor(color);
            this.findViewById(R.id.baseWindowDefaultThemeMainLinearLayoutOuter).setBackgroundColor(color);
            this.findViewById(R.id.baseWindowDefaultThemeMainLayoutTopEdge).setBackgroundColor(color);
        }
    }

    protected void originalOnStop() {
        super.onStop();
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
        if (this._hasFooter) {
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
        this._animationHasBeenEnabled = true;
        if (this.getAnimationEnabledPreferenceValue()) {
            this.enableWindowAnimationForElements();
        }
    }

    protected boolean getAnimationEnabledPreferenceValue() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_NAME_ANIMATION, true);
    }

    protected void enableWindowAnimationForEachViewGroup(ViewGroup viewGroup) {
        LayoutTransition lt = viewGroup.getLayoutTransition();
        lt.enableTransitionType(LayoutTransition.CHANGING);
        viewGroup.setLayoutTransition(lt);
    }

    protected void disableWindowAnimationForEachViewGroup(ViewGroup viewGroup) {
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
        if (this._isDefaultLayOut) {
            enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowDefaultThemeMainLinearLayoutOuter));
            enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowDefaultThemeHeaderStartAccent));
            enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowDefaultThemeFooterEndAccent));
            enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowDefaultThemeHeaderImagePart));
            enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowDefaultThemeFooterImagePart));
        }
    }

    @CallSuper
    protected void disableWindowAnimationForElements() {
        disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowMainLayoutRoot));
        disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowRootLinearLayout));
        disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowHeaderWrapper));
        disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowMainLinearLayout));
        disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowFooterWrapper));
        if (this._isDefaultLayOut) {
            disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowDefaultThemeMainLinearLayoutOuter));
            disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowDefaultThemeHeaderStartAccent));
            disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowDefaultThemeFooterEndAccent));
            disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowDefaultThemeHeaderImagePart));
            disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.baseWindowDefaultThemeFooterImagePart));
        }
    }

    protected double getWindowBodyAvailableHeight() {
        return findViewById(R.id.baseWindowMainLayoutRoot).getHeight() - findViewById(R.id.baseWindowHeaderWrapper).getHeight() * (this._hasFooter ? 2.0 : 1.0);
    }

    private class ExitOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }

    protected void changeBaseWindowElementSizeForAnimation(boolean visible) {
        LinearLayout centerLL = findViewById(R.id.baseWindowMainLinearLayout);
        View centerLLOuter = findViewById(R.id.baseWindowDefaultThemeMainLinearLayoutOuter);
        View mainLL = centerLL.getChildAt(0);
        LinearLayout.LayoutParams mainLP = (LinearLayout.LayoutParams) mainLL.getLayoutParams();

        // This is for animation, so if animation is disabled no reason to make invisible
        if (visible || !this.getAnimationEnabledPreferenceValue()) {
            mainLP.width = LinearLayout.LayoutParams.MATCH_PARENT;
            mainLP.height = this._smallWindow ? LinearLayout.LayoutParams.WRAP_CONTENT : LinearLayout.LayoutParams.MATCH_PARENT;
            mainLL.setLayoutParams(mainLP);

            LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
            centerLP.height = this._smallWindow ? LinearLayout.LayoutParams.WRAP_CONTENT : LinearLayout.LayoutParams.MATCH_PARENT;
            centerLL.setLayoutParams(centerLP);

            if (! this.getCurrentTheme().equals(PREF_VALUE_THEME_OLD_COMPUTER) && ! this.getCurrentTheme().equals(PREF_VALUE_THEME_MARINE)) {
                LinearLayout.LayoutParams centerLPOuter = (LinearLayout.LayoutParams) centerLLOuter.getLayoutParams();
                centerLPOuter.height = this._smallWindow ? LinearLayout.LayoutParams.WRAP_CONTENT : LinearLayout.LayoutParams.MATCH_PARENT;
                centerLLOuter.setLayoutParams(centerLPOuter);
            }

        } else {
            mainLP.width = (int) (200 * getResources().getDisplayMetrics().density + 0.5);
            mainLP.height = 0;
            mainLL.setLayoutParams(mainLP);

            LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
            centerLP.height = 0;
            centerLL.setLayoutParams(centerLP);

            if (! this.getCurrentTheme().equals(PREF_VALUE_THEME_OLD_COMPUTER) && ! this.getCurrentTheme().equals(PREF_VALUE_THEME_MARINE)) {
                LinearLayout.LayoutParams centerLPOuter = (LinearLayout.LayoutParams) centerLLOuter.getLayoutParams();
                centerLPOuter.height = 0;
                centerLLOuter.setLayoutParams(centerLPOuter);
            }
        }
    }
}
