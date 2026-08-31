package com.example.icnew;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class SampleHistoryDetailActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_history_detail);
        ((TextView) findViewById(R.id.history_sample_type)).setText(getIntent().getStringExtra("sampleType") + " sample");
        ((TextView) findViewById(R.id.history_classification)).setText("Result: " + getIntent().getStringExtra("classification"));
        ((TextView) findViewById(R.id.history_date)).setText(getIntent().getStringExtra("savedAt"));
        String imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath == null || !new File(imagePath).exists()) return;
        ImageView image = findViewById(R.id.history_sample_image);
        image.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        image.setVisibility(View.VISIBLE);
    }
}