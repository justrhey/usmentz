package com.example.usmentz;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private DateViewModel dateViewModel;
    private DateLocation currentDate;

    private EditText etName, etAddress, etDescription;
    private TextView tvDate;
    private RatingBar ratingBar;
    private EditText etReview;
    private ImageView ivPhoto;
    private Button btnSelectPhoto, btnSave;
    private FloatingActionButton fabEdit, fabDelete;
    private TextView tvViewOnlyMessage;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private Date selectedDate;
    private Uri selectedImageUri;
    private boolean isViewMode = false;
    private static final String TAG = "DetailActivity";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Initialize date formatters
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        initViews();

        // Get date location from intent
        currentDate = (DateLocation) getIntent().getSerializableExtra("date_location");

        // Setup ViewModel
        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);

        // Determine if view mode (completed) or edit mode
        if (currentDate != null) {
            isViewMode = currentDate.isCompleted();
            loadData();
            selectedDate = currentDate.getDate();
        } else {
            // New moment
            selectedDate = new Date();
            currentDate = new DateLocation("", "", "", selectedDate);
            isViewMode = false;
        }

        tvDate.setText(dateFormat.format(selectedDate));

        // Setup UI based on mode
        setupUIMode();

        // Setup image picker
        setupImagePicker();

        // Setup click listeners
        setupClickListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        etDescription = findViewById(R.id.etDescription);
        tvDate = findViewById(R.id.tvDate);
        ratingBar = findViewById(R.id.ratingBar);
        etReview = findViewById(R.id.etReview);
        ivPhoto = findViewById(R.id.ivPhoto);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        btnSave = findViewById(R.id.btnSave);
        fabEdit = findViewById(R.id.fabEdit);
        fabDelete = findViewById(R.id.fabDelete);
        tvViewOnlyMessage = findViewById(R.id.tvViewOnlyMessage);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivPhoto.setImageURI(selectedImageUri);
                        ivPhoto.setVisibility(View.VISIBLE);

                        // Immediately save the photo path if we're in edit mode
                        if (!isViewMode && currentDate != null) {
                            currentDate.setPhotoPath(selectedImageUri.toString());
                            Toast.makeText(this, "Photo added", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupUIMode() {
        if (isViewMode) {
            // VIEW MODE - Read only, can add photo
            etName.setEnabled(false);
            etAddress.setEnabled(false);
            etDescription.setEnabled(false);
            tvDate.setEnabled(false);
            ratingBar.setIsIndicator(true);
            etReview.setEnabled(false);
            btnSelectPhoto.setVisibility(View.VISIBLE); // Can still add photos
            btnSave.setVisibility(View.GONE);
            fabEdit.setVisibility(View.VISIBLE);
            tvViewOnlyMessage.setVisibility(View.VISIBLE);
            tvViewOnlyMessage.setText("✓ Completed - View Only Mode (You can still add photos)");
        } else {
            // EDIT MODE - Full editing
            etName.setEnabled(true);
            etAddress.setEnabled(true);
            etDescription.setEnabled(true);
            tvDate.setEnabled(true);
            ratingBar.setIsIndicator(false);
            etReview.setEnabled(true);
            btnSelectPhoto.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);
            fabEdit.setVisibility(View.GONE);
            tvViewOnlyMessage.setVisibility(View.GONE);
        }
    }

    private void loadData() {
        etName.setText(currentDate.getName());
        etAddress.setText(currentDate.getAddress());
        etDescription.setText(currentDate.getDescription());
        tvDate.setText(dateFormat.format(currentDate.getDate()));
        ratingBar.setRating(currentDate.getRating());
        etReview.setText(currentDate.getReview());

        // Load photo if exists
        if (currentDate.getPhotoPath() != null && !currentDate.getPhotoPath().isEmpty()) {
            try {
                Uri photoUri = Uri.parse(currentDate.getPhotoPath());
                Glide.with(this).load(photoUri).into(ivPhoto);
                ivPhoto.setVisibility(View.VISIBLE);
                Log.d(TAG, "Photo loaded: " + currentDate.getPhotoPath());
            } catch (Exception e) {
                Log.e(TAG, "Error loading photo", e);
                ivPhoto.setVisibility(View.GONE);
            }
        } else {
            ivPhoto.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // Date picker
        tvDate.setOnClickListener(v -> {
            if (!isViewMode) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            selectedDate = calendar.getTime();
                            tvDate.setText(dateFormat.format(selectedDate));
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();
            }
        });

        // Select photo button
        btnSelectPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Save button (edit mode)
        btnSave.setOnClickListener(v -> saveMoment(false));

        // Edit button (view mode) - switch to edit mode
        fabEdit.setOnClickListener(v -> {
            isViewMode = false;
            setupUIMode();
        });

        // Delete button
        fabDelete.setOnClickListener(v -> deleteMoment());
    }

    private void saveMoment(boolean isCompleted) {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String review = etReview.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address is required");
            return;
        }

        // Update current date object
        currentDate.setName(name);
        currentDate.setAddress(address);
        currentDate.setDescription(description);
        currentDate.setDate(selectedDate);
        currentDate.setRating(rating);
        currentDate.setReview(review);
        currentDate.setCompleted(isCompleted);

        // Save photo URI if selected
        if (selectedImageUri != null) {
            currentDate.setPhotoPath(selectedImageUri.toString());
            Log.d(TAG, "Photo saved: " + selectedImageUri.toString());
        }

        if (currentDate.getId() == 0) {
            // New moment
            dateViewModel.insert(currentDate);
            Toast.makeText(this, "Moment created!", Toast.LENGTH_SHORT).show();
        } else {
            // Update existing
            dateViewModel.update(currentDate);
            Toast.makeText(this, "Moment updated!", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void deleteMoment() {
        if (currentDate.getId() != 0) {
            dateViewModel.delete(currentDate);
            Toast.makeText(this, "Moment deleted!", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}