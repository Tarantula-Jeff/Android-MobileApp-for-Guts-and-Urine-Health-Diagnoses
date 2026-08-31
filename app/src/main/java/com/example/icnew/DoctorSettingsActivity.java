package com.example.icnew;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class DoctorSettingsActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_settings);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
        ((TextView) findViewById(R.id.doctor_settings_email)).setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        ((TextView) findViewById(R.id.doctor_settings_id)).setText(FirebaseAuth.getInstance().getCurrentUser().getUid());
        EditText name = findViewById(R.id.doctor_display_name); EditText title = findViewById(R.id.doctor_title); Switch alerts = findViewById(R.id.doctor_alert_preference); RadioGroup filter = findViewById(R.id.doctor_alert_filter);
        android.content.SharedPreferences prefs = getSharedPreferences("DoctorProfilePrefs", MODE_PRIVATE);
        name.setText(prefs.getString("name", "")); title.setText(prefs.getString("title", "")); alerts.setChecked(prefs.getBoolean("urgentAlerts", true)); String savedFilter = prefs.getString("alertFilter", "all"); filter.check(savedFilter.equals("new") ? R.id.filter_new : savedFilter.equals("acknowledged") ? R.id.filter_acknowledged : savedFilter.equals("resolved") ? R.id.filter_resolved : R.id.filter_all);
        findViewById(R.id.save_doctor_profile).setOnClickListener(view -> { prefs.edit().putString("name", name.getText().toString().trim()).putString("title", title.getText().toString().trim()).putBoolean("urgentAlerts", alerts.isChecked()).putString("alertFilter", filter.getCheckedRadioButtonId() == R.id.filter_new ? "new" : filter.getCheckedRadioButtonId() == R.id.filter_acknowledged ? "acknowledged" : filter.getCheckedRadioButtonId() == R.id.filter_resolved ? "resolved" : "all").apply(); Map<String,Object> profile = new HashMap<>(); profile.put("name", name.getText().toString().trim()); profile.put("title", title.getText().toString().trim()); FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(profile, com.google.firebase.firestore.SetOptions.merge()); Toast.makeText(this, "Profile preferences saved.", Toast.LENGTH_SHORT).show(); });
        findViewById(R.id.doctor_settings_back).setOnClickListener(view -> finish());
        findViewById(R.id.doctor_settings_signout).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent login = new Intent(this, Login.class);
            login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(login);
        });
    }
}
