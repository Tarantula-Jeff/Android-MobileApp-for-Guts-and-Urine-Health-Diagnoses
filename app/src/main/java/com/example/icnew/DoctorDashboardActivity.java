package com.example.icnew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DoctorDashboardActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);
        BottomNavigationView navigation = findViewById(R.id.doctor_bottom_nav);
        navigation.setSelectedItemId(R.id.nav_dashboard);
        navigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_patients) { openPatients(null); return true; }
            if (item.getItemId() == R.id.nav_alerts) { openAlerts(null); return true; }
            if (item.getItemId() == R.id.nav_doctor_profile) { openDoctorSettings(null); return true; }
            return true;
        });
        String doctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ((TextView) findViewById(R.id.doctor_app_id)).setText("Your app ID: " + doctorId);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users").whereEqualTo("doctorId", doctorId).addSnapshotListener((value, error) -> {
            if (error == null && value != null) ((TextView) findViewById(R.id.patient_count)).setText(value.size() + " connected patient" + (value.size() == 1 ? "" : "s"));
        });
        database.collection("alerts").whereEqualTo("doctorId", doctorId).addSnapshotListener((value, error) -> {
            if (error == null && value != null) ((TextView) findViewById(R.id.alert_count)).setText(value.size() + " alert" + (value.size() == 1 ? "" : "s"));
        });
    }

    public void openDoctorQr(View view) { startActivity(new Intent(this, DoctorQrActivity.class)); }
    public void openPatients(View view) { startActivity(new Intent(this, DoctorPatientsActivity.class)); }
    public void openAlerts(View view) { startActivity(new Intent(this, DoctorAlertsActivity.class)); }
    public void goBack(View view) { startActivity(new Intent(this, DoctorSettingsActivity.class)); }
    public void openDoctorSettings(View view) { startActivity(new Intent(this, DoctorSettingsActivity.class)); }
    public void signOut(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent login = new Intent(this, Login.class);
        login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(login);
    }
}