package net.nhiroki.bluelineconsole.applicationMain;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;

import net.nhiroki.bluelineconsole.R;

import static android.content.Context.NOTIFICATION_SERVICE;
import static androidx.core.app.NotificationCompat.PRIORITY_MAX;

public class AppNotification {
    private static final int NOTIFICATION_ID_ALWAYS = 1;
    private static final String NOTIFICATION_CHANNEL_ALWAYS = "always_channel";

    public static void update(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ALWAYS, context.getString(R.string.notification_channel_always_show), NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }


        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_main_always_show_notification", false)) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), NOTIFICATION_CHANNEL_ALWAYS)
                    .setPriority(PRIORITY_MAX)
                    .setSmallIcon(R.mipmap.ic_launcher_monochrome)
                    .setContentTitle(context.getString(R.string.notification_launch_this_app))
                    .setOngoing(true)
                    .setSound(null)
                    .setVibrate(null)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setShowWhen(false)
                    .setWhen(0)
                    .setContentIntent(pendingIntent);

            Notification notification = builder.build();

            notificationManager.notify(NOTIFICATION_ID_ALWAYS, notification);

        } else {
            notificationManager.cancel(NOTIFICATION_ID_ALWAYS);
        }
    }
}
