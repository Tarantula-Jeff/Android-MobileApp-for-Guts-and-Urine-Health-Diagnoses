package com.example.icnew;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;

public class DoctorDirectoryActivity extends AppCompatActivity {
    private final ArrayList<Doctor> allDoctors = new ArrayList<>();
    private final ArrayList<Doctor> visibleDoctors = new ArrayList<>();
    private final ArrayList<String> labels = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private TextView emptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_directory);
        emptyMessage = findViewById(R.id.no_doctors_message);
        ListView doctorList = findViewById(R.id.doctor_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels);
        doctorList.setAdapter(adapter);
        doctorList.setOnItemClickListener((parent, view, position, id) -> chooseDoctor(visibleDoctors.get(position)));
        findViewById(R.id.directory_back).setOnClickListener(view -> finish());
        ((TextInputEditText) findViewById(R.id.doctor_search)).addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) { }
        });
        loadDoctors();
    }

    private void loadDoctors() {
        FirebaseFirestore.getInstance().collection("users").whereEqualTo("role", "doctor").get()
                .addOnSuccessListener(documents -> {
                    allDoctors.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot document : documents) {
                        String email = document.getString("email");
                        String name = document.getString("name");
                        allDoctors.add(new Doctor(document.getId(), name, email));
                    }
                    filter(((TextInputEditText) findViewById(R.id.doctor_search)).getText().toString());
                })
                .addOnFailureListener(error -> {
                    emptyMessage.setVisibility(View.VISIBLE);
                    emptyMessage.setText("Could not load doctors. Check your connection and try again.");
                });
    }

    private void filter(String query) {
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        visibleDoctors.clear();
        labels.clear();
        for (Doctor doctor : allDoctors) {
            if (doctor.matches(normalized)) {
                visibleDoctors.add(doctor);
                labels.add(doctor.label());
            }
        }
        adapter.notifyDataSetChanged();
        emptyMessage.setVisibility(visibleDoctors.isEmpty() ? View.VISIBLE : View.GONE);
        if (visibleDoctors.isEmpty()) {
            emptyMessage.setText(allDoctors.isEmpty() ? "No doctor accounts have been added yet." : "No doctors match your search.");
        }
    }

    private void chooseDoctor(Doctor doctor) {
        Intent result = new Intent();
        result.putExtra("doctorId", doctor.id);
        result.putExtra("doctorLabel", doctor.label());
        setResult(RESULT_OK, result);
        finish();
    }

    private static class Doctor {
        final String id;
        final String name;
        final String email;

        Doctor(String id, String name, String email) {
            this.id = id;
            this.name = name == null ? "" : name;
            this.email = email == null ? "" : email;
        }

        boolean matches(String query) {
            return query.isEmpty() || name.toLowerCase(Locale.ROOT).contains(query)
                    || email.toLowerCase(Locale.ROOT).contains(query)
                    || id.toLowerCase(Locale.ROOT).contains(query);
        }

        @NonNull String label() {
            String title = name.isEmpty() ? email : name;
            String subtitle = name.isEmpty() ? "" : "\n" + email;
            return title + subtitle + "\nApp ID: " + id;
        }
    }
}
