package com.example.icnew;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.SmsManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.icnew.ml.Urinemodel;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;




public class UrineAnalysis extends AppCompatActivity {

    Button camera, gallery;
    ImageView imageView;
    TextView result;
    int imageSize = 32;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.button);
        gallery = findViewById(R.id.button2);

        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);

        // Retrieve the doctor's phone number from SharedPreferences

        SharedPreferences preferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
        String doctorPhoneNumber = preferences.getString("doctorPhoneNumber", "");

        // Log the retrieved phone number (for debugging purposes)
        Log.d("DoctorPhoneNumber", "Doctor's Phone Number: " + doctorPhoneNumber);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 3);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent, 1);
            }
        });
    }

    public void classifyImage(Bitmap image){
        try {
            Urinemodel model = Urinemodel.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for(int i = 0; i < imageSize; i ++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Urinemodel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"Black", "Blue", "Brown", "Clear", "Green", "Orange", "Red", "Yellow"};

            SharedPreferences preferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
            String doctorPhoneNumber = preferences.getString("doctorPhoneNumber", "");

            if(classes[maxPos].equals("Red")){
                String mess1 ="URGENT: Patient's urine analysis detected pink-to-reddish color.Could be from beets or exercise, but no recent causes.Possible concerns: blood in Urine ";
                result.setText("Likely "+classes[maxPos]+ rdiagnoses());
                showConfirmationDialog(mess1);

               // result.setText("likely" + classes[maxPos]);

                // Retrieve the doctor's phone number from SharedPreferences



            }
            else if (classes[maxPos].equals("Black")){
                result.setText("Likely "+classes[maxPos]+bdiagnoses());
                String mess2 ="Urgent:Dark-colored urine, possibly indicating dehydration,medication side effects or"
                        +" liver disease." +
                        " Please advise on next steps.";
                showConfirmationDialog(mess2);

            } else if (classes[maxPos].equals("Clear")){
                result.setText("Likely "+classes[maxPos]+cdiagnoses());
            } else if (classes[maxPos].equals("Green")){
                String mess3="URGENT:unusual greenish urine detected.  " +
                        " Possible causes:food dyes, medications,"+
                        "Genetic condition like familial benign hypercalcemia " +
                        " Please advise on necessary action.";
                showConfirmationDialog(mess3);
                result.setText("Likely "+classes[maxPos]+gdiagnoses());
            } else if (classes[maxPos].equals("Orange")){
                result.setText("Likely "+classes[maxPos]+odiagnoses());
            } else if (classes[maxPos].equals("Yellow")){
                result.setText("Likely "+classes[maxPos]+ydiagnoses());
            } else if (classes[maxPos].equals("Blue")){
                result.setText("Likely "+classes[maxPos]+bldiagnoses());
                String mess4="URGENT: Urine analysis detected blue urine. " +
                        "Possible causes:bacterial infection ,familial benign hypercalcemia " +
                        ",dyes from food, medications," +
                        "Please advise on necessary action.\n";
                showConfirmationDialog(mess4);

            } else if (classes[maxPos].equals("Brown")){
                result.setText("Likely "+classes[maxPos]+brdiagnoses());

            } else  {
                result.setText("Likely "+classes[maxPos]);
            }

            //result.setText("likely"+classes[maxPos]);
            //  String home =result.setText(classes[maxPos]);


            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }


    public void showConfirmationDialog(String docMess) {
        SharedPreferences preferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
        String doctorPhoneNumber = preferences.getString("doctorPhoneNumber", "");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogue, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnSend = dialog.findViewById(R.id.btn_send);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Call the method to send the message to the doctor
                sendSMS(doctorPhoneNumber, docMess);
                dialog.dismiss();
            }
        });
    }
    public String cdiagnoses(){
        String clear ="                                                         "+
                "Clear urine:   You are good. " +
                "You may be drinking too much water. " +
                "Your body needs water to stay hydrated and " +
                "function properly but drinking more than " +
                "the daily recommended amount of water can lower your " +
                "salt rob you of your body of electrolytes, bringing it " +
                "below the level of what your body needs.\n" +
                "However if your urine is clear and  If you’re not consuming large amounts " +
                "of water and have ongoing clear urine, that may signal an underlying kidney problem , " +
                "diabetes, liver problems like cirrhosis and viral hepatitis. In this situation, " +
                "it’s best to see a doctor to get answers.\n";
        return clear;
    }
    public String bdiagnoses(){
        String black="                                                         "+
                "Dark urine:   In most cases, indicates dehydration. " +
                "Medications: Dark brown urine can also be a side effect of certain medications," +
                " including metronidazole (Flagyl) and nitrofurantoin (Furadantin)," +
                " chloroquine (Aralen), cascara or sennabased laxatives, and methocarbamol.  " +
                "Eating large amounts of rhubarb, aloe, or fava beans can cause this.\n" +
                "\n" +
                "Brown, tea-colored urine could be a symptom of rhabdomyolosis, a breakdown of muscle tissue that is a serious medical condition.\n" +
                " A condition called porphyria can cause a buildup of the natural chemicals in your bloodstream and cause rusty or brown urine.\n" +
                " Dark brown urine can also indicate liver disease, as it can be caused by bile getting into your urine.\n" +
                " Intense physical activity, especially running, can cause dark brown urine, known as exertional hematuria.\n" +
                "This isn’t considered unusual. When your urine is dark because of exercise, it’ll typically resolve with some rest within a few hours.\n" +
                "However  If you frequently see dark brown urine after exercise, or if your urine doesn’t return to normal after 48 hours, I highly recommend you should speak with a doctor about possible underlying causes\n";
                return black;
    }
    public String rdiagnoses(){
        String red="                                                         "+
                "Pink- to reddish-color:   This could be caused by eating certain kinds of edibles such as beets, blueberries or within the last day or so. " +
                "Hard exercise, such as long-distance running and A tuberculosis medicine such as rifampin (Rifadin, Rimactane) also can cause this bleeding. " +
                "However, if you do not fall under any of the above, then you should be concerned. This could be a sign of: Blood in your urine. Kidney disease. Cancers of the kidney or bladder. Kidney stones. A urinary tract infection. Prostate problems. Lead or mercury poisoning. Contact your doctor as soon as possible if the color doesn’t return to yellow.\n" +
                "Possible Remedy:\n" +
                "See your doctor as soon as possible if persists. You can just allow this app to send the message to your doctor too.\n";
        return red;
    } public String gdiagnoses(){
        String green="                                                         "+
                "Green urine:   can be caused by dyes. Some brightly colored food dyes(think heavily something you ate) can cause this. Dyes used for some kidney and bladder tests can turn urine blue.  A medicine for depression called amitriptyline can make urine look greenish-blue. So can a treatment for ulcers and acid reflux called cimetidine (Tagamet HB). A water pill called triamterene (Dyrenium) also can turn urine greenish-blue\n" +
                "Health problems\n" +
                "Pseudomonas aeruginosa bacterial infection can also cause your urine to turn blue, green, or indigo purple. A condition called familial benign hypercalcemia can also cause blue or green urine. Low to moderate calcium levels may appear in your urine and change color when you have this condition. Many people with this genetic condition don’t have symptoms that they notice.\n" +
                "If you do not fall under any of the above, quickly see you doctor.\n";
        return green;
    } public String odiagnoses(){
        String orange="                                                         "+
                "Don’t freak out. " +
                "This could happen if you have taken medicines like Phenazopyridine and some constipation medicines can turn urine orange. So can sulfasalazine (Azulfidine), a medicine that lessens swelling and irritation. Some chemotherapy medicines for cancer also can make urine look orange. Vitamins, such as A and B-12, can turn urine orange or yellow-orange. So no need to worry.\n" +
                "You only need to worry or have to take measures is when none of the above are met and this keeps on. As this could be a sign of a problem with the liver or bile duct, mainly if you also have light-colored stools.\n" +
                " Dehydration also can make your urine look orange\n" +
                "Possible Remedy:\n" +
                "Drink enough water.\n" +
                "If drinking enough water doesn’t change it nor even after you are done taking the medicine, see you doctor.\n";
        return orange;
    } public String ydiagnoses(){
        String yellow="                                                         "+
                "Good news! You’re in the preferred section of the urine color chart. " +
                "The urochrome pigment naturally in your urine becomes more diluted as you drink water. " +
                " In most situations, the color of your urine will depend on how diluted this pigment is." +
                " Having a lot of B vitamins in your bloodstream can also cause urine to appear neon yellow";
        return yellow;
    } public String brdiagnoses(){
        String brown="                                                         "+
                "Your dehydration level just crossed a line into a more worrisome status. Get Liquid as soon as possible . Urine that is dark brown also could be caused by bile getting into your urine, a sign of liver disease. Rusty or brown-colored pee also is a symptom of porphyria, a rare disorder affecting the skin and nervous system. If rehydrating doesn’t lighten up your urine, see your doctor\n" +
                "Possible Remedy\n" +
                "Drink enough water.\n" +
                "If the color does not lighten up, You need to see your doctor\n";
        return brown;
    } public String bldiagnoses(){
        String blue="                                                         "+
                "Blue urine:   Can be caused by dyes. Some brightly colored food dyes(think heavily something you ate) can cause this. Dyes used for some kidney and bladder tests can turn urine blue.  A medicine for depression called amitriptyline can make urine look greenish-blue. So can a treatment for ulcers and acid reflux called cimetidine (Tagamet HB). A water pill called triamterene (Dyrenium) also can turn urine greenish-blue\n" +
                "Health problems\n" +
                "Pseudomonas aeruginosa bacterial infection can also cause your urine to turn blue, green, or indigo purple. A condition called familial benign hypercalcemia can also cause blue or green urine. Low to moderate calcium levels may appear in your urine and change color when you have this condition. Many people with this genetic condition don’t have symptoms that they notice.\n" +
                "If you do not fall under any of the above, quickly see you doctor.\n";
        return blue;
    }

    // Method to send an SMS
    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void goBack(View view){
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == 3){
                Bitmap image = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }else{
                Uri dat = data.getData();
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}