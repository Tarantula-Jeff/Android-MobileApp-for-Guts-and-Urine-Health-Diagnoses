package com.example.icnew;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SymptomTrackerActivity extends AppCompatActivity {
    private static final String PREFS = "SymptomTracker";
    private static final String KEY_ITEMS = "entries";
    private final ArrayList<Entry> entries = new ArrayList<>();
    private ArrayAdapter<Entry> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_tracker);

        adapter = new ArrayAdapter<Entry>(this, R.layout.item_health_record_rich, entries) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = convertView == null ? LayoutInflater.from(getContext())
                        .inflate(R.layout.item_health_record_rich, parent, false) : convertView;
                Entry entry = getItem(position);
                ((TextView) row.findViewById(R.id.record_title)).setText(entry.symptom);
                ((TextView) row.findViewById(R.id.record_detail)).setText(entry.notes.isEmpty()
                        ? "No additional notes" : entry.notes);
                ((TextView) row.findViewById(R.id.record_date)).setText(entry.date);
                TextView severity = row.findViewById(R.id.record_status);
                severity.setText(entry.severity.toUpperCase(Locale.getDefault()));
                severity.setTextColor(Color.WHITE);
                severity.setBackground(severityBackground(entry.severity));
                return row;
            }
        };
        ((ListView) findViewById(R.id.symptom_list)).setAdapter(adapter);
        loadEntries();

        findViewById(R.id.save_symptom).setOnClickListener(v -> saveEntry());
    }

    private void saveEntry() {
        EditText symptomField = findViewById(R.id.symptom_name);
        EditText notesField = findViewById(R.id.symptom_notes);
        String symptom = symptomField.getText().toString().trim();
        if (symptom.isEmpty()) {
            symptomField.setError("Enter a symptom first");
            return;
        }
        RadioGroup severityGroup = findViewById(R.id.severity_group);
        String severity = severityGroup.getCheckedRadioButtonId() == R.id.severity_severe ? "Severe"
                : severityGroup.getCheckedRadioButtonId() == R.id.severity_moderate ? "Moderate" : "Mild";
        entries.add(0, new Entry(symptom, notesField.getText().toString().trim(), severity,
                new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date())));
        persistEntries();
        adapter.notifyDataSetChanged();
        symptomField.setText("");
        notesField.setText("");
        severityGroup.check(R.id.severity_mild);
        Toast.makeText(this, "Symptom saved privately on this device", Toast.LENGTH_SHORT).show();
    }

    private void loadEntries() {
        String saved = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_ITEMS, "[]");
        try {
            JSONArray array = new JSONArray(saved);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                entries.add(new Entry(item.optString("symptom"), item.optString("notes"),
                        item.optString("severity", "Mild"), item.optString("date")));
            }
        } catch (Exception ignored) { }
        adapter.notifyDataSetChanged();
    }

    private void persistEntries() {
        JSONArray array = new JSONArray();
        for (Entry entry : entries) {
            try {
                JSONObject item = new JSONObject();
                item.put("symptom", entry.symptom);
                item.put("notes", entry.notes);
                item.put("severity", entry.severity);
                item.put("date", entry.date);
                array.put(item);
            } catch (Exception ignored) { }
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_ITEMS, array.toString()).apply();
    }

    private GradientDrawable severityBackground(String severity) {
        int color = "Severe".equalsIgnoreCase(severity) ? getColor(R.color.health_urgent)
                : "Moderate".equalsIgnoreCase(severity) ? getColor(R.color.health_warning)
                : getColor(R.color.health_success);
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(100f);
        return background;
    }

    private static class Entry {
        final String symptom, notes, severity, date;
        Entry(String symptom, String notes, String severity, String date) {
            this.symptom = symptom;
            this.notes = notes;
            this.severity = severity;
            this.date = date;
        }
    }
}
