package com.example.icnew;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/** Sends an urgent finding using the delivery method selected by the patient. */
public final class AlertDispatcher {
    private AlertDispatcher() { }

    public static void dispatch(Context context, String message, Bitmap image) {
        SharedPreferences preferences = context.getSharedPreferences("DoctorPrefs", Context.MODE_PRIVATE);
        if ("sms".equals(preferences.getString("deliveryMethod", "app"))) {
            sendSms(context, preferences.getString("doctorPhoneNumber", ""), message);
            return;
        }

        String doctorId = preferences.getString("doctorUid", "");
        FirebaseUser patient = FirebaseAuth.getInstance().getCurrentUser();
        if (doctorId.isEmpty() || patient == null) {
            Toast.makeText(context, "Add your doctor's app ID in Settings before sending in-app alerts.", Toast.LENGTH_LONG).show();
            return;
        }
        if (preferences.getBoolean("includePicture", true) && image != null) {
            uploadImageThenCreateAlert(context, doctorId, patient, message, image);
        } else {
            createAlert(context, doctorId, patient, message, null);
        }
    }

    private static void uploadImageThenCreateAlert(Context context, String doctorId, FirebaseUser patient, String message, Bitmap image) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 82, bytes);
        StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("alert-images/" + patient.getUid() + "/" + System.currentTimeMillis() + ".jpg");
        reference.putBytes(bytes.toByteArray()).continueWithTask(task -> reference.getDownloadUrl())
                .addOnSuccessListener(uri -> createAlert(context, doctorId, patient, message, uri.toString()))
                .addOnFailureListener(error -> {
                    createAlert(context, doctorId, patient, message, null);
                    Toast.makeText(context, "Image could not be attached; the alert was sent without it.", Toast.LENGTH_LONG).show();
                });
    }

    private static void createAlert(Context context, String doctorId, FirebaseUser patient, String message, String imageUrl) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("doctorId", doctorId);
        alert.put("patientId", patient.getUid());
        alert.put("patientEmail", patient.getEmail());
        alert.put("message", message);
        alert.put("imageUrl", imageUrl);
        alert.put("status", "new");
        alert.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        boolean online = NetworkStatus.isOnline(context);
        FirebaseFirestore.getInstance().collection("alerts").add(alert)
                .addOnSuccessListener(documentReference -> Toast.makeText(context, online
                        ? "Alert sent securely to your doctor's dashboard."
                        : "Alert saved. It will sync securely when you are online.", Toast.LENGTH_LONG).show())
                .addOnFailureListener(error -> Toast.makeText(context, "Could not send the in-app alert: " + error.getMessage(), Toast.LENGTH_LONG).show());
    }

    private static void sendSms(Context context, String phoneNumber, String message) {
        if (phoneNumber.isEmpty()) {
            Toast.makeText(context, "Add your doctor's phone number in Settings before sending SMS.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(context, "SMS alert sent.", Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            Toast.makeText(context, "SMS failed. Please check SMS permission and try again.", Toast.LENGTH_LONG).show();
        }
    }
}
