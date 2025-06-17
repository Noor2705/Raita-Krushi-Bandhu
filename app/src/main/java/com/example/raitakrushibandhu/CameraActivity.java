package com.example.raitakrushibandhu;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    public static final String IMAGE_PATH_EXTRA = "image_path_extra";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Uri photoUri;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchCamera();
    }

    private void launchCamera() {
        try {
            photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(
                        this,
                        "com.example.raitakrushibandhu.fileprovider",
                        photoFile
                );

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error launching camera", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "RKBandhu_" + timeStamp + ".jpg";

        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File appFolder = new File(picturesDir, "RaitaKrushiBandhu");

        if (!appFolder.exists()) {
            appFolder.mkdirs();
        }

        File imageFile = new File(appFolder, imageFileName);
        imageFile.createNewFile();

        return imageFile;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK && photoUri != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(IMAGE_PATH_EXTRA, photoUri.toString());
                setResult(Activity.RESULT_OK, resultIntent);
            } else {
                Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show();
                if (photoFile != null && photoFile.exists()) {
                    photoFile.delete();
                }
                setResult(Activity.RESULT_CANCELED);
            }
            finish();
        }
    }
}
