package com.example.icnew;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SampleHistoryActivity extends AppCompatActivity {
    private final ArrayList<String> labels = new ArrayList<>();
    private ArrayList<SampleHistoryRepository.SampleRecord> records;
    private ArrayAdapter<String> adapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_history);
        adapter = new ArrayAdapter<String>(this, R.layout.item_health_record_rich, labels) {
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                View row = convertView == null ? LayoutInflater.from(getContext()).inflate(R.layout.item_health_record_rich, parent, false) : convertView;
                SampleHistoryRepository.SampleRecord record = records.get(position);
                ((TextView) row.findViewById(R.id.record_title)).setText(record.sampleType + " analysis");
                ((TextView) row.findViewById(R.id.record_detail)).setText("Result: " + record.classification);
                ((TextView) row.findViewById(R.id.record_date)).setText(record.savedAt);
                ((TextView) row.findViewById(R.id.record_status)).setText(record.classification.toUpperCase());
                ImageView image = row.findViewById(R.id.record_image);
                image.setImageBitmap(BitmapFactory.decodeFile(record.imagePath));
                return row;
            }
        };        ListView list = findViewById(R.id.sample_history_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> {
            SampleHistoryRepository.SampleRecord record = records.get(position);
            Intent intent = new Intent(this, SampleHistoryDetailActivity.class);
            intent.putExtra("sampleType", record.sampleType);
            intent.putExtra("classification", record.classification);
            intent.putExtra("savedAt", record.savedAt);
            intent.putExtra("imagePath", record.imagePath);
            startActivity(intent);
        });
    }

    @Override protected void onResume() {
        super.onResume();
        records = SampleHistoryRepository.getAll(this);
        labels.clear();
        for (SampleHistoryRepository.SampleRecord record : records) {
            labels.add(record.sampleType + " analysis\n" + record.savedAt + "\nResult: " + record.classification);
        }
        adapter.notifyDataSetChanged();
        ((TextView) findViewById(R.id.history_count)).setText(records.size() + " saved sample" + (records.size() == 1 ? "" : "s") + " on this phone");
    }
}