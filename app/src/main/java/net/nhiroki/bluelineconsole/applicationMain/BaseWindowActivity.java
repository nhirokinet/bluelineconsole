package net.nhiroki.bluelineconsole.applicationMain;

import android.animation.LayoutTransition;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import net.nhiroki.bluelineconsole.R;

public class BaseWindowActivity extends AppCompatActivity {
    private int _mainLayoutResID;
    private boolean _smallWindow;

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
        super.onCreate(savedInstanceState);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.setContentView(R.layout.base_window_layout);

        ViewStub mainViewStub = this.findViewById(R.id.baseWindowMainViewStub);
        mainViewStub.setLayoutResource(this._mainLayoutResID);
        mainViewStub.inflate();

        this.findViewById(R.id.baseWindowRootLinearLayout).setOnClickListener(new ExitOnClickListener());

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

    @Override
    protected void onResume() {
        super.onResume();

        ((ConstraintLayout) findViewById(R.id.baseWindowMainLayoutRoot)).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
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
        this.finish();
    }

    protected void onHeightChange() {

    }

    protected void setRootPadding(int left, int top, int right, int bottom) {
        findViewById(R.id.baseWindowRootLinearLayout).setPadding(left, top, right, bottom);
    }

    protected void setNestingPadding(int step) {
        findViewById(R.id.baseWindowRootLinearLayout).setPadding(
                (int)(8 * step * getResources().getDisplayMetrics().density),
                (int)(24 * step * getResources().getDisplayMetrics().density),
                (int)(8 * step * getResources().getDisplayMetrics().density),
                (int)(24 * step * getResources().getDisplayMetrics().density));
    }

    protected void setHeaderFooterTexts(CharSequence headerText, CharSequence footerText) {
        ((TextView) findViewById(R.id.baseWindowMainHeaderTextView)).setText(headerText);
        ((TextView) findViewById(R.id.baseWindowMainFooterTextView)).setText(footerText);
    }

    protected void setWindowLocationGravity(int gravity) {
        ((LinearLayout)findViewById(R.id.baseWindowRootLinearLayout)).setGravity(gravity);
    }

    protected void enableBaseWindowAnimation() {
        ((ViewGroup) findViewById(R.id.baseWindowMainLayoutRoot)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowRootLinearLayout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowHeaderWrapper)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowMainLinearLayoutOuter)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowMainLinearLayout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowFooterWrapper)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
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
        View mainLL = ((LinearLayout)findViewById(R.id.baseWindowMainLinearLayout)).getChildAt(0);
        LinearLayout.LayoutParams mainLP = (LinearLayout.LayoutParams) mainLL.getLayoutParams();
        View centerLL = findViewById(R.id.baseWindowMainLinearLayout);
        View centerLLOuter = findViewById(R.id.baseWindowMainLinearLayoutOuter);

        if (visible) {
            mainLP.width = LinearLayout.LayoutParams.MATCH_PARENT;
            mainLP.height = this._smallWindow ? LinearLayout.LayoutParams.WRAP_CONTENT : LinearLayout.LayoutParams.MATCH_PARENT;
            mainLL.setLayoutParams(mainLP);

            LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
            centerLP.height = this._smallWindow ? LinearLayout.LayoutParams.WRAP_CONTENT : LinearLayout.LayoutParams.MATCH_PARENT;
            centerLL.setLayoutParams(centerLP);

            LinearLayout.LayoutParams centerLPOuter = (LinearLayout.LayoutParams) centerLLOuter.getLayoutParams();
            centerLPOuter.height = this._smallWindow ? LinearLayout.LayoutParams.WRAP_CONTENT : LinearLayout.LayoutParams.MATCH_PARENT;
            centerLLOuter.setLayoutParams(centerLPOuter);

        } else {
            mainLP.width = (int) (200 * getResources().getDisplayMetrics().density + 0.5);
            mainLP.height = 0;
            mainLL.setLayoutParams(mainLP);

            LinearLayout.LayoutParams centerLP = (LinearLayout.LayoutParams) centerLL.getLayoutParams();
            centerLP.height = 0;
            centerLL.setLayoutParams(centerLP);

            LinearLayout.LayoutParams centerLPOuter = (LinearLayout.LayoutParams) centerLLOuter.getLayoutParams();
            centerLPOuter.height = 0;
            centerLLOuter.setLayoutParams(centerLPOuter);
        }
    }}

