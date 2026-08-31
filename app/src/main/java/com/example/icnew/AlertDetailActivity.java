package com.example.icnew;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.view.View;
import android.graphics.BitmapFactory;

import java.net.URL;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AlertDetailActivity extends AppCompatActivity {
    private String alertId;
    private EditText note;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_detail);
        alertId = getIntent().getStringExtra("alertId");
        ((TextView) findViewById(R.id.alert_patient)).setText(getIntent().getStringExtra("patientEmail"));
        ((TextView) findViewById(R.id.alert_message)).setText(getIntent().getStringExtra("message"));
        String imageUrl = getIntent().getStringExtra("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) loadImage(imageUrl);
        note = findViewById(R.id.doctor_note);
        findViewById(R.id.acknowledge_alert).setOnClickListener(view -> updateAlert("acknowledged"));
        findViewById(R.id.resolve_alert).setOnClickListener(view -> updateAlert("resolved"));
    }

    private void loadImage(String imageUrl) {
        ImageView image = findViewById(R.id.alert_image);
        image.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                android.graphics.Bitmap bitmap = BitmapFactory.decodeStream(new URL(imageUrl).openStream());
                runOnUiThread(() -> image.setImageBitmap(bitmap));
            } catch (Exception ignored) { }
        }).start();
    }

    private void updateAlert(String status) {
        if (alertId == null) return;
        Map<String, Object> update = new HashMap<>();
        update.put("status", status);
        update.put("doctorNote", note.getText().toString().trim());
        update.put("updatedAt", FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance().collection("alerts").document(alertId).update(update)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Alert marked " + status + ".", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(error -> Toast.makeText(this, "Could not update this alert.", Toast.LENGTH_SHORT).show());
    }
}
