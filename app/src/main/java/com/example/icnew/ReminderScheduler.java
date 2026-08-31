package com.example.icnew;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public final class ReminderScheduler {
    private static final String PREFS = "ReminderPrefs";
    private static final String[] TYPES = {"hydration", "sample", "medication", "appointment"};

    private ReminderScheduler() { }

    public static void refresh(Context context) {
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int hour = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("hour", 9);
        int minute = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("minute", 0);
        for (int i = 0; i < TYPES.length; i++) {
            PendingIntent intent = pendingIntent(context, TYPES[i], i);
            alarms.cancel(intent);
            if (!context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getBoolean(TYPES[i], false)) continue;
            Calendar next = Calendar.getInstance();
            next.set(Calendar.HOUR_OF_DAY, hour);
            next.set(Calendar.MINUTE, minute);
            next.set(Calendar.SECOND, 0);
            next.set(Calendar.MILLISECOND, 0);
            if (next.getTimeInMillis() <= System.currentTimeMillis()) next.add(Calendar.DAY_OF_YEAR, 1);
            alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, intent);
        }
    }

    private static PendingIntent pendingIntent(Context context, String type, int requestCode) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("type", type);
        return PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
