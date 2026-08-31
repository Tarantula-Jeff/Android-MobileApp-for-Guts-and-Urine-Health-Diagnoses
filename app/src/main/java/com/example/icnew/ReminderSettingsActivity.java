package com.example.icnew;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class ReminderSettingsActivity extends AppCompatActivity {
    private static final String PREFS = "ReminderPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_settings);
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        TimePicker time = findViewById(R.id.reminder_time);
        time.setHour(prefs.getInt("hour", 9));
        time.setMinute(prefs.getInt("minute", 0));
        ((Switch) findViewById(R.id.reminder_hydration)).setChecked(prefs.getBoolean("hydration", false));
        ((Switch) findViewById(R.id.reminder_sample)).setChecked(prefs.getBoolean("sample", false));
        ((Switch) findViewById(R.id.reminder_medication)).setChecked(prefs.getBoolean("medication", false));
        ((Switch) findViewById(R.id.reminder_appointment)).setChecked(prefs.getBoolean("appointment", false));
        findViewById(R.id.save_reminders).setOnClickListener(v -> save());
    }

    private void save() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 90);
        }
        TimePicker time = findViewById(R.id.reminder_time);
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putInt("hour", time.getHour()).putInt("minute", time.getMinute())
                .putBoolean("hydration", ((Switch) findViewById(R.id.reminder_hydration)).isChecked())
                .putBoolean("sample", ((Switch) findViewById(R.id.reminder_sample)).isChecked())
                .putBoolean("medication", ((Switch) findViewById(R.id.reminder_medication)).isChecked())
                .putBoolean("appointment", ((Switch) findViewById(R.id.reminder_appointment)).isChecked())
                .apply();
        ReminderScheduler.refresh(this);
        Toast.makeText(this, "Reminder preferences saved", Toast.LENGTH_SHORT).show();
    }
}
