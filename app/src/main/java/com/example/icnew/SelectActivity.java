package com.example.icnew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);



        }
    public void StoolAnalyze(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void UrineAnalyze(View view) {
        Intent intent = new Intent(this, UrineAnalysis.class);
        startActivity(intent);
    }

    public void Chatchat(View view) {
        Intent intent = new Intent(this, SUChatbot.class);
        startActivity(intent);
    }


    public void menu(View view){
        finish();
    }
}