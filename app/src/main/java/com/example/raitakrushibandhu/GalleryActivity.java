package com.example.raitakrushibandhu;

import com.example.raitakrushibandhu.ImageAdapter;



import android.os.Bundle;
import android.os.Environment;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {

    private GridView gridView;
    private ArrayList<File> imageFiles;
    private ImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        gridView = findViewById(R.id.gallery_grid);
        imageFiles = loadImagesFromFolder();

        if (imageFiles.isEmpty()) {
            Toast.makeText(this, "No images found in RaitaKrushiBandhu folder", Toast.LENGTH_SHORT).show();
        }

        adapter = new ImageAdapter(this, imageFiles);
        gridView.setAdapter(adapter);
    }

    private ArrayList<File> loadImagesFromFolder() {
        ArrayList<File> images = new ArrayList<>();
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "RaitaKrushiBandhu");

        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {
                        images.add(file);
                    }
                }
            }
        }
        return images;
    }
}
