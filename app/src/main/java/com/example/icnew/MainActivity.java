package com.example.icnew;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.icnew.ml.Stoolmodel;
import com.example.icnew.ml.Stoolmodel2;
import com.example.icnew.ml.Stoolmodel20;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;




public class MainActivity extends AppCompatActivity {

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
            Stoolmodel model = Stoolmodel.newInstance(getApplicationContext());

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
            Stoolmodel.Outputs outputs = model.process(inputFeature0);
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
            String[] classes ={"BlackTarry","Greenpoop","Mucus","PENCILorFLAT","Redpoop","invalidsample",
                    "type1","type2","type3","type4","type5","type6","type7"};


                // result.setText("likely" + classes[maxPos]);

                // Retrieve the doctor's phone number from SharedPreferences
                SharedPreferences preferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
                String doctorPhoneNumber = preferences.getString("doctorPhoneNumber", "");



             if (classes[maxPos].equals("BlackTarry")){
                result.setText("Likely "+classes[maxPos]+btdiagnoses());
                String smess1 ="URGENT: Black tarry sample detected" +
                        "possibly: upper GI bleeding.Symptoms include faintness,rapid heartbeat, short breaths,abdominal pains." +
                        "Please advise needed";
                showConfirmationDialog(smess1);
            } else if (classes[maxPos].equals("type1")){
                result.setText("Likely "+classes[maxPos]+t1diagnoses());
            } else if (classes[maxPos].equals("type2")){
                result.setText("Likely "+classes[maxPos]+t2diagnoses());
            } else if (classes[maxPos].equals("type3")){
                result.setText("Likely "+classes[maxPos]+t3diagnoses());
            } else if (classes[maxPos].equals("type4")){
                result.setText("Likely "+classes[maxPos]+t4diagnoses());
            } else if (classes[maxPos].equals("type5")){
                result.setText("Likely "+classes[maxPos]+t5diagnoses());
            } else if (classes[maxPos].equals("type6")){
                result.setText("Likely "+classes[maxPos]+t6diagnoses());
            } else if (classes[maxPos].equals("type7")){
                result.setText("Likely "+classes[maxPos]+t7diagnoses());
            } else if (classes[maxPos].equals("PENCILorFLAT")){
                result.setText("Likely "+classes[maxPos]+fpdiagnoses());
                String smess2 ="this is death";
                showConfirmationDialog(smess2);
            } else if (classes[maxPos].equals("Greenpoop")){
                result.setText("Likely "+classes[maxPos]+gpdiagnoses());
                String smess4 ="this is death";
                showConfirmationDialog(smess4);
            } else if (classes[maxPos].equals("Redpoop")){
                result.setText("Likely "+classes[maxPos]+rpdiagnoses());
                String smess5 ="URGENT:Shades of red detected.Possibly diet,medication,bleeding in intestine.Potential Causes:hemorrhoids,polyps,CRC,or IBD.Please advise on further diagnosis";
                showConfirmationDialog(smess5);
            }
            else if (classes[maxPos].equals("Mucus")){
            result.setText("Likely "+classes[maxPos]+mdiagnoses());
                String smess6 ="URGENT:Mucus detected.Possible causes:Crohn's disease,cystic fibrosis, ulcerative " +
                        "colitis,IBS,malabsorption and CRC.Please advise on further diagnosis.";
                showConfirmationDialog(smess6);
            }
            else  {
                result.setText("Likely "+classes[maxPos]);
            }



            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    public void showConfirmationDialog(String sdocMess) {
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
                sendSMS(doctorPhoneNumber, sdocMess);
                dialog.dismiss();
            }
        });
    }

   public String btdiagnoses(){
        String bt="                                                         "+
                "Black tarry:   stools may be due to bleeding in" +
                " the upper part of the GI(gastrointestinal) tract, " +
                "such as the esophagus, stomach, or the first part of the small intestine." +
                " The blood  is darker because it gets" +
                " digested on its way through the GI TRACT.\n" +
                "\n" +
                "See the doctor immediately if it is accompanied by these symptoms too\n" +
                "1. You are faint\n" +
                "2. Pale\n" +
                "3. Experiencing a fast heartbeat\n" +
                "4. Having low blood pressure \n" +
                "5.Short breath\n" +
                "6. Feeling abdominal pains\n";
        return bt;
   }
    public String t1diagnoses(){
        String t1="                                                         "+
                "Marbles Appearance in nature:   Hard and separate little lumps that look like nuts and are hard to pass. Indicates: These little pellets typically mean you’re constipated. It shouldn’t happen frequently\n" +
                "Remedy\n" +
                "Eat more fiber. Adults should aim for 25-35 grams daily from fruits to veggies.\n" +
                "Manage your stress.\n" +
                "Take exercise\n" +
                "Consider some probiotics or fermented foods like tempeh, kimchee, pickled foods etc.\n";
        return t1;
    } public String t2diagnoses(){
        String t2="                                                         "+
                "Caterpillar Appearance:   Log-shaped but lumpy in nature. Indicates: Here we have another sign of constipation that, again, shouldn’t happen frequentlyThis stool also can be a sign that you’re constipated.\n" +
                "Remedy \n" +
                "Try to get more fiber in your diet and drink more water to move things along.\n" +
                "Eat more fiber. Adults should aim for 25-35 grams daily from fruits to veggies.\n" +
                "Manage your stress.\n" +
                "Take exercise\n" +
                "Consider some probiotics or fermented foods like tempeh, kimchee, pickled foods etc.\n";
        return t2;
    } public String t3diagnoses(){
        String t3="                                                         "+
                "Hot dog Appearance:   Log-shaped with some cracks on the surface. Indicates: This is the gold standard of poop, especially if it’s somewhat soft and easy to pass out." +
                " Your guts are healthy. Keep up with your healthy eating.";
        return t3;
    } public String t4diagnoses(){
        String t4="                                                         "+
                "Snake Appearance:   Smooth and snake-like. " +
                "Indicates: Hurry this is a normal poop that should happen every 1–3 days and no need to worry about it";
        return t4;
    } public String t5diagnoses(){
        String t5="                                                         "+
                "Amoebas Appearance:   These are easy to pass, but you may feel a sense of urgency about getting to the bathroom. That can be a sign of mild diarrhea. Most of the time, it goes away on its own in a couple of days\n" +
                "Indicates: This type of poop means you’re lacking fiber\n" +
                "Remedy\n" +
                "and should find ways to add some to your diet through cereal or vegetables\n";
        return t5;
    } public String t6diagnoses(){
        String t6="                                                         "+
                "Soft serve Appearance:   Fluffy and mushy with ragged edges. Indicates: This too-soft consistency could be a sign of mild diarrhea. If you have these more than three times a day, you have diarrhea.\n" +
                "Remedy\n" +
                "Try drinking more water and electrolyte-infused beverages to help improve this.\n" +
                "Make sure to drink plenty of fluids. Water is good," +
                " but you also need to replace the minerals you’re losing (called electrolytes). Fruit juices and soup can help.\n";
        return t6;
    } public String t7diagnoses(){
        String t7="                                                         "+
                "Watery Appearance:   Completely watery with no solid pieces. " +
                "Indicates: In other words, you’ve got the runs or diarrhea. " +
                "This means your stool moved through your bowels very quickly and didn’t form into a healthy poop.This could also means you have an inflammation in your digestive system.\n" +
                "Remedy\n" +
                "See your doctor if you have more than three of these a day for longer than 2 days. You should check with your doctor if you also have other signs of dehydration (dry mouth, sleepiness, headache, or dizziness), severe pain in your tummy or rear end, or a fever of 102 degrees or higher\n";
        return t7;
    } public String fpdiagnoses(){
        String fp="                                                         "+
                "Flat poop:   can be a sign of constipation, diarrhea irritable bowel sybdrome(IBS), enlarged prostate or colorectal cancer. Flat poop could also be due to changes of diet.\n" +
                "However it would be best if you consider seeing talking with your doctor if unexplained changes lasts a few days.\n" +
                "Remedy\n" +
                "It is best to notify your doctor if this change in your stool pattern is consistent and not changed.\n" +
                "However, you can try the following below, to help\n" +
                "1. Increase fibre intake by eating more whole grains as well as fruits and veggies with skin whenever possible.\n" +
                "2.Drink plenty of water to make stools easier to pass\n" +
                "3, Increase physical activity\n" +
                "4.Taking steps to reducing stress. This could be done through meditation, deep breathing and soft music  etc.\n" +
                "\n" +
                "Pencil- thin poop\n" +
                "Narrow stools that happens now and then probably are harmless but some cases can mean aa sign that the colon is narrowing or has a blockage. And that could be colon cancer or irritable bowel syndrome\n" +
                "You should definitely see your doctor or allow this app to send a message to your doctor if this poo is consistent and  accompanied by these symptoms:\n" +
                "A. Blood in stool or on the toilet paper\n" +
                "B. Changes in the frequency of your bowel movements, such as going more or less often.\n" +
                "C. Changes in the consistency of stool, such as increase in diarrhea\n" +
                "D. Feeling like you aren’t fully emptying your stool every time\n" +
                "E. High fever\n" +
                "F. Stomach  pain or cramping\n";
        return fp;
    } public String gpdiagnoses(){
        String gp="                                                         "+
                "Green poop:   Well, It may be because you ate a lot of green vegetables (which is good) or too much green food coloring (not so good). ). It also may mean that your food is moving through your system too quickly -- think diarrhea -- and the green in your bile doesn’t have time to break down.\n" +
                "If occurring with other symptoms, such as consistent diarrhea or vomiting that does not improve, green stool could indicate a condition such as Crohn's disease or irritable bowel syndrome\n" +
                "Remedy\n" +
                "If the color doesn’t change, see a doctor. \n";
        return gp;
    } public String rpdiagnoses(){
        String rp="                                                         "+
                "Shades of Red:   This might be from eating too much red food coloring. " +
                "It can also come from red-colored medicine. Your stool should soon return to its normal color. But a bright red stool could mean bleeding in your large intestine. " +
                "Sometimes it’s blood from your rectum, too, from a scratch or a hemorrhoid. " +
                "If you keep passing red stools, check with your doctor\n" +
                "\n" +
                "The color can range from bright red to almost black." +
                "Rectal bleeding can be a symptom of many conditions -- some serious, others less so. " +
                "It can signal a problem anywhere in your digestive tract, from your esophagus to your anus and points in between.\n" +
                "Some of the possible courses could be\n" +
                "1. Hemorrhoids\n" +
                "2.Anal Fissures \n" +
                "3.Polyps \n" +
                "4.Colorectal Cancer \n" +
                "5.Inflammatory Bowel Disease\n" +
                "6. Peptic Ulcers \n";
        return rp;
    }

    public String mdiagnoses(){
        String m="                                                         "+
                "Mucus in stool can be normal but a large amount may have underlying condition that needs treatment. Hemorrhoids and rectal prolapse can also lead to a small amount of mucus leaking into the stool.\n" +
                "Below are some diseases that can cause mucus in your stools too are,\n" +
                "1.Crohn’s disease: This affects the GI tract accompanied with symptoms including diarrhea or fatigue\n" +
                "2.Cystic fibrosis, a genetic disorder that results in the build up in thick sticky mucus in lungs, pancreas, liver or intestines.\n" +
                "3.Ulcerative colitis: A chronic condition that causes inflammation in your large intestine or rectum.\n" +
                "4. Irritable bowel syndrome\n" +
                "5. Intestinal infection \n" +
                "6.Malabsorbtion issues which occurs when your bowel is unable to properly absorb certain nutrients\n" +
                "7.Colorectal Cancer \n";
        return m;
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