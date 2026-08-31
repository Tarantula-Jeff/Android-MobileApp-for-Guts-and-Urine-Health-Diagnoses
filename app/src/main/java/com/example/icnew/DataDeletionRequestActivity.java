package com.example.icnew;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DataDeletionRequestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_deletion_request);
        findViewById(R.id.submit_deletion_request).setOnClickListener(view -> submit());
    }

    private void submit() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String confirmation = ((EditText) findViewById(R.id.deletion_confirmation))
                .getText().toString().trim();
        if (!"DELETE".equals(confirmation)) {
            ((EditText) findViewById(R.id.deletion_confirmation)).setError("Type DELETE to confirm");
            return;
        }
        boolean clearLocal = ((CheckBox) findViewById(R.id.clear_local_data)).isChecked();
        Map<String, Object> request = new HashMap<>();
        request.put("userId", user.getUid());
        request.put("email", user.getEmail());
        request.put("role", "patient");
        request.put("status", "requested");
        request.put("requestedAt", FieldValue.serverTimestamp());
        request.put("clearLocalData", clearLocal);

        FirebaseFirestore.getInstance().collection("deletionRequests").document(user.getUid()).set(request)
                .addOnSuccessListener(unused -> {
                    if (clearLocal) clearLocalData();
                    Toast.makeText(this, "Deletion request submitted. Your account remains active until it is processed.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(error -> Toast.makeText(this,
                        "Could not submit request: " + error.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void clearLocalData() {
        SampleHistoryRepository.clear(this);
        getSharedPreferences("SymptomTracker", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("ReminderPrefs", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("DoctorPrefs", MODE_PRIVATE).edit().clear().apply();
    }
}
