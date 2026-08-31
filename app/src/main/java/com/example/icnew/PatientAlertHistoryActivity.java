package com.example.icnew;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class PatientAlertHistoryActivity extends AppCompatActivity {
    private final ArrayList<AlertItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_alert_history);

        ArrayAdapter<AlertItem> adapter = new ArrayAdapter<AlertItem>(
                this, R.layout.item_health_record_rich, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = convertView == null
                        ? LayoutInflater.from(getContext()).inflate(
                                R.layout.item_health_record_rich, parent, false)
                        : convertView;
                AlertItem item = getItem(position);

                ((TextView) row.findViewById(R.id.record_title))
                        .setText("Update from your doctor");
                ((TextView) row.findViewById(R.id.record_detail))
                        .setText(item.message);
                ((TextView) row.findViewById(R.id.record_date))
                        .setText(item.sentDate);

                TextView status = row.findViewById(R.id.record_status);
                status.setText(item.status);
                status.setTextColor(Color.WHITE);
                status.setBackground(createStatusBackground(item.status));
                return row;
            }
        };

        ((ListView) findViewById(R.id.patient_alert_list)).setAdapter(adapter);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String patientId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("alerts").whereEqualTo("patientId", patientId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    items.clear();
                    value.getDocuments().forEach(doc -> {
                        String status = doc.getString("status");
                        String note = doc.getString("doctorNote");
                        String message = doc.getString("message");
                        Timestamp createdAt = doc.getTimestamp("createdAt");

                        if (status == null || status.trim().isEmpty()) status = "NEW";
                        if (message == null || message.trim().isEmpty()) {
                            message = "Your urgent result was shared with your doctor.";
                        }
                        if (note != null && !note.trim().isEmpty()) {
                            message += "\n\nDoctor note: " + note.trim();
                        }

                        String sentDate = createdAt == null
                                ? "Sent date unavailable"
                                : "Sent " + new SimpleDateFormat(
                                        "dd MMM yyyy, HH:mm", Locale.getDefault())
                                        .format(createdAt.toDate());

                        items.add(new AlertItem(status.toUpperCase(Locale.getDefault()),
                                message, sentDate));
                    });

                    adapter.notifyDataSetChanged();
                    ((TextView) findViewById(R.id.patient_alert_count)).setText(
                            items.size() + " alert" + (items.size() == 1 ? "" : "s")
                                    + " shared with your doctor");
                });
    }

    private GradientDrawable createStatusBackground(String status) {
        int color;
        if ("RESOLVED".equalsIgnoreCase(status)) {
            color = getColor(R.color.health_success);
        } else if ("ACKNOWLEDGED".equalsIgnoreCase(status)) {
            color = getColor(R.color.health_warning);
        } else {
            color = getColor(R.color.health_urgent);
        }

        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(100f);
        return background;
    }

    private static class AlertItem {
        final String status;
        final String message;
        final String sentDate;

        AlertItem(String status, String message, String sentDate) {
            this.status = status;
            this.message = message;
            this.sentDate = sentDate;
        }
    }
}
