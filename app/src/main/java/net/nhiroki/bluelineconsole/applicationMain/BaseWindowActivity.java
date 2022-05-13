package net.nhiroki.bluelineconsole.applicationMain;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;

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

        this.currentTheme.apply(this, this.iAmHomeActivity, this.smallWindow);

        this.currentTheme.setPayloadLayout(this.mainLayoutResID, this);

        // TitleBarDragOnTouchListener has some state, it is safer to create different instance
        this.currentTheme.setOnTouchListenerForTitleBar(new TitleBarDragOnTouchListener(), this);

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
                    View window = BaseWindowActivity.this.currentTheme.findVisibleRootView(BaseWindowActivity.this);
                    paddingLeftOffset = window.getPaddingLeft() - event.getRawX();
                    paddingTopOffset = window.getPaddingTop() - event.getRawY();
                    paddingRightOffset = window.getPaddingRight() + event.getRawX();
                    paddingBottomOffset = window.getPaddingBottom() + event.getRawY();

                    BaseWindowActivity.this.disableWindowAnimationForElements();
                    return true;
                }
                case MotionEvent.ACTION_MOVE: {
                    View window = BaseWindowActivity.this.currentTheme.findVisibleRootView(BaseWindowActivity.this);

                    window.setPadding((int) (paddingLeftOffset + event.getRawX()), (int) (paddingTopOffset + event.getRawY()),
                            (int) (paddingRightOffset - event.getRawX()), (int) (paddingBottomOffset - event.getRawY()));

                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    View window = BaseWindowActivity.this.currentTheme.findVisibleRootView(BaseWindowActivity.this);

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

        this.currentTheme.findWholeDisplayView(this).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousHeight = -1;

            @Override
            public void onGlobalLayout() {
                int rootHeight = BaseWindowActivity.this.currentTheme.findWholeDisplayView(BaseWindowActivity.this).getHeight();

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

    public static final int ROOT_WINDOW_FULL_WIDTH_IN_MOBILE = 1;
    public static final int ROOT_WINDOW_ALWAYS_HORIZONTAL_MARGIN = 2;
    public static final int ROOT_WINDOW_FULL_WIDTH_ALWAYS = 3;

    protected void setWindowBoundarySize(int widthMode, int windowNestStep) {
        this.currentTheme.setWindowBoundarySize(widthMode, windowNestStep, this);
    }

    protected void setHeaderFooterTexts(CharSequence headerText, CharSequence footerText) {
        this.currentTheme.setHeaderFooterTexts(this, headerText, footerText);
    }

    protected void setWindowLocationGravity(int gravity) {
        this.currentTheme.setWindowLocationGravity(gravity, this);
    }

    protected void enableBaseWindowAnimation() {
        this.animationHasBeenEnabled = true;
        if (this.getAnimationEnabledPreferenceValue()) {
            this.enableWindowAnimationForElements();
        }
    }

    public boolean getAnimationEnabledPreferenceValue() {
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
        this.currentTheme.enableWindowAnimationForElement(this);
    }

    @CallSuper
    protected void disableWindowAnimationForElements() {
        this.currentTheme.disableWindowAnimationForElement(this);
    }

    protected double getWindowBodyAvailableHeight() {
        return this.currentTheme.getWindowBodyAvailableHeight(this);
    }

    protected void changeBaseWindowElementSizeForAnimation(boolean visible) {
        this.currentTheme.changeBaseWindowElementSizeForAnimation(this, visible || !this.getAnimationEnabledPreferenceValue(), this.smallWindow);
    }
}
