package com.example.icnew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {
    private TextInputEditText editTextEmail, editTextPassword, fullName;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private RadioGroup roleGroup;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        auth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        fullName = findViewById(R.id.full_name);
        progressBar = findViewById(R.id.progressBar);
        roleGroup = findViewById(R.id.role_group);
        ((TextView) findViewById(R.id.loginNow)).setOnClickListener(view -> {
            startActivity(new Intent(this, Login.class));
            finish();
        });
        ((Button) findViewById(R.id.Register_button)).setOnClickListener(view -> register());
    }

    private void register() {
        String email = String.valueOf(editTextEmail.getText()).trim();
        String password = String.valueOf(editTextPassword.getText());
        String name = String.valueOf(fullName.getText()).trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter your full name, email, and password.", Toast.LENGTH_SHORT).show();
            return;
        }
        String role = roleGroup.getCheckedRadioButtonId() == R.id.role_doctor ? "doctor" : "patient";
        progressBar.setVisibility(View.VISIBLE);
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, task.getException() == null ? "Account creation failed." : task.getException().getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            FirebaseUser user = auth.getCurrentUser();
            Map<String, Object> profile = new HashMap<>();
            profile.put("email", user.getEmail());
            profile.put("name", name);
            profile.put("role", role);
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).set(profile)
                    .addOnSuccessListener(unused -> openDashboard(role))
                    .addOnFailureListener(error -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Account created, but profile setup failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }

    private void openDashboard(String role) {
        progressBar.setVisibility(View.GONE);
        Class<?> destination = "doctor".equals(role) ? DoctorDashboardActivity.class : FirstActivity.class;
        startActivity(new Intent(this, destination));
        finish();
    }
}