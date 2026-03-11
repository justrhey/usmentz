package com.example.usmentz;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.usmentz.adapter.ExpenseAdapter;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.fina.Expense;
import com.example.usmentz.viewmodel.DateViewModel;
import com.example.usmentz.viewmodel.ExpenseViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MomentsDetailActivity extends AppCompatActivity {

    private DateViewModel dateViewModel;
    private ExpenseViewModel expenseViewModel;
    private DateLocation currentMoment;
    private int momentId;

    // View elements
    private TextView tvName, tvAddress, tvDescription, tvDate, tvReview, tvTotalSpent, tvEmptyExpenses;
    private RatingBar ratingBar;
    private ImageView ivPhoto;
    private View cardPhoto, tvNoPhoto, ratingSection;
    private EditText etReview, etExpenseDescription, etExpenseAmount;
    private Button btnSelectPhoto, btnSaveReview, btnEditReview, btnAddExpense, btnEdit, btnDelete;
    private RecyclerView expensesRecyclerView;
    private ExpenseAdapter expenseAdapter;
    private NestedScrollView nestedScrollView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moments_detail);

        momentId = getIntent().getIntExtra("moment_id", -1);
        if (momentId == -1) {
            Toast.makeText(this, "Error loading moment", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupImagePicker();
        setupViewModels();
        setupRecyclerView();

        loadMomentData();
        loadExpenses();

        setupClickListeners();
    }

    private void initViews() {
        // Main info
        tvName = findViewById(R.id.tvMomentName);
        tvAddress = findViewById(R.id.tvAddress);
        tvDescription = findViewById(R.id.tvDescription);
        tvDate = findViewById(R.id.tvDate);

        // Rating
        ratingBar = findViewById(R.id.ratingBar);
        ratingSection = findViewById(R.id.ratingSection);

        // Photo
        ivPhoto = findViewById(R.id.ivPhoto);
        cardPhoto = findViewById(R.id.cardPhoto);
        tvNoPhoto = findViewById(R.id.tvNoPhoto);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);

        // Review
        tvReview = findViewById(R.id.tvReview);
        etReview = findViewById(R.id.etReview);
        btnSaveReview = findViewById(R.id.btnSaveReview);
        btnEditReview = findViewById(R.id.btnEditReview);

        // Expenses
        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        tvEmptyExpenses = findViewById(R.id.tvEmptyExpenses);
        expensesRecyclerView = findViewById(R.id.expensesRecyclerView);
        etExpenseDescription = findViewById(R.id.etExpenseDescription);
        etExpenseAmount = findViewById(R.id.etExpenseAmount);
        btnAddExpense = findViewById(R.id.btnAddExpenseInline);

        // Action buttons
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        // ScrollView - MAKE SURE YOU HAVE android:id="@+id/nestedScrollView" IN YOUR XML
        nestedScrollView = findViewById(R.id.nestedScrollView);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Moment Details");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivPhoto.setImageURI(selectedImageUri);
                        cardPhoto.setVisibility(View.VISIBLE);
                        tvNoPhoto.setVisibility(View.GONE);

                        // Save photo if in edit mode
                        if (isEditMode && currentMoment != null) {
                            currentMoment.setPhotoPath(selectedImageUri.toString());
                            dateViewModel.update(currentMoment);
                        }

                        // FIX: Scroll to show the save button when photo is added
                        if (btnSaveReview.getVisibility() == View.VISIBLE && nestedScrollView != null) {
                            btnSaveReview.post(() -> {
                                nestedScrollView.smoothScrollTo(0, btnSaveReview.getBottom());
                            });
                        }
                    }
                });
    }

    private void setupViewModels() {
        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
    }

    private void setupRecyclerView() {
        expenseAdapter = new ExpenseAdapter();
        expensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expensesRecyclerView.setAdapter(expenseAdapter);

        expenseAdapter.setOnExpenseDeleteListener(expense -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Delete", (dialog, which) -> expenseViewModel.delete(expense))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupClickListeners() {
        // Photo selection
        btnSelectPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Add expense
        btnAddExpense.setOnClickListener(v -> addExpense());

        // Save review
        btnSaveReview.setOnClickListener(v -> saveReview());

        // Edit review
        btnEditReview.setOnClickListener(v -> enterEditMode());

        // Edit moment
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(MomentsDetailActivity.this, DetailActivity.class);
            intent.putExtra("date_location", currentMoment);
            startActivity(intent);
        });

        // Delete moment
        btnDelete.setOnClickListener(v -> deleteMoment());
    }

    private void loadMomentData() {
        dateViewModel.getDateById(momentId).observe(this, dateLocation -> {
            if (dateLocation != null) {
                currentMoment = dateLocation;

                // Basic info
                tvName.setText(dateLocation.getName());
                tvAddress.setText(dateLocation.getAddress());
                tvDescription.setText(dateLocation.getDescription());
                tvDate.setText(dateFormat.format(dateLocation.getDate()));

                // Rating section
                if (dateLocation.isCompleted()) {
                    ratingSection.setVisibility(View.VISIBLE);
                    ratingBar.setRating(dateLocation.getRating());
                } else {
                    ratingSection.setVisibility(View.GONE);
                }

                // Review section
                if (dateLocation.getReview() != null && !dateLocation.getReview().isEmpty()) {
                    tvReview.setText(dateLocation.getReview());
                    tvReview.setVisibility(View.VISIBLE);
                    etReview.setVisibility(View.GONE);
                    btnSaveReview.setVisibility(View.GONE);
                    btnEditReview.setVisibility(View.VISIBLE);
                } else {
                    tvReview.setVisibility(View.GONE);
                    etReview.setVisibility(View.VISIBLE);
                    btnSaveReview.setVisibility(View.VISIBLE);
                    btnEditReview.setVisibility(View.GONE);
                    isEditMode = true;
                }

                // Photo
                if (dateLocation.getPhotoPath() != null && !dateLocation.getPhotoPath().isEmpty()) {
                    Glide.with(this).load(dateLocation.getPhotoPath()).into(ivPhoto);
                    cardPhoto.setVisibility(View.VISIBLE);
                    tvNoPhoto.setVisibility(View.GONE);
                } else {
                    cardPhoto.setVisibility(View.GONE);
                    tvNoPhoto.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadExpenses() {
        expenseViewModel.getExpensesForMoment(momentId).observe(this, expenses -> {
            if (expenses != null && !expenses.isEmpty()) {
                expenseAdapter.setExpenses(expenses);
                expensesRecyclerView.setVisibility(View.VISIBLE);
                tvEmptyExpenses.setVisibility(View.GONE);
            } else {
                expensesRecyclerView.setVisibility(View.GONE);
                tvEmptyExpenses.setVisibility(View.VISIBLE);
            }
        });

        expenseViewModel.getTotalSpentForMoment(momentId).observe(this, total -> {
            if (total != null) {
                tvTotalSpent.setText(currencyFormat.format(total));
            } else {
                tvTotalSpent.setText(currencyFormat.format(0));
            }
        });
    }

    private void addExpense() {
        String description = etExpenseDescription.getText().toString().trim();
        String amountStr = etExpenseAmount.getText().toString().trim();

        if (TextUtils.isEmpty(description)) {
            etExpenseDescription.setError("Description required");
            return;
        }

        if (TextUtils.isEmpty(amountStr)) {
            etExpenseAmount.setError("Amount required");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            Expense expense = new Expense(description, amount, momentId);
            expenseViewModel.insert(expense);
            etExpenseDescription.setText("");
            etExpenseAmount.setText("");
            Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            etExpenseAmount.setError("Invalid amount");
        }
    }

    private void saveReview() {
        String reviewText = etReview.getText().toString().trim();
        if (reviewText.isEmpty()) {
            Toast.makeText(this, "Please write a review", Toast.LENGTH_SHORT).show();
            return;
        }

        currentMoment.setReview(reviewText);
        currentMoment.setCompleted(true);
        currentMoment.setRating(ratingBar.getRating());

        if (selectedImageUri != null) {
            currentMoment.setPhotoPath(selectedImageUri.toString());

            // FIX 1: Update the ImageView visibility IMMEDIATELY
            Glide.with(this).load(selectedImageUri).into(ivPhoto);
            cardPhoto.setVisibility(View.VISIBLE);
            tvNoPhoto.setVisibility(View.GONE);

            // FIX 2: Scroll to show the save button after photo appears
            if (nestedScrollView != null) {
                btnSaveReview.post(() -> {
                    nestedScrollView.smoothScrollTo(0, btnSaveReview.getBottom());
                });
            }
        }

        dateViewModel.update(currentMoment);

        // Switch to view mode
        tvReview.setText(reviewText);
        tvReview.setVisibility(View.VISIBLE);
        etReview.setVisibility(View.GONE);
        btnSaveReview.setVisibility(View.GONE);
        btnEditReview.setVisibility(View.VISIBLE);
        ratingSection.setVisibility(View.VISIBLE);
        isEditMode = false;

        Toast.makeText(this, "Review saved", Toast.LENGTH_SHORT).show();
    }

    private void enterEditMode() {
        etReview.setText(tvReview.getText());
        tvReview.setVisibility(View.GONE);
        etReview.setVisibility(View.VISIBLE);
        btnSaveReview.setVisibility(View.VISIBLE);
        btnEditReview.setVisibility(View.GONE);
        isEditMode = true;
    }

    private void deleteMoment() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Moment")
                .setMessage("Are you sure you want to delete this moment? All expenses will also be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    expenseViewModel.deleteAllExpensesForMoment(momentId);
                    dateViewModel.delete(currentMoment);
                    Toast.makeText(this, "Moment deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}