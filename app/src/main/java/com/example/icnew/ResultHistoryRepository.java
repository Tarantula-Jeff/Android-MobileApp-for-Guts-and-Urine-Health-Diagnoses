package com.example.icnew;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Syncs text-only results for a patient's linked doctor. Sample images never leave the device. */
public final class ResultHistoryRepository {
    private ResultHistoryRepository() { }

    public static void save(Context context, String analysisType, String classification) {
        FirebaseUser patient = FirebaseAuth.getInstance().getCurrentUser();
        if (patient == null) return;
        SharedPreferences preferences = context.getSharedPreferences("DoctorPrefs", Context.MODE_PRIVATE);
        String rememberedDoctorId = preferences.getString("doctorUid", "");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users").document(patient.getUid()).get()
                .addOnSuccessListener(profile -> {
                    String doctorId = profile.getString("doctorId");
                    saveForDoctor(database, patient, doctorId == null || doctorId.trim().isEmpty() ? rememberedDoctorId : doctorId, analysisType, classification);
                })
                .addOnFailureListener(error -> saveForDoctor(database, patient, rememberedDoctorId, analysisType, classification));
    }

    private static void saveForDoctor(FirebaseFirestore database, FirebaseUser patient, String doctorId, String analysisType, String classification) {
        if (doctorId == null || doctorId.trim().isEmpty()) return;
        Map<String, Object> result = new HashMap<>();
        result.put("patientId", patient.getUid());
        result.put("patientEmail", patient.getEmail());
        result.put("doctorId", doctorId);
        result.put("analysisType", analysisType);
        result.put("classification", classification);
        result.put("createdAtText", new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date()));
        result.put("createdAt", FieldValue.serverTimestamp());
        database.collection("analysisResults").add(result);
    }
}