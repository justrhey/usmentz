package com.example.usmentz;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhotoboothActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private Camera camera;

    private TextView tvCountdown;
    private LinearLayout photoStrip;
    private ImageButton btnShutter, btnOrientation, btnFlash;
    private View btnCancel, btnUsePhotos;
    private ImageView ivBorderOverlay;

    private final ArrayList<Uri> capturedPhotos = new ArrayList<>();
    private boolean isLandscape = false;
    private boolean isFlashOn = false;

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photobooth);

        previewView = findViewById(R.id.previewView);
        tvCountdown = findViewById(R.id.tvCountdown);
        photoStrip = findViewById(R.id.photoStrip);
        btnShutter = findViewById(R.id.btnShutter);
        btnOrientation = findViewById(R.id.btnOrientation);
        btnFlash = findViewById(R.id.btnFlash);
        btnCancel = findViewById(R.id.btnCancel);
        btnUsePhotos = findViewById(R.id.btnUsePhotos);
        ivBorderOverlay = findViewById(R.id.ivBorderOverlay);

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        btnShutter.setOnClickListener(v -> startBurstCapture());
        btnCancel.setOnClickListener(v -> finish());
        btnUsePhotos.setOnClickListener(v -> returnPhotos());
        
        btnOrientation.setOnClickListener(v -> {
            isLandscape = !isLandscape;
            // In a real implementation, we'd rotate the layout or preview here.
            // For now, we just toggle the icon or state.
            Toast.makeText(this, isLandscape ? "Landscape Mode" : "Portrait Mode", Toast.LENGTH_SHORT).show();
        });

        btnFlash.setOnClickListener(v -> {
            isFlashOn = !isFlashOn;
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                camera.getCameraControl().enableTorch(isFlashOn);
            }
            btnFlash.setImageResource(isFlashOn ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                Toast.makeText(this, "Camera init failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void startBurstCapture() {
        btnShutter.setEnabled(false);
        btnShutter.setAlpha(0.5f);

        new CountDownTimer(3500, 1000) {
            int count = 3;

            public void onTick(long millisUntilFinished) {
                tvCountdown.setVisibility(View.VISIBLE);
                tvCountdown.setText(String.valueOf(count));
                count--;
            }

            public void onFinish() {
                tvCountdown.setVisibility(View.GONE);
                captureSequence();
            }
        }.start();
    }

    private void captureSequence() {
        // Capture 3 photos rapidly
        for (int i = 0; i < 3; i++) {
            takePhoto();
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        }
        
        btnShutter.setEnabled(true);
        btnShutter.setAlpha(1.0f);
        btnUsePhotos.setVisibility(View.VISIBLE);
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(getExternalFilesDir(null), "photobooth_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                Uri savedUri = Uri.fromFile(photoFile);
                capturedPhotos.add(savedUri);
                addThumbnailToStrip(savedUri);
            }

            @Override
            public void onError(@NonNull ImageCaptureException error) {
                Toast.makeText(PhotoboothActivity.this, "Capture failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addThumbnailToStrip(Uri uri) {
        ImageView thumb = new ImageView(this);
        int size = (int) (80 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(8, 0, 8, 0);
        thumb.setLayoutParams(params);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumb.setBackgroundResource(R.drawable.bg_photo_placeholder);
        thumb.setImageURI(uri);
        photoStrip.addView(thumb);
    }

    private void returnPhotos() {
        if (capturedPhotos.isEmpty()) {
            finish();
            return;
        }
        
        // Return the last captured photo as the result (or we could return all)
        Intent resultIntent = new Intent();
        resultIntent.putExtra("photo_uri", capturedPhotos.get(capturedPhotos.size() - 1).toString());
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
