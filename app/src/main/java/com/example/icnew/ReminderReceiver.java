package com.example.icnew;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra("type");
        String title = "GutGuardian reminder";
        String message = "Take a moment to check in with your health.";
        if ("hydration".equals(type)) message = "Remember to drink water and stay hydrated.";
        if ("sample".equals(type)) message = "If needed, record a stool or urine sample today.";
        if ("medication".equals(type)) message = "Remember to take medication as prescribed by your clinician.";
        if ("appointment".equals(type)) message = "Check any upcoming appointments or follow-up plans.";

        String channelId = "wellness_reminders";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Wellness reminders",
                    NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);
        }
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        try {
            NotificationManagerCompat.from(context).notify(type == null ? 100 : type.hashCode(), notification.build());
        } catch (SecurityException ignored) { }
    }
}
