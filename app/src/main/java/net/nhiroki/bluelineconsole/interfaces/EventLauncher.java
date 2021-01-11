package net.nhiroki.bluelineconsole.interfaces;

import android.app.Activity;

public interface EventLauncher {
    /**
     * launch corresponding event from activity
     * @param activity Source activity that triggers new activity
     */
    void launch(Activity activity);
}
