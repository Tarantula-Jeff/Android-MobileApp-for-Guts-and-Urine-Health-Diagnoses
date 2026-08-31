package com.example.icnew;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class PatientDetailActivity extends AppCompatActivity {
    private final ArrayList<String> alerts = new ArrayList<>();
    private final ArrayList<String> results = new ArrayList<>();

    private ArrayAdapter<String> cardAdapter(ArrayList<String> data) {
        return new ArrayAdapter<String>(this, R.layout.item_health_record_rich, data) {
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                View row = convertView == null ? LayoutInflater.from(getContext()).inflate(R.layout.item_health_record_rich, parent, false) : convertView;
                String[] parts = getItem(position).split("\\n", 3);
                boolean isAlert = parts[0].equals("NEW") || parts[0].equals("ACKNOWLEDGED") || parts[0].equals("RESOLVED"); ((TextView) row.findViewById(R.id.record_title)).setText(isAlert ? "Urgent patient alert" : parts[0] + " analysis");
                ((TextView) row.findViewById(R.id.record_detail)).setText(parts.length > 1 ? parts[1] : "");
                ((TextView) row.findViewById(R.id.record_status)).setText(parts[0].startsWith("NEW") ? "NEW" : "HISTORY");
                ((TextView) row.findViewById(R.id.record_date)).setText("Patient record");
                return row;
            }
        };
    }
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);
        String patientId = getIntent().getStringExtra("patientId");
        ((TextView) findViewById(R.id.detail_patient_email)).setText(getIntent().getStringExtra("patientEmail"));
        ArrayAdapter<String> alertAdapter = cardAdapter(alerts);
        ArrayAdapter<String> resultAdapter = cardAdapter(results);
        ((ListView) findViewById(R.id.patient_previous_alerts)).setAdapter(alertAdapter);
        ((ListView) findViewById(R.id.patient_result_history)).setAdapter(resultAdapter);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("alerts").whereEqualTo("patientId", patientId).addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;
            alerts.clear();
            value.getDocuments().forEach(doc -> alerts.add((doc.getString("status") == null ? "NEW" : doc.getString("status").toUpperCase()) + "\\n" + doc.getString("message") + "\\n" + (doc.getTimestamp("createdAt") == null ? "Date unavailable" : new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(doc.getTimestamp("createdAt").toDate()))));
            alertAdapter.notifyDataSetChanged();
        });
        database.collection("analysisResults").whereEqualTo("patientId", patientId).addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;
            results.clear();
            value.getDocuments().forEach(doc -> results.add(doc.getString("analysisType") + "\\nResult: " + doc.getString("classification") + "\\n" + doc.getString("createdAtText")));
            resultAdapter.notifyDataSetChanged();
        });
    }
}