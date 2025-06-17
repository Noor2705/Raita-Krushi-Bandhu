package com.example.raitakrushibandhu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

public class FullscreenImageActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URIS = "extra_image_uris";
    public static final String EXTRA_SELECTED_INDEX = "extra_selected_index";

    private ArrayList<Uri> imageUris;
    private ViewPager2 viewPager;
    private FullscreenImageAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        viewPager = findViewById(R.id.view_pager);

        ArrayList<String> uriStrings = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URIS);
        int selectedIndex = getIntent().getIntExtra(EXTRA_SELECTED_INDEX, 0);

        if (uriStrings == null || uriStrings.isEmpty()) {
            Toast.makeText(this, "No images to display", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Convert string URIs to Uri objects
        imageUris = new ArrayList<>();
        for (String uriStr : uriStrings) {
            imageUris.add(Uri.parse(uriStr));
        }

        adapter = new FullscreenImageAdapter(this, imageUris);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(selectedIndex, false);

        // Button actions
        ImageButton btnShare = findViewById(R.id.btn_share);
        ImageButton btnDelete = findViewById(R.id.btn_delete);
        ImageButton btnDiagnose = findViewById(R.id.btn_diagnose);

        btnShare.setOnClickListener(v -> shareCurrentImage());
        btnDelete.setOnClickListener(v -> deleteCurrentImage());
        btnDiagnose.setOnClickListener(v -> diagnoseCurrentImage());
    }

    private void shareCurrentImage() {
        int currentIndex = viewPager.getCurrentItem();
        Uri imageUri = imageUris.get(currentIndex);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }

    private void deleteCurrentImage() {
        int currentIndex = viewPager.getCurrentItem();
        Uri uriToRemove = imageUris.get(currentIndex);

        try {
            getContentResolver().delete(uriToRemove, null, null); // Deletes from MediaStore
            Toast.makeText(this, "Image deleted from device", Toast.LENGTH_SHORT).show();

            imageUris.remove(currentIndex);
            adapter.notifyItemRemoved(currentIndex);

            if (imageUris.isEmpty()) {
                finish();
            } else {
                viewPager.setCurrentItem(Math.max(currentIndex - 1, 0), true);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
        }
    }


    private void diagnoseCurrentImage() {
        int currentIndex = viewPager.getCurrentItem();
        Uri selectedUri = imageUris.get(currentIndex);

        Toast.makeText(this, "Running diagnosis on selected image...", Toast.LENGTH_SHORT).show();

        // TODO: Pass URI to your ML model activity/class
        // Example:
        // Intent intent = new Intent(this, DiagnosisActivity.class);
        // intent.putExtra("image_uri", selectedUri.toString());
        // startActivity(intent);
    }
}
