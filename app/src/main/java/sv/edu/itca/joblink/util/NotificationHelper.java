package sv.edu.itca.joblink.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import sv.edu.itca.joblink.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "joblink_notifications";
    private static final String CHANNEL_NAME = "Notificaciones de JobLink";

    public static void showNotification(Context context, String title, String message, Intent targetIntent, int notificationId) {
        if (context == null) return;

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            channel.setDescription("Canal de notificaciones de nuevos mensajes y solicitudes de trabajo");
            manager.createNotificationChannel(channel);
        }

        PendingIntent pendingIntent = null;
        if (targetIntent != null) {
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            pendingIntent = PendingIntent.getActivity(context, notificationId, targetIntent, flags);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_person)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{0, 250, 250, 250})
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }

        manager.notify(notificationId, builder.build());
    }
}
