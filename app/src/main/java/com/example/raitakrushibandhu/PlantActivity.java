package com.example.raitakrushibandhu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlantActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_PERMISSIONS_CODE = 100;

    private ImageView capturedImagePreview;
    private MaterialButton cameraButton, galleryButton, diagnoseButton, clearButton;
    private MaterialCardView card_crop_info,card_soil_check,card_weather,card_history,card_profile;

    private Bitmap selectedBitmap = null;
    private Uri selectedImageUri = null;
    private List<Uri> loadedImageUris = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant);


        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Raita Krushi Bandhu");
        }

        capturedImagePreview = findViewById(R.id.captured_image_preview);
        cameraButton = findViewById(R.id.camera_button);
        galleryButton = findViewById(R.id.gallery_button);
        diagnoseButton = findViewById(R.id.diagnose_button);
        clearButton = findViewById(R.id.clear);
        card_crop_info = findViewById(R.id.card_crop_info);
        card_soil_check = findViewById(R.id.card_soil_check);
        card_weather = findViewById(R.id.card_weather);
        card_history = findViewById(R.id.card_history);
        card_profile = findViewById(R.id.card_profile);


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
            }
            return false;
        });

        cameraButton.setOnClickListener(v -> openCamera());
        galleryButton.setOnClickListener(v -> openGalleryPicker());

        diagnoseButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                Intent intent = new Intent(this, DiagnoseActivity.class);
                intent.putExtra("image_uri", selectedImageUri.toString());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select or capture an image first.", Toast.LENGTH_SHORT).show();
            }
        });



        clearButton.setOnClickListener(v -> clearImage());

        capturedImagePreview.setOnClickListener(v -> {
            if (selectedImageUri != null && !loadedImageUris.isEmpty()) {
                ArrayList<String> uriStrings = new ArrayList<>();
                for (Uri uri : loadedImageUris) {
                    uriStrings.add(uri.toString());
                }

                int selectedIndex = loadedImageUris.indexOf(selectedImageUri);
                if (selectedIndex == -1) selectedIndex = 0;

                Intent intent = new Intent(this, FullscreenImageActivity.class);
                intent.putStringArrayListExtra(FullscreenImageActivity.EXTRA_IMAGE_URIS, uriStrings);
                intent.putExtra(FullscreenImageActivity.EXTRA_SELECTED_INDEX, selectedIndex);
                startActivity(intent);
            }
        });
        card_crop_info.setOnClickListener(view ->
                startActivity(new Intent(PlantActivity.this, CropInfoActivity.class)));
        card_soil_check.setOnClickListener(view ->
                startActivity(new Intent(PlantActivity.this, SoilNpkCheckerActivity.class)));
        card_weather.setOnClickListener(view ->
                startActivity(new Intent(PlantActivity.this, WeatherForecastActivity.class)));
        card_history.setOnClickListener(view ->
                startActivity(new Intent(PlantActivity.this, DiagnosisHistoryActivity.class)));
        card_profile.setOnClickListener(view ->
                startActivity(new Intent(PlantActivity.this, ProfileSettingsActivity.class)));


        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    REQUEST_PERMISSIONS_CODE);
        }
    }

    private void openCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void openGalleryPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void clearImage() {
        capturedImagePreview.setImageDrawable(null);
        capturedImagePreview.setVisibility(View.GONE);
        selectedBitmap = null;
        selectedImageUri = null;
        Toast.makeText(this, "Image cleared.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.hasExtra(CameraActivity.IMAGE_PATH_EXTRA)) {
                String imagePath = data.getStringExtra(CameraActivity.IMAGE_PATH_EXTRA);
                selectedImageUri = Uri.parse(imagePath);
                selectedBitmap = null;

                capturedImagePreview.setVisibility(View.VISIBLE);
                Glide.with(this).load(selectedImageUri).into(capturedImagePreview);

                loadedImageUris.clear();
                loadedImageUris.add(selectedImageUri);
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                selectedImageUri = data.getData();
                try {
                    Bitmap galleryImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    selectedBitmap = galleryImage;

                    capturedImagePreview.setVisibility(View.VISIBLE);
                    capturedImagePreview.setImageBitmap(galleryImage);

                    loadedImageUris.clear();
                    loadedImageUris.add(selectedImageUri);
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
