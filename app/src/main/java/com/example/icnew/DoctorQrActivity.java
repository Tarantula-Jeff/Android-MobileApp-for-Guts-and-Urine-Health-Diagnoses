package com.example.icnew;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

public class DoctorQrActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_qr);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
        String doctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ((TextView) findViewById(R.id.qr_doctor_id)).setText(doctorId);
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    "gutguardian:doctor:" + doctorId, BarcodeFormat.QR_CODE, 900, 900);
            Bitmap bitmap = Bitmap.createBitmap(900, 900, Bitmap.Config.RGB_565);
            for (int x = 0; x < 900; x++) {
                for (int y = 0; y < 900; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? 0xFF111827 : 0xFFFFFFFF);
                }
            }
            ((ImageView) findViewById(R.id.doctor_qr_image)).setImageBitmap(bitmap);
        } catch (Exception ignored) { }
    }
}
