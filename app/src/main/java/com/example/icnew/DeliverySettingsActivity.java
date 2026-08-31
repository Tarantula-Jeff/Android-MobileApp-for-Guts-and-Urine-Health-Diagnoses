package com.example.icnew;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class DeliverySettingsActivity extends AppCompatActivity {
    private static final int FIND_DOCTOR_REQUEST = 42;
    private EditText phone, doctorId;
    private RadioButton appDelivery, smsDelivery;
    private Switch includePicture;
    private TextView selectedDoctor; private EditText displayName; private Switch urgentNotifications, shareConsent; private RadioGroup defaultAnalysis;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_settings);
        phone = findViewById(R.id.settings_doctor_phone);
        doctorId = findViewById(R.id.settings_doctor_id);
        appDelivery = findViewById(R.id.delivery_app);
        smsDelivery = findViewById(R.id.delivery_sms);
        includePicture = findViewById(R.id.include_picture);
        selectedDoctor = findViewById(R.id.selected_doctor); displayName = findViewById(R.id.settings_display_name); urgentNotifications = findViewById(R.id.settings_urgent_notifications); shareConsent = findViewById(R.id.settings_share_consent); defaultAnalysis = findViewById(R.id.default_analysis_group);
        SharedPreferences preferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
        phone.setText(preferences.getString("doctorPhoneNumber", "")); displayName.setText(preferences.getString("displayName", "")); urgentNotifications.setChecked(preferences.getBoolean("urgentNotifications", true)); shareConsent.setChecked(preferences.getBoolean("shareConsent", true)); defaultAnalysis.check("urine".equals(preferences.getString("defaultAnalysis", "stool")) ? R.id.default_urine : R.id.default_stool);
        String currentDoctorId = preferences.getString("doctorUid", "");
        doctorId.setText(currentDoctorId);
        if (!currentDoctorId.isEmpty()) selectedDoctor.setText("Current doctor app ID: " + currentDoctorId);
        appDelivery.setChecked(!"sms".equals(preferences.getString("deliveryMethod", "app")));
        smsDelivery.setChecked("sms".equals(preferences.getString("deliveryMethod", "app")));
        includePicture.setChecked(false);
        includePicture.setEnabled(false);
        findViewById(R.id.save_delivery_settings).setOnClickListener(view -> save());
        findViewById(R.id.request_data_deletion).setOnClickListener(view -> startActivity(new Intent(this, DataDeletionRequestActivity.class)));
        findViewById(R.id.clear_local_history).setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Clear local history?")
                .setMessage("This permanently removes saved stool and urine sample images from this phone. Doctor alerts and online text history are not affected.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (dialog, which) -> { SampleHistoryRepository.clear(this); Toast.makeText(this, "Local sample history cleared.", Toast.LENGTH_SHORT).show(); })
                .show());
        findViewById(R.id.disconnect_doctor).setOnClickListener(view -> new AlertDialog.Builder(this).setTitle("Disconnect doctor?").setMessage("This removes your doctor connection. Your local sample history is kept.").setNegativeButton("Cancel", null).setPositiveButton("Disconnect", (d, w) -> { getSharedPreferences("DoctorPrefs", MODE_PRIVATE).edit().remove("doctorUid").remove("doctorPhoneNumber").apply(); FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser(); if (current != null) { Map<String, Object> update = new HashMap<>(); update.put("doctorId", null); FirebaseFirestore.getInstance().collection("users").document(current.getUid()).set(update, SetOptions.merge()); } doctorId.setText(""); phone.setText(""); selectedDoctor.setText("No doctor connected."); Toast.makeText(this, "Doctor disconnected.", Toast.LENGTH_SHORT).show(); }).show());
                findViewById(R.id.scan_doctor_qr).setOnClickListener(view -> GmsBarcodeScanning.getClient(this).startScan().addOnSuccessListener(barcode -> {
            String raw = barcode.getRawValue();
            if (raw != null && raw.startsWith("gutguardian:doctor:")) {
                String scannedId = raw.substring("gutguardian:doctor:".length());
                doctorId.setText(scannedId);
                selectedDoctor.setText("Doctor QR scanned. Tap Save preferences to connect.");
            } else {
                Toast.makeText(this, "This is not a GutGuardian doctor QR code.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(error -> Toast.makeText(this, "Could not scan QR code: " + error.getMessage(), Toast.LENGTH_LONG).show()));
        findViewById(R.id.find_doctor).setOnClickListener(view ->
                startActivityForResult(new Intent(this, DoctorDirectoryActivity.class), FIND_DOCTOR_REQUEST));
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FIND_DOCTOR_REQUEST && resultCode == RESULT_OK && data != null) {
            doctorId.setText(data.getStringExtra("doctorId"));
            selectedDoctor.setText(data.getStringExtra("doctorLabel"));
        }
    }

    private void save() {
        String savedDoctorId = doctorId.getText().toString().trim();
        boolean useSms = smsDelivery.isChecked();
        if (!useSms && savedDoctorId.isEmpty()) {
            Toast.makeText(this, "Choose your doctor for in-app alerts.", Toast.LENGTH_LONG).show();
            return;
        }
        if (useSms) {
            savePreferencesAndProfile(savedDoctorId);
            return;
        }
        FirebaseFirestore.getInstance().collection("users").document(savedDoctorId).get()
                .addOnSuccessListener(doctor -> {
                    if (!doctor.exists() || !"doctor".equals(doctor.getString("role"))) {
                        Toast.makeText(this, "That app ID does not belong to a doctor account.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    savePreferencesAndProfile(savedDoctorId);
                })
                .addOnFailureListener(error -> Toast.makeText(this, "Could not verify the doctor ID: " + error.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void savePreferencesAndProfile(String savedDoctorId) {
        getSharedPreferences("DoctorPrefs", MODE_PRIVATE).edit()
                .putString("doctorPhoneNumber", phone.getText().toString().trim())
                .putString("doctorUid", savedDoctorId)
                .putString("deliveryMethod", smsDelivery.isChecked() ? "sms" : "app")
                .putBoolean("includePicture", false).putString("displayName", displayName.getText().toString().trim()).putBoolean("urgentNotifications", urgentNotifications.isChecked()).putBoolean("shareConsent", shareConsent.isChecked()).putString("defaultAnalysis", defaultAnalysis.getCheckedRadioButtonId() == R.id.default_urine ? "urine" : "stool")
                .apply();
        FirebaseUser patient = FirebaseAuth.getInstance().getCurrentUser();
        if (patient == null) return;
        Map<String, Object> profile = new HashMap<>();
        profile.put("email", patient.getEmail()); profile.put("name", displayName.getText().toString().trim());
        profile.put("role", "patient");
        profile.put("doctorId", savedDoctorId);
        FirebaseFirestore.getInstance().collection("users").document(patient.getUid()).set(profile, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Settings saved. Your doctor connection is active.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(error -> Toast.makeText(this, "Settings were saved on this phone, but could not sync: " + error.getMessage(), Toast.LENGTH_LONG).show());
    }
}