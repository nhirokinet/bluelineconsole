package net.nhiroki.bluelineconsole.applicationMain;

import android.animation.LayoutTransition;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;

import net.nhiroki.bluelineconsole.R;

public class BaseWindowActivity extends AppCompatActivity {
    private int _mainLayoutResID;

    protected BaseWindowActivity(@LayoutRes int mainLayoutResID) {
        this._mainLayoutResID = mainLayoutResID;
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
    public void onStop() {
        // This app should be as stateless as possible. When app disappears most activities should finish.
        super.onStop();
        this.finish();
    }

    protected void enableBaseWindowAnimation() {
        ((ViewGroup) findViewById(R.id.baseMainLayoutRoot)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowRootLinearLayout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowHeaderWrapper)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowMainLinearLayoutOuter)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowMainLinearLayout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup) findViewById(R.id.baseWindowFooterWrapper)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
    }

    private class ExitOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }
}
