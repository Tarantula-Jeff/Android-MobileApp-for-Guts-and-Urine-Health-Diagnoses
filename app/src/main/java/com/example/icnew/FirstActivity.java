package com.example.icnew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirstActivity extends AppCompatActivity {

    EditText doctorPhoneNumberEditText;

    private static final int PICK_CONTACT_REQUEST = 1;
FirebaseAuth auth;
Button button;
TextView textView;
FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);
        user  = auth.getCurrentUser();
        if(user ==null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        else{
            textView.setText(user.getEmail());
        }

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();

            }
        });
        doctorPhoneNumberEditText = findViewById(R.id.doctor_phone_number_edit_text);
    }
    // Method to handle button click event
    public void saveDoctorPhoneNumber(View view) {
        String doctorPhoneNumber = doctorPhoneNumberEditText.getText().toString().trim();

        if (doctorPhoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save doctor's phone number to SharedPreferences
        SharedPreferences preferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("doctorPhoneNumber", doctorPhoneNumber);
        editor.apply();

        Toast.makeText(this, "Doctor's phone number saved successfully", Toast.LENGTH_SHORT).show();
    }


    public void selectContact(View view) {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    // Button click handler for selecting contact


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String phoneNumber = cursor.getString(numberIndex);
                TextView phoneNumberTextView = findViewById(R.id.doctor_phone_number_edit_text);
                phoneNumberTextView.setText(phoneNumber);
                cursor.close();
            }
        }


    }





    public void goToSecondActivity(View view){
        Intent intent = new Intent(this, SelectActivity.class);
        startActivity(intent);
    }
}