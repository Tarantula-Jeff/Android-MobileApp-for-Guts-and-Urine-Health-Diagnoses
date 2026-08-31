package com.example.icnew;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Toast;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.io.File;
import java.io.FileOutputStream;

public class HealthSummaryActivity extends AppCompatActivity {
    private static final long WEEK_MILLIS = 7L * 24L * 60L * 60L * 1000L;
    private final ArrayList<TimelineEntry> timeline = new ArrayList<>();
    private String summary;
    private TextView summaryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_summary);
        summaryText = findViewById(R.id.summary_text);
        addLocalTimelineEntries();
        refreshSummary();
        findViewById(R.id.share_summary).setOnClickListener(v -> shareSummary());
        findViewById(R.id.export_summary_pdf).setOnClickListener(v -> exportPdf());
        loadWeeklyAlerts();
    }

    private void loadWeeklyAlerts() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String patientId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long cutoff = System.currentTimeMillis() - WEEK_MILLIS;
        FirebaseFirestore.getInstance().collection("alerts").whereEqualTo("patientId", patientId).get()
                .addOnSuccessListener(value -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                        Timestamp createdAt = doc.getTimestamp("createdAt");
                        if (createdAt == null || createdAt.toDate().getTime() < cutoff) continue;
                        String status = doc.getString("status");
                        if (status == null || status.trim().isEmpty()) status = "NEW";
                        String message = doc.getString("message");
                        if (message == null || message.trim().isEmpty()) message = "Urgent result shared with doctor";
                        timeline.add(new TimelineEntry(createdAt.toDate().getTime(),
                                "Urgent alert [" + status.toUpperCase(Locale.getDefault()) + "]: " + message));
                    }
                    refreshSummary();
                })
                .addOnFailureListener(error -> {
                    summaryText.setText(summary + "\n\nOnline urgent alerts could not be loaded right now.");
                });
    }

    private void addLocalTimelineEntries() {
        long cutoff = System.currentTimeMillis() - WEEK_MILLIS;
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

        for (SampleHistoryRepository.SampleRecord sample : SampleHistoryRepository.getAll(this)) {
            try {
                long date = format.parse(sample.savedAt).getTime();
                if (date >= cutoff) {
                    timeline.add(new TimelineEntry(date, sample.sampleType + " analysis: "
                            + sample.classification));
                }
            } catch (Exception ignored) { }
        }

        try {
            JSONArray entries = new JSONArray(getSharedPreferences("SymptomTracker", MODE_PRIVATE)
                    .getString("entries", "[]"));
            for (int i = 0; i < entries.length(); i++) {
                JSONObject entry = entries.getJSONObject(i);
                long date = format.parse(entry.optString("date")).getTime();
                if (date >= cutoff) {
                    String item = "Symptom: " + entry.optString("symptom") + " ("
                            + entry.optString("severity", "Mild") + ")";
                    String notes = entry.optString("notes").trim();
                    if (!notes.isEmpty()) item += " - " + notes;
                    timeline.add(new TimelineEntry(date, item));
                }
            }
        } catch (Exception ignored) { }
    }

    private void refreshSummary() {
        Collections.sort(timeline, (first, second) -> Long.compare(second.time, first.time));
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM - HH:mm", Locale.getDefault());
        StringBuilder text = new StringBuilder();
        text.append("GutGuardian weekly health summary\n");
        text.append("Last 7 days, created ")
                .append(new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        .format(new Date()))
                .append("\n\n");
        text.append("This is a personal record from the app, not a medical diagnosis. ");
        text.append("Seek urgent medical care for severe or worsening symptoms.\n\n");
        text.append("YOUR TIMELINE\n");

        if (timeline.isEmpty()) {
            text.append("No analysis results, symptom entries, or urgent alerts were recorded in the last 7 days.");
        } else {
            for (TimelineEntry entry : timeline) {
                text.append("- ").append(format.format(new Date(entry.time))).append(": ")
                        .append(entry.text).append("\n");
            }
        }

        text.append("\nOnly local analysis and symptom records, plus your in-app urgent alerts, are included.");
        summary = text.toString();
        summaryText.setText(summary);
    }

    private void shareSummary() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "GutGuardian weekly health summary");
        send.putExtra(Intent.EXTRA_TEXT, summary);
        startActivity(Intent.createChooser(send, "Share weekly health summary"));
    }

    private void exportPdf() {
        PdfDocument document = new PdfDocument();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xFF172033);
        paint.setTextSize(12f);
        int pageNumber = 1;
        PdfDocument.Page page = document.startPage(new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create());
        Canvas canvas = page.getCanvas();
        int y = 56;
        for (String rawLine : summary.split("\\n")) {
            String remaining = rawLine.isEmpty() ? " " : rawLine;
            while (!remaining.isEmpty()) {
                int length = Math.min(78, remaining.length());
                if (length < remaining.length()) {
                    int space = remaining.lastIndexOf(" ", length);
                    if (space > 0) length = space;
                }
                String line = remaining.substring(0, length).trim();
                if (y > 790) {
                    document.finishPage(page);
                    pageNumber++;
                    page = document.startPage(new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create());
                    canvas = page.getCanvas();
                    y = 56;
                }
                canvas.drawText(line, 42, y, paint);
                y += 19;
                remaining = remaining.substring(length).trim();
            }
        }
        document.finishPage(page);
        try {
            File folder = new File(getCacheDir(), "exports");
            if (!folder.exists() && !folder.mkdirs()) throw new Exception("Could not create export folder");
            File pdf = new File(folder, "GutGuardian-weekly-summary.pdf");
            try (FileOutputStream output = new FileOutputStream(pdf)) {
                document.writeTo(output);
            }
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".healthsummary", pdf);
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("application/pdf");
            send.putExtra(Intent.EXTRA_STREAM, uri);
            send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(send, "Share weekly summary PDF"));
        } catch (Exception error) {
            Toast.makeText(this, "Could not create PDF: " + error.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            document.close();
        }
    }
    private static class TimelineEntry {
        final long time;
        final String text;

        TimelineEntry(long time, String text) {
            this.time = time;
            this.text = text;
        }
    }
}
