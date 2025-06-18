package com.example.raitakrushibandhu;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

public class DiagnoseActivity extends AppCompatActivity {

    private ImageView diagnosisImageView;
    private ProgressBar progressBar;
    private TextView resultTextView, confidenceTextView, descriptionTextView;
    private Button diagnoseAnotherButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnose);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.diagnose_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        // Initialize Views
        diagnosisImageView = findViewById(R.id.diagnosis_image_view);
        progressBar = findViewById(R.id.progress_bar);
        resultTextView = findViewById(R.id.diagnosis_result);
        confidenceTextView = findViewById(R.id.diagnosis_confidence);
        descriptionTextView = findViewById(R.id.diagnosis_description);
        diagnoseAnotherButton = findViewById(R.id.button_diagnose_another);

        // Handle Image URI
        String imageUriString = getIntent().getStringExtra("image_uri");
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            Glide.with(this).load(imageUri).into(diagnosisImageView);
        } else {
            Toast.makeText(this, "No image found", Toast.LENGTH_SHORT).show();
        }

        // Simulated Diagnosis Result (replace with ML result logic)
        simulateDiagnosis();

        diagnoseAnotherButton.setOnClickListener(v -> finish()); // Return to previous activity
    }

    private void simulateDiagnosis() {
        // Simulate delay (you can replace this with actual model loading + inference)
        progressBar.setVisibility(View.VISIBLE);

        diagnosisImageView.postDelayed(() -> {
            progressBar.setVisibility(View.GONE);

            // Simulated result
            resultTextView.setText("Tomato Leaf Mold");
            confidenceTextView.setText("Confidence: 94.8%");
            descriptionTextView.setText("Remedy: Improve ventilation and avoid overhead watering. Use fungicide if necessary.");
        }, 1500); // 1.5 seconds delay
    }
}
