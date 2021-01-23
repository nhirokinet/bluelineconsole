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
    private final int _mainLayoutResID;
    private final boolean _smallWindow;
    private boolean _comingBack = false;
    private String _currentTheme;

    private boolean _animationEnabled = false;

    public static final String PREF_NAME_THEME = "pref_appearance_theme";
    private static final String PREF_VALUE_THEME_DEFAULT = "default";
    private static final String PREF_VALUE_THEME_LIGHT = "light";
    private static final String PREF_VALUE_THEME_DARK = "dark";
    private static final String PREF_VALUE_THEME_OLD_COMPUTER = "old_computer";
    private static final String PREF_VALUE_THEME_MARINE = "marine";

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
        this._mainLayoutResID = mainLayoutResID;
        this._smallWindow = smallWindow;
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

        String actualTheme = this._currentTheme;

        super.onCreate(savedInstanceState);

        if (this._currentTheme.equals(PREF_VALUE_THEME_DEFAULT)) {
            boolean system_is_dark_theme = Build.VERSION.SDK_INT >= 29 && (this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            actualTheme = system_is_dark_theme ? PREF_VALUE_THEME_DARK : PREF_VALUE_THEME_LIGHT;
        }

        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        switch (actualTheme) {
            case PREF_VALUE_THEME_DARK:
                this.setTheme(R.style.AppThemeDark);
                this.setContentView(R.layout.base_window_layout_default);
                break;
            case PREF_VALUE_THEME_MARINE:
                this.setTheme(R.style.AppThemeMarine);
                this.setContentView(R.layout.base_window_layout_marine);

                LinearLayout centerLL = findViewById(R.id.baseWindowIntermediateWrapper);
                LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
                centerLP.height = this._smallWindow ? LinearLayout.LayoutParams.WRAP_CONTENT : LinearLayout.LayoutParams.MATCH_PARENT;
                centerLL.setLayoutParams(centerLP);
                break;
            case PREF_VALUE_THEME_OLD_COMPUTER:
                this.setTheme(R.style.AppThemeOldComputer);
                this.setContentView(R.layout.base_window_layout_old_computer);
                break;
            case PREF_VALUE_THEME_LIGHT:
            default:
                this.setTheme(R.style.AppTheme);
                this.setContentView(R.layout.base_window_layout_default);
                break;
        }

        ViewStub mainViewStub = this.findViewById(R.id.baseWindowMainViewStub);
        mainViewStub.setLayoutResource(this._mainLayoutResID);
        mainViewStub.inflate();

        this.findViewById(R.id.baseWindowMainLayoutRoot).setOnClickListener(new ExitOnClickListener());
        ((LinearLayout)findViewById(R.id.baseWindowMainLinearLayout)).getChildAt(0).setOnClickListener(null);

        if (! this.getCurrentTheme().equals(PREF_VALUE_THEME_OLD_COMPUTER)) {
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

        this.onAccentColorChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        this._comingBack = false;

        final boolean animationEnabledBySetting = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_NAME_ANIMATION, true);

        if (animationEnabledBySetting) {
            if (this._animationEnabled) {
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
        return PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME_THEME, PREF_VALUE_THEME_DEFAULT);
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

    @CallSuper
    protected void onAccentColorChanged() {
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

        this.applyAccentColor(color);
    }

    @CallSuper
    protected void applyAccentColor(int color) {
        // TODO: why? setTint doesn't work when resuming from another Activity even if called from onResume; setBackgroundColor works, although.
        // After this is resolved, applyAccentColor can be called from onResume instead of onCreate, and apply immediately after change.
        // This behavior also seems to depend on Android version. Currently not to pursur perfect behavior here, just encourage users to restart.
        if (this.getCurrentTheme().equals(PREF_VALUE_THEME_DARK) || this.getCurrentTheme().equals(PREF_VALUE_THEME_LIGHT) || this.getCurrentTheme().equals(PREF_VALUE_THEME_DEFAULT)) {
            DrawableCompat.setTint(this.findViewById(R.id.baseWindowDefaultThemeHeaderAccent).getBackground(), color);
            DrawableCompat.setTint(this.findViewById(R.id.baseWindowDefaultThemeFooterAccent).getBackground(), color);
            this.findViewById(R.id.baseWindowDefaultThemeHeaderStartAccent).setBackgroundColor(color);
            this.findViewById(R.id.baseWindowDefaultThemeFooterEndAccent).setBackgroundColor(color);
            this.findViewById(R.id.baseWindowMainLinearLayoutOuter).setBackgroundColor(color);
        }
    }

    protected void onHeightChange() {}

    protected static final int ROOT_WINDOW_FULL_WIDTH_IN_MOBILE = 1;
    protected static final int ROOT_WINDOW_ALWAYS_HIRZONTAL_MARGIN = 2;
    protected static final int ROOT_WINDOW_FULL_WIDTH_ALWAYS = 3;

    protected void setWindowBoundarySize(int widthMode, int windowNestStep) {
        final int baseHorizontalMerginInPixels = (int)(8 * windowNestStep * getResources().getDisplayMetrics().density);
        final int baseVerticalMerginInPixels = (int)(24 * windowNestStep * getResources().getDisplayMetrics().density);

        if (widthMode == ROOT_WINDOW_FULL_WIDTH_ALWAYS) {
            findViewById(R.id.baseWindowRootLinearLayout).setPadding(baseHorizontalMerginInPixels, baseVerticalMerginInPixels, baseHorizontalMerginInPixels, baseVerticalMerginInPixels);

        } else {
            final Point displaySize = new Point();
            this.getWindowManager().getDefaultDisplay().getSize(displaySize);

            final int maxPanelWidth;

            maxPanelWidth = (int) (600 * getResources().getDisplayMetrics().density);

            final int panelWidth;

            if (widthMode == ROOT_WINDOW_ALWAYS_HIRZONTAL_MARGIN) {
                panelWidth = Math.min((int) (displaySize.x * ((displaySize.x < displaySize.y) ? 0.87 : 0.7) - baseHorizontalMerginInPixels), maxPanelWidth - baseHorizontalMerginInPixels);
            } else {
                panelWidth = Math.min(maxPanelWidth - baseHorizontalMerginInPixels, displaySize.x - baseHorizontalMerginInPixels);
            }

            final int horizontal = Math.max((displaySize.x - panelWidth) / 2, baseHorizontalMerginInPixels);
            findViewById(R.id.baseWindowRootLinearLayout).setPadding(horizontal, baseVerticalMerginInPixels, horizontal, baseVerticalMerginInPixels);
        }
    }

    @SuppressLint("SetTextI18n")
    protected void setHeaderFooterTexts(CharSequence headerText, CharSequence footerText) {
        ((TextView) findViewById(R.id.baseWindowMainHeaderTextView)).setText(headerText);
        if (this.getCurrentTheme().equals(PREF_VALUE_THEME_OLD_COMPUTER)) {
            if (footerText == null) {
                ((TextView) findViewById(R.id.baseWindowMainHeaderTextView)).setText(headerText);
            } else {
                ((TextView) findViewById(R.id.baseWindowMainHeaderTextView)).setText(headerText + " " + footerText);
            }
        } else {
            if (footerText == null) {
                ((TextView) findViewById(R.id.baseWindowMainFooterTextView)).setText(headerText);
            } else {
                ((TextView) findViewById(R.id.baseWindowMainFooterTextView)).setText(footerText);
            }
            ((TextView) findViewById(R.id.baseWindowMainHeaderTextView)).setText(headerText);
        }
    }

    protected void setWindowLocationGravity(int gravity) {
        ((LinearLayout)findViewById(R.id.baseWindowRootLinearLayout)).setGravity(gravity);
    }

    protected void enableBaseWindowAnimation() {
        this._animationEnabled = true;
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_NAME_ANIMATION, true)) {
            this.enableWindowAnimationForElements();
        }
    }

    private void enableWindowAnimationForElements() {
        ((ViewGroup) findViewById(R.id.baseWindowMainLayoutRoot)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowRootLinearLayout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowHeaderWrapper)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        if (! this.getCurrentTheme().equals(PREF_VALUE_THEME_OLD_COMPUTER) && !this.getCurrentTheme().equals(PREF_VALUE_THEME_MARINE)) {
            ((ViewGroup) findViewById(R.id.baseWindowMainLinearLayoutOuter)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            ((ViewGroup) findViewById(R.id.baseWindowDefaultThemeHeaderStartAccent)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            ((ViewGroup) findViewById(R.id.baseWindowDefaultThemeFooterEndAccent)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            ((ViewGroup) findViewById(R.id.baseWindowDefaultThemeHeaderImagePart)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            ((ViewGroup) findViewById(R.id.baseWindowDefaultThemeFooterImagePart)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        }
        ((ViewGroup) findViewById(R.id.baseWindowMainLinearLayout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowFooterWrapper)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
    }

    private void disableWindowAnimationForElements() {
        ((ViewGroup) findViewById(R.id.baseWindowMainLayoutRoot)).getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowRootLinearLayout)).getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowHeaderWrapper)).getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
        if (! this.getCurrentTheme().equals(PREF_VALUE_THEME_OLD_COMPUTER) && this.getCurrentTheme().equals(PREF_VALUE_THEME_MARINE)) {
            ((ViewGroup) findViewById(R.id.baseWindowMainLinearLayoutOuter)).getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
            ((ViewGroup) findViewById(R.id.baseWindowDefaultThemeHeaderStartAccent)).getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
            ((ViewGroup) findViewById(R.id.baseWindowDefaultThemeFooterEndAccent)).getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
            ((ViewGroup) findViewById(R.id.baseWindowDefaultThemeHeaderImagePart)).getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
            ((ViewGroup) findViewById(R.id.baseWindowDefaultThemeFooterImagePart)).getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
        }
        ((ViewGroup) findViewById(R.id.baseWindowMainLinearLayout)).getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowFooterWrapper)).getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
    }

    protected double getWindowBodyAvailableHeight() {
        return findViewById(R.id.baseWindowMainLayoutRoot).getHeight() - findViewById(R.id.baseWindowHeaderWrapper).getHeight() * 2.0;
    }

    private class ExitOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }

    protected void changeBaseWindowElementSize(boolean visible) {
        LinearLayout centerLL = findViewById(R.id.baseWindowMainLinearLayout);
        View centerLLOuter = findViewById(R.id.baseWindowMainLinearLayoutOuter);
        View mainLL = centerLL.getChildAt(0);
        LinearLayout.LayoutParams mainLP = (LinearLayout.LayoutParams) mainLL.getLayoutParams();

        if (visible) {
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
