package com.example.icnew;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class EmergencyGuidanceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_guidance);
        findViewById(R.id.call_emergency).setOnClickListener(view ->
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))));
    }
}
