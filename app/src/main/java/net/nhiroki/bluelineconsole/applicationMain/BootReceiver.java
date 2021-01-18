package net.nhiroki.bluelineconsole.applicationMain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String actionFrom = intent.getAction();

        if (actionFrom == null) {
            return;
        }
        if (! actionFrom.equals(Intent.ACTION_BOOT_COMPLETED)) {
            return;
        }
        AppNotification.update(context);
    }
}
