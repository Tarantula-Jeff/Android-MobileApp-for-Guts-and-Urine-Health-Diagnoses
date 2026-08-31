package com.example.icnew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class DoctorAlertsActivity extends AppCompatActivity {
    private final ArrayList<String> alerts = new ArrayList<>();
    private final ArrayList<String> ids = new ArrayList<>();
    private final ArrayList<String> emails = new ArrayList<>();
    private final ArrayList<String> messages = new ArrayList<>();
    private final ArrayList<String> imageUrls = new ArrayList<>();
    private final ArrayList<String> statuses = new ArrayList<>();
    private final ArrayList<String> dates = new ArrayList<>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_alerts);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_health_record_rich, alerts) { @Override public View getView(int p, View v, ViewGroup parent) { View row = v == null ? LayoutInflater.from(getContext()).inflate(R.layout.item_health_record_rich, parent, false) : v; String[] a = getItem(p).split("\\n", 2); ((TextView) row.findViewById(R.id.record_title)).setText(emails.get(p)); ((TextView) row.findViewById(R.id.record_detail)).setText(messages.get(p)); TextView chip = row.findViewById(R.id.record_status); chip.setText(statuses.get(p)); chip.setBackgroundColor(getColor(statuses.get(p).equals("NEW") ? R.color.health_urgent : statuses.get(p).equals("RESOLVED") ? R.color.health_success : R.color.health_warning)); chip.setTextColor(getColor(R.color.white)); ((TextView) row.findViewById(R.id.record_date)).setText(dates.get(p)); return row; } };
        ListView list = findViewById(R.id.doctor_alerts_list);
        list.setAdapter(adapter);
        findViewById(R.id.alerts_back).setOnClickListener(view -> finish());
        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, AlertDetailActivity.class);
            intent.putExtra("alertId", ids.get(position));
            intent.putExtra("patientEmail", emails.get(position));
            intent.putExtra("message", messages.get(position));
            intent.putExtra("imageUrl", imageUrls.get(position));
            startActivity(intent);
        });
        String doctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("alerts").whereEqualTo("doctorId", doctorId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    alerts.clear(); ids.clear(); emails.clear(); messages.clear(); imageUrls.clear(); statuses.clear(); dates.clear();
                    String preferredFilter = getSharedPreferences("DoctorProfilePrefs", MODE_PRIVATE).getString("alertFilter", "all");
                    value.getDocuments().forEach(doc -> {
                        String status = doc.getString("status");
                        String email = doc.getString("patientEmail");
                        if (!"all".equals(preferredFilter) && !preferredFilter.equals(status == null ? "new" : status.toLowerCase())) return;
                        String message = doc.getString("message");
                        String cardStatus = status == null ? "NEW" : status.toUpperCase();
                        com.google.firebase.Timestamp created = doc.getTimestamp("createdAt");
                        ids.add(doc.getId());
                        emails.add(email);
                        messages.add(message);
                        imageUrls.add(doc.getString("imageUrl"));
                        statuses.add(cardStatus);
                        dates.add(created == null ? "Date unavailable" : new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(created.toDate()));
                        alerts.add((status == null ? "NEW" : status.toUpperCase()) + " - " + email + "\n" + message);
                    });
                    adapter.notifyDataSetChanged();
                    ((TextView) findViewById(R.id.doctor_alert_page_count)).setText(alerts.size() + " alert" + (alerts.size() == 1 ? "" : "s"));
                    findViewById(R.id.alerts_empty).setVisibility(alerts.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }
}
