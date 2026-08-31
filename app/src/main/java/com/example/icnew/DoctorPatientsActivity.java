package com.example.icnew;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DoctorPatientsActivity extends AppCompatActivity {
    private LinearLayout patientCards;
    private TextView count, empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_patients);
        patientCards = findViewById(R.id.patient_cards);
        count = findViewById(R.id.patient_page_count);
        empty = findViewById(R.id.patient_empty);
        findViewById(R.id.patients_back).setOnClickListener(view -> finish());
        loadPatients();
    }

    private void loadPatients() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String doctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").whereEqualTo("doctorId", doctorId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    patientCards.removeAllViews();
                    count.setText(value.size() + " patient" + (value.size() == 1 ? "" : "s"));
                    empty.setVisibility(value.isEmpty() ? View.VISIBLE : View.GONE);
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        addPatientCard(doc.getId(), doc.getString("email"), doctorId);
                    }
                });
    }

    private void addPatientCard(String patientId, String email, String doctorId) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(getColor(R.color.app_surface));
        card.setStrokeColor(getColor(R.color.brand_primary_container));
        card.setStrokeWidth(dp(1));
        card.setRadius(dp(20));
        card.setCardElevation(dp(2));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(18), dp(20), dp(18));

        TextView name = textView(email == null || email.isEmpty() ? "Patient" : email, 18,
                R.color.app_on_surface, true);
        TextView latest = textView("Latest result: Loading...", 14, R.color.app_on_surface_variant, false);
        latest.setPadding(0, dp(8), 0, 0);
        TextView alerts = textView("Urgent alerts: Loading...", 14, R.color.app_on_surface_variant, false);
        alerts.setPadding(0, dp(5), 0, 0);
        TextView activity = textView("Last activity: Loading...", 13, R.color.app_on_surface_variant, false);
        activity.setPadding(0, dp(8), 0, 0);

        content.addView(name);
        content.addView(latest);
        content.addView(alerts);
        content.addView(activity);
        card.addView(content);
        card.setOnClickListener(view -> {
            Intent intent = new Intent(this, PatientDetailActivity.class);
            intent.putExtra("patientId", patientId);
            intent.putExtra("patientEmail", email);
            startActivity(intent);
        });
        patientCards.addView(card);
        loadPatientOverview(patientId, doctorId, latest, alerts, activity);
    }

    private void loadPatientOverview(String patientId, String doctorId, TextView latest,
                                     TextView alerts, TextView activity) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("analysisResults").whereEqualTo("patientId", patientId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        latest.setText("Latest result: Unavailable");
                        return;
                    }
                    DocumentSnapshot newest = newestDocument(value.getDocuments());
                    if (newest == null) {
                        latest.setText("Latest result: No shared results yet");
                    } else {
                        String type = newest.getString("analysisType");
                        String result = newest.getString("classification");
                        latest.setText("Latest result: " + safe(type, "Analysis") + " - "
                                + safe(result, "No result"));
                        showActivity(activity, newest.getTimestamp("createdAt"),
                                newest.getString("createdAtText"));
                    }
                });

        database.collection("alerts").whereEqualTo("patientId", patientId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        alerts.setText("Urgent alerts: Unavailable");
                        return;
                    }
                    int activeCount = 0;
                    DocumentSnapshot newest = newestDocument(value.getDocuments());
                    for (DocumentSnapshot alert : value.getDocuments()) {
                        String status = alert.getString("status");
                        if (!"RESOLVED".equalsIgnoreCase(status)) activeCount++;
                    }
                    alerts.setText(activeCount == 0 ? "Urgent alerts: None active"
                            : "Urgent alerts: " + activeCount + " needs attention");
                    if (newest != null) {
                        showActivity(activity, newest.getTimestamp("createdAt"), null);
                    }
                });
    }

    private DocumentSnapshot newestDocument(java.util.List<DocumentSnapshot> documents) {
        DocumentSnapshot newest = null;
        long newestTime = Long.MIN_VALUE;
        for (DocumentSnapshot document : documents) {
            Timestamp timestamp = document.getTimestamp("createdAt");
            long time = timestamp == null ? Long.MIN_VALUE : timestamp.toDate().getTime();
            if (newest == null || time > newestTime) {
                newest = document;
                newestTime = time;
            }
        }
        return newest;
    }

    private void showActivity(TextView activity, Timestamp timestamp, String fallback) {
        if (timestamp != null) {
            activity.setText("Last activity: " + new SimpleDateFormat("dd MMM yyyy, HH:mm",
                    Locale.getDefault()).format(timestamp.toDate()));
        } else if (fallback != null && !fallback.isEmpty()) {
            activity.setText("Last activity: " + fallback);
        }
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private TextView textView(String text, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(getColor(color));
        if (bold) view.setTypeface(null, 1);
        return view;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }
}
