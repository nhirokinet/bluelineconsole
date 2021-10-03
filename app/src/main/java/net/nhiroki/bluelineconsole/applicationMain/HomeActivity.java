package net.nhiroki.bluelineconsole.applicationMain;

import android.annotation.SuppressLint;

public class HomeActivity extends MainActivity {
    public HomeActivity() {
        super();
        this._iAmHomeActivity = true;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onStop() {
        super.originalOnStop();
    }
}
