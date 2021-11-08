package net.nhiroki.bluelineconsole.interfaces;

import net.nhiroki.bluelineconsole.applicationMain.MainActivity;

public interface EventLauncher {
    /**
     * launch corresponding event from activity
     * @param activity Source activity that triggers new activity
     */
    void launch(MainActivity activity);
}
