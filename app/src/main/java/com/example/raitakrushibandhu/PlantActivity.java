package com.example.raitakrushibandhu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.raitakrushibandhu.CameraActivity;
import com.example.raitakrushibandhu.HomeActivity;
import com.example.raitakrushibandhu.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.io.File;

public class PlantActivity extends AppCompatActivity {

    private ImageView capturedImagePreview;
    private String capturedImagePath = null;

    // Use the new Activity Result API
    private ActivityResultLauncher<Intent> cameraActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Raita Krushi Bandhu");
        }

        // Setup Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.profile) {
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.home) {
                startActivity(new Intent(this, HomeActivity.class));
                return true;
            } else if (id == R.id.diagnose) {
                Toast.makeText(this, "Diagnose clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.my_plants) {
                Toast.makeText(this, "My Plants clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.account) {
                Toast.makeText(this, "Account clicked", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });
        MaterialButton clearButton = findViewById(R.id.clear);
        clearButton.setOnClickListener(v -> {
            capturedImagePath = null;
            capturedImagePreview.setImageResource(R.drawable.capture); // or any default image
            Toast.makeText(this, "Image cleared. Take a new picture", Toast.LENGTH_SHORT).show();
        });


        capturedImagePreview = findViewById(R.id.captured_image_preview);

        MaterialButton cameraButton = findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(v -> {
            Intent intent = new Intent(PlantActivity.this, CameraActivity.class);
            cameraActivityLauncher.launch(intent);
        });

        MaterialButton diagnoseButton = findViewById(R.id.diagnose_button);
        diagnoseButton.setOnClickListener(v -> {
            if (capturedImagePath != null) {
                Intent diagnoseIntent = new Intent(PlantActivity.this, DiagnoseActivity.class);
                diagnoseIntent.putExtra("image_path", capturedImagePath);
                startActivity(diagnoseIntent);
            } else {
                Toast.makeText(this, "Please capture an image first.", Toast.LENGTH_SHORT).show();
            }
        });

        // Register the launcher for camera activity result
        cameraActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        capturedImagePath = data.getStringExtra(CameraActivity.IMAGE_PATH_EXTRA);
                        if (capturedImagePath != null) {
                            File imageFile = new File(capturedImagePath);
                            if (imageFile.exists()) {
                                capturedImagePreview.setVisibility(View.VISIBLE);
                                Glide.with(this)
                                        .load(Uri.fromFile(imageFile))
                                        .skipMemoryCache(true)                           // Disable memory cache
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)       // Disable disk cache
                                        .placeholder(R.drawable.ic_plant)                 // Placeholder image
                                        .error(R.drawable.ic_error)                        // Error image
                                        .into(capturedImagePreview);
                            } else {
                                Toast.makeText(this, "Captured image file not found.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });


    }
}