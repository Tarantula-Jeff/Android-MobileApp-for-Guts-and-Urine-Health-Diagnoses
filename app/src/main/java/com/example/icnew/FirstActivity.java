package com.example.icnew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirstActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        auth = FirebaseAuth.getInstance();
        TextView userDetails = findViewById(R.id.user_details);
        Button logout = findViewById(R.id.logout);
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
            return;
        }
        userDetails.setText(user.getEmail());
        BottomNavigationView navigation = findViewById(R.id.patient_bottom_nav);
        navigation.setSelectedItemId(R.id.nav_home);
        navigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_analyze) { goToSecondActivity(null); return true; }
            if (item.getItemId() == R.id.nav_history) { openSampleHistory(null); return true; }
            if (item.getItemId() == R.id.nav_profile) { openDeliverySettings(null); return true; }
            return true;
        });
        logout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent login = new Intent(getApplicationContext(), Login.class);
            login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(login);
        });
    }

    public void goToSecondActivity(View view) {
        startActivity(new Intent(this, "urine".equals(getSharedPreferences("DoctorPrefs", MODE_PRIVATE).getString("defaultAnalysis", "stool")) ? UrineAnalysis.class : MainActivity.class));
    }

    public void openDeliverySettings(View view) {
        startActivity(new Intent(this, DeliverySettingsActivity.class));
    }

    public void openSampleHistory(View view) {
        startActivity(new Intent(this, SampleHistoryActivity.class));
    }

    public void openEmergencyGuidance(View view) {
        startActivity(new Intent(this, EmergencyGuidanceActivity.class));
    }

    public void openReminders(View view) {
        startActivity(new Intent(this, ReminderSettingsActivity.class));
    }

    public void openHealthSummary(View view) {
        startActivity(new Intent(this, HealthSummaryActivity.class));
    }

    public void openSymptomTracker(View view) {
        startActivity(new Intent(this, SymptomTrackerActivity.class));
    }

    public void openAlertHistory(View view) {
        startActivity(new Intent(this, PatientAlertHistoryActivity.class));
    }
}