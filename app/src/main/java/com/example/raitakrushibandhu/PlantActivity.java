package com.example.raitakrushibandhu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;

public class PlantActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private ImageView capturedImagePreview;
    private MaterialButton cameraButton, galleryButton, diagnoseButton, clearButton;

    private Bitmap selectedBitmap = null;
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant);

        setupToolbar();
        initializeViews();
        setupBottomNavigation();
        setupButtonListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Raita Krushi Bandhu");
        }
    }

    private void initializeViews() {
        capturedImagePreview = findViewById(R.id.captured_image_preview);
        cameraButton = findViewById(R.id.camera_button);
        galleryButton = findViewById(R.id.gallery_button);
        diagnoseButton = findViewById(R.id.diagnose_button);
        clearButton = findViewById(R.id.clear);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (id == R.id.home) {
                startActivity(new Intent(this, HomeActivity.class));
                return true;
            } else if (id == R.id.diagnose) {
                startActivity(new Intent(this, DiagnoseHistory.class));
                return true;
            }
            return false;
        });
    }

    private void setupButtonListeners() {
        cameraButton.setOnClickListener(v -> openCamera());
        galleryButton.setOnClickListener(v -> openGallery());

        diagnoseButton.setOnClickListener(v -> {
            if (selectedBitmap != null) {
                Toast.makeText(this, "Proceeding to diagnose...", Toast.LENGTH_SHORT).show();
                // Future: Intent to DiagnosisActivity with Bitmap or Uri
                // startActivity(new Intent(this, DiagnoseActivity.class));
            } else {
                Toast.makeText(this, "Please select or capture an image first.", Toast.LENGTH_SHORT).show();
            }
        });

        clearButton.setOnClickListener(v -> {
            capturedImagePreview.setImageDrawable(null);
            capturedImagePreview.setVisibility(View.GONE);
            selectedBitmap = null;
            selectedImageUri = null;
            Toast.makeText(this, "Image cleared.", Toast.LENGTH_SHORT).show();
        });
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    if (extras != null && extras.get("data") != null) {
                        selectedBitmap = (Bitmap) extras.get("data");
                        capturedImagePreview.setVisibility(View.VISIBLE);
                        capturedImagePreview.setImageBitmap(selectedBitmap);
                    }
                    break;

                case REQUEST_IMAGE_PICK:
                    selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        try {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                            capturedImagePreview.setVisibility(View.VISIBLE);
                            // Using Glide for efficient image loading
                            Glide.with(this).load(selectedImageUri).into(capturedImagePreview);
                        } catch (IOException e) {
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }
    }
}
