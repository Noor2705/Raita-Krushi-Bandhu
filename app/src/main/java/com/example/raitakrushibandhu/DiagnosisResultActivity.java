package com.example.raitakrushibandhu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import java.util.HashMap;
import java.util.Map;

public class DiagnosisResultActivity extends AppCompatActivity {

    private ImageView diagnosedImage;
    private TextView diseaseName, confidenceScore, remedyText;
    private Button saveReportBtn;
    private Uri imageUri;
    private String predictedDisease = "Leaf Blight";
    private float confidence = 92.5f;
    private String remedy = "Apply fungicide twice a week";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnosis_result);

        diagnosedImage = findViewById(R.id.diagnosedImage);
        diseaseName = findViewById(R.id.diseaseName);
        confidenceScore = findViewById(R.id.confidenceScore);
        remedyText = findViewById(R.id.remedyText);
        saveReportBtn = findViewById(R.id.saveReportBtn);

        Intent intent = getIntent();
        imageUri = intent.getParcelableExtra("imageUri");
        predictedDisease = intent.getStringExtra("diseaseName");
        confidence = intent.getFloatExtra("confidence", 90f);
        remedy = intent.getStringExtra("remedy");

        if (imageUri != null) {
            Glide.with(this).load(imageUri).into(diagnosedImage);
        }

        diseaseName.setText("Disease Name: " + predictedDisease);
        confidenceScore.setText("Confidence: " + confidence + "%");
        remedyText.setText("Remedy: " + remedy);

        saveReportBtn.setOnClickListener(v -> saveToFirebase());
    }

    private void saveToFirebase() {
        if (imageUri != null) {
            String filename = "diagnoses/" + System.currentTimeMillis() + ".jpg";
            FirebaseStorage.getInstance().getReference(filename)
                    .putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                saveToFirestore(uri.toString());
                            }));
        }
    }

    private void saveToFirestore(String imageUrl) {
        Map<String, Object> data = new HashMap<>();
        data.put("diseaseName", predictedDisease);
        data.put("confidence", confidence);
        data.put("remedy", remedy);
        data.put("imageUrl", imageUrl);
        data.put("timestamp", System.currentTimeMillis());

        FirebaseFirestore.getInstance()
                .collection("diagnoses")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    // Optionally show a toast or redirect
                });
    }
}
