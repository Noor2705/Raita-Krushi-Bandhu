package com.example.raitakrushibandhu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.raitakrushibandhu.databinding.ActivityCameraBinding;
import com.example.raitakrushibandhu.utils.ImageUtils;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    public static final String IMAGE_PATH_EXTRA = "captured_image_path";

    private ActivityCameraBinding binding;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private CameraControl cameraControl;
    private CameraInfo cameraInfo;

    private ExecutorService cameraExecutor;
    private boolean flashEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Fullscreen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        previewView = binding.viewFinder;
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Back button closes camera
        binding.backButton.setOnClickListener(v -> finish());

        // Help button shows instructions dialog
        binding.helpButton.setOnClickListener(v -> showHelpDialog());

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        binding.imageCaptureButton.setOnClickListener(v -> takePhoto());
        configureFlashButton();
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Camera Help")
                .setMessage("Tap the shutter button to capture a photo.\n\n" +
                        "Tap on the preview to focus.\n\n" +
                        "Use pinch to zoom.\n\n" +
                        "Use flash button to toggle flash.")
                .setPositiveButton("OK", null)
                .show();
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraActivity", "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        previewView.post(() -> {
            int ratio = aspectRatio(previewView.getWidth(), previewView.getHeight());
            int rotation = previewView.getDisplay().getRotation();

            cameraProvider.unbindAll();

            Preview preview = new Preview.Builder()
                    .setTargetAspectRatio(ratio)
                    .setTargetRotation(rotation)
                    .build();

            CameraSelector selector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();

            imageCapture = new ImageCapture.Builder()
                    .setTargetAspectRatio(ratio)
                    .setTargetRotation(rotation)
                    .build();

            Camera camera = cameraProvider.bindToLifecycle(this, selector, preview, imageCapture);
            cameraControl = camera.getCameraControl();
            cameraInfo = camera.getCameraInfo();

            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            setUpZoomAndTapToFocus();
            updateFlashIcon();
        });
    }

    private int aspectRatio(int width, int height) {
        double ratio = (double) Math.max(width, height) / Math.min(width, height);
        return Math.abs(ratio - 4.0 / 3.0) <= Math.abs(ratio - 16.0 / 9.0)
                ? AspectRatio.RATIO_4_3
                : AspectRatio.RATIO_16_9;
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File dir = new File(getExternalFilesDir(null), "PlantImages");
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                Toast.makeText(this, "Failed to create storage directory", Toast.LENGTH_SHORT).show();
                enableCaptureButtons();
                return;
            }
        }

        String fileName = "plant_capture_" + System.currentTimeMillis() + ".jpg";
        File file = new File(dir, fileName);

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

        binding.imageCaptureButton.setEnabled(false);

        imageCapture.takePicture(outputOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                runOnUiThread(() -> {
                    String path = file.getAbsolutePath();
                    Bitmap bitmap = null;
                    try {
                        bitmap = ImageUtils.getCorrectOrientatedBitmap(path);
                    } catch (Exception e) {
                        Log.e("CameraActivity", "Failed to process bitmap", e);
                    }
                    if (bitmap != null) {
                        binding.captureImagePreview.setVisibility(View.VISIBLE);
                        binding.captureImagePreview.setImageBitmap(bitmap);

                        binding.flashButton.setVisibility(View.INVISIBLE);
                        binding.addPhotoButton.setIconResource(R.drawable.ic_cancel_24);
                        binding.imageCaptureButton.setIconResource(R.drawable.ic_select_24);

                        binding.imageCaptureButton.setEnabled(true);
                        binding.imageCaptureButton.setOnClickListener(v -> {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra(IMAGE_PATH_EXTRA, path);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        });

                        binding.addPhotoButton.setEnabled(true);
                        binding.addPhotoButton.setOnClickListener(v -> resetToPreviewMode());

                        Toast.makeText(getBaseContext(), "Image saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getBaseContext(), "Image processing failed", Toast.LENGTH_SHORT).show();
                        enableCaptureButtons();
                    }
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException e) {
                runOnUiThread(() -> {
                    Toast.makeText(getBaseContext(), "Capture failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    enableCaptureButtons();
                });
                Log.e("CameraActivity", "Image capture error", e);
            }
        });
    }


    private void resetToPreviewMode() {
        binding.captureImagePreview.setVisibility(View.GONE);
        binding.flashButton.setVisibility(View.VISIBLE);
        binding.imageCaptureButton.setIconResource(R.drawable.ic_camera_shutter_24);
        binding.addPhotoButton.setIconResource(R.drawable.ic_add_photo_24);

        binding.imageCaptureButton.setOnClickListener(v -> takePhoto());
    }

    private void enableCaptureButtons() {
        binding.imageCaptureButton.setEnabled(true);
        binding.addPhotoButton.setEnabled(true);
    }

    private void configureFlashButton() {
        binding.flashButton.setOnClickListener(v -> {
            flashEnabled = !flashEnabled;
            cameraControl.enableTorch(flashEnabled);
            updateFlashIcon();
        });
    }

    private void updateFlashIcon() {
        int colorRes = flashEnabled ? R.color.yellow : R.color.black;
        binding.flashButton.setIconTintResource(colorRes);
        int iconRes = flashEnabled ? R.drawable.ic_flash_on_24 : R.drawable.ic_flash_off_24;
        binding.flashButton.setIconResource(iconRes);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpZoomAndTapToFocus() {
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        ZoomState zoomState = cameraInfo.getZoomState().getValue();
                        if (zoomState != null) {
                            float currentZoom = zoomState.getZoomRatio();
                            float newZoom = currentZoom * detector.getScaleFactor();
                            newZoom = Math.max(zoomState.getMinZoomRatio(), Math.min(newZoom, zoomState.getMaxZoomRatio()));
                            cameraControl.setZoomRatio(newZoom);
                            return true;
                        }
                        return false;
                    }
                });

        previewView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                MeteringPointFactory factory = previewView.getMeteringPointFactory();
                MeteringPoint point = factory.createPoint(event.getX(), event.getY());
                cameraControl.startFocusAndMetering(new FocusMeteringAction.Builder(point).build());
                animateFocusRing(event.getX(), event.getY());
            }
            return true;
        });
    }

    private void animateFocusRing(float x, float y) {
        // Optionally implement a visual indicator of focus point
        ImageView focusRing = binding.focusRing;
        focusRing.setX(x - focusRing.getWidth() / 2f);
        focusRing.setY(y - focusRing.getHeight() / 2f);
        focusRing.setVisibility(View.VISIBLE);
        focusRing.animate().alpha(0f).setDuration(1000).withEndAction(() -> {
            focusRing.setVisibility(View.GONE);
            focusRing.setAlpha(1f);
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
