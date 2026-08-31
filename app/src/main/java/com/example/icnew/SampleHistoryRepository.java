package com.example.icnew;

import android.content.Context;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/** Stores analysis sample photos privately on this device; no network service is used. */
public final class SampleHistoryRepository {
    private static final String PREFS = "LocalSampleHistory";
    private static final String HISTORY_KEY = "records";

    private SampleHistoryRepository() { }

    public static void save(Context context, String sampleType, String classification, Bitmap sampleImage) {
        if (sampleImage == null) return;
        new Thread(() -> {
            try {
                String id = UUID.randomUUID().toString();
                File folder = new File(context.getFilesDir(), "sample-history");
                if (!folder.exists() && !folder.mkdirs()) return;
                File imageFile = new File(folder, id + ".jpg");
                try (FileOutputStream output = new FileOutputStream(imageFile)) {
                    sampleImage.compress(Bitmap.CompressFormat.JPEG, 85, output);
                }
                JSONArray records = getJsonRecords(context);
                JSONObject record = new JSONObject();
                record.put("id", id);
                record.put("sampleType", sampleType);
                record.put("classification", classification);
                record.put("imagePath", imageFile.getAbsolutePath());
                record.put("savedAt", new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date()));
                records.put(record);
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                        .putString(HISTORY_KEY, records.toString()).apply();
            } catch (Exception ignored) { }
        }).start();
    }

    public static void clear(Context context) {
        try {
            File folder = new File(context.getFilesDir(), "sample-history");
            File[] files = folder.listFiles();
            if (files != null) for (File file : files) if (file.isFile()) file.delete();
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(HISTORY_KEY).apply();
        } catch (Exception ignored) { }
    }
    public static ArrayList<SampleRecord> getAll(Context context) {
        ArrayList<SampleRecord> records = new ArrayList<>();
        try {
            JSONArray json = getJsonRecords(context);
            for (int i = json.length() - 1; i >= 0; i--) {
                JSONObject item = json.getJSONObject(i);
                records.add(new SampleRecord(item.optString("id"), item.optString("sampleType"), item.optString("classification"), item.optString("imagePath"), item.optString("savedAt")));
            }
        } catch (Exception ignored) { }
        return records;
    }

    private static JSONArray getJsonRecords(Context context) {
        try {
            return new JSONArray(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(HISTORY_KEY, "[]"));
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }

    public static final class SampleRecord {
        public final String id, sampleType, classification, imagePath, savedAt;
        SampleRecord(String id, String sampleType, String classification, String imagePath, String savedAt) {
            this.id = id;
            this.sampleType = sampleType;
            this.classification = classification;
            this.imagePath = imagePath;
            this.savedAt = savedAt;
        }
    }
}