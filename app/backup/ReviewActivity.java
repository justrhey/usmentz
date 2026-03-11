package com.example.usmentz;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class ReviewActivity extends AppCompatActivity {

    public static final String EXTRA_DATE_ID = "date_id";
    public static final String EXTRA_CATEGORY_ID = "category_id";

    private EditText editReview;
    private RatingBar ratingBar;
    private ImageView imageViewPhoto;
    private Button buttonSave;
    private Button buttonPickPhoto;
    private ImageView btnBack; // Add this if you have a back button

    private DateViewModel dateViewModel;
    private int dateId;
    private int categoryId;
    private String currentPhotoPath = null;

    private static final int REQUEST_IMAGE_PICK = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Initialize views
        editReview = findViewById(R.id.editReview);
        ratingBar = findViewById(R.id.ratingBar);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        buttonSave = findViewById(R.id.buttonSave);
        buttonPickPhoto = findViewById(R.id.buttonPickPhoto);

        // Optional back button - comment out if you don't have it
        // btnBack = findViewById(R.id.btnBack);

        // Get data from intent
        dateId = getIntent().getIntExtra(EXTRA_DATE_ID, -1);
        categoryId = getIntent().getIntExtra(EXTRA_CATEGORY_ID, -1);

        // Setup ViewModel
        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);

        // Load existing review data if available
        loadExistingReview();

        // Setup click listeners
        buttonPickPhoto.setOnClickListener(v -> pickImage());
        buttonSave.setOnClickListener(v -> saveReview());

        // Optional back button listener
        // if (btnBack != null) {
        //     btnBack.setOnClickListener(v -> finish());
        // }
    }

    private void loadExistingReview() {
        dateViewModel.getDateById(dateId).observe(this, dateLocation -> {
            if (dateLocation != null) {
                ratingBar.setRating(dateLocation.getRating());
                editReview.setText(dateLocation.getReview());

                if (dateLocation.getPhotoPath() != null && !dateLocation.getPhotoPath().isEmpty()) {
                    currentPhotoPath = dateLocation.getPhotoPath();
                    loadImageIntoView(currentPhotoPath);
                }
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();

                // Take persistable permission
                try {
                    getContentResolver().takePersistableUriPermission(
                            selectedImageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                // Save URI as string
                currentPhotoPath = selectedImageUri.toString();
                loadImageIntoView(currentPhotoPath);
            }
        }
    }

    private void loadImageIntoView(String imagePath) {
        try {
            imageViewPhoto.setVisibility(ImageView.VISIBLE);

            if (imagePath.startsWith("content://")) {
                Uri imageUri = Uri.parse(imagePath);
                imageViewPhoto.setImageURI(imageUri);
            } else {
                // Simple file loading without Glide
                imageViewPhoto.setImageURI(Uri.parse(imagePath));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveReview() {
        String review = editReview.getText().toString().trim();
        float rating = ratingBar.getRating();

        dateViewModel.updateReview(dateId, review, rating, currentPhotoPath);

        Toast.makeText(this, "Review saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}