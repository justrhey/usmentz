package com.example.usmentz;

import android.content.Intent;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.usmentz.adapter.ExpenseAdapter;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.fina.Expense;
import com.example.usmentz.viewmodel.ExpenseViewModel;
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    // Shared across inner fragments via static — safe be cause only one DetailActivity at a time
    static DateLocation moment;
    static DateViewModel dateVm;
    static ExpenseViewModel expenseVm;
    static String photoPath;

    private ViewPager2 viewPager;

    private final SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    final ActivityResultLauncher<String> photoPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    // Copy image to app's internal storage for persistence
                    String savedPath = saveImageToInternalStorage(uri);
                    if (savedPath != null) {
                        photoPath = savedPath;
                        // Notify fragment to refresh
                        Fragment f = getSupportFragmentManager().findFragmentByTag("f2");
                        if (f instanceof ReviewFragment) {
                            ((ReviewFragment) f).refreshPhoto();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        moment = (DateLocation) getIntent().getSerializableExtra("date_location");
        if (moment == null) {
            finish();
            return;
        }
        photoPath = moment.getPhotoPath();

        dateVm = new ViewModelProvider(this).get(DateViewModel.class);
        expenseVm = new ViewModelProvider(this).get(ExpenseViewModel.class);

        // Toolbar
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(moment.getName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // ViewPager
        viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new PagerAdapter(this));
        viewPager.setOffscreenPageLimit(3);

        // Setup segmented control tabs
        setupPillTabs();
    }

    private void setupPillTabs() {
        LinearLayout tabDetails = findViewById(R.id.tabDetails);
        LinearLayout tabExpenses = findViewById(R.id.tabExpenses);
        LinearLayout tabReview = findViewById(R.id.tabReview);

        if (tabDetails != null) {
            tabDetails.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        }
        if (tabExpenses != null) {
            tabExpenses.setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        }
        if (tabReview != null) {
            tabReview.setOnClickListener(v -> viewPager.setCurrentItem(2, true));
        }

        // Update tab selection when swiping
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateTabSelection(position);
            }
        });

        // Initialize tab selection
        updateTabSelection(0);
    }

    // Save image to internal storage for persistence
    private String saveImageToInternalStorage(android.net.Uri sourceUri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) return null;

            String fileName = "review_photo_" + System.currentTimeMillis() + ".jpg";
            java.io.File outputDir = new java.io.File(getFilesDir(), "images");
            if (!outputDir.exists()) outputDir.mkdirs();
            java.io.File outputFile = new java.io.File(outputDir, fileName);

            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(outputFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();

            return outputFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateTabSelection(int position) {
        LinearLayout tabDetails = findViewById(R.id.tabDetails);
        LinearLayout tabExpenses = findViewById(R.id.tabExpenses);
        LinearLayout tabReview = findViewById(R.id.tabReview);
        TextView tvTabDetails = findViewById(R.id.tvTabDetails);
        TextView tvTabExpenses = findViewById(R.id.tvTabExpenses);
        TextView tvTabReview = findViewById(R.id.tvTabReview);

        // Reset all to inactive
        if (tabDetails != null) tabDetails.setBackgroundResource(R.drawable.bg_tab_inactive);
        if (tabExpenses != null) tabExpenses.setBackgroundResource(R.drawable.bg_tab_inactive);
        if (tabReview != null) tabReview.setBackgroundResource(R.drawable.bg_tab_inactive);

        // Reset text colors
        if (tvTabDetails != null) tvTabDetails.setTextColor(0xFF9E9E9E);
        if (tvTabExpenses != null) tvTabExpenses.setTextColor(0xFF9E9E9E);
        if (tvTabReview != null) tvTabReview.setTextColor(0xFF9E9E9E);

        // Set active tab
        switch (position) {
            case 0:
                if (tabDetails != null) tabDetails.setBackgroundResource(R.drawable.bg_tab_active);
                if (tvTabDetails != null) {
                    tvTabDetails.setTextColor(0xFF212121);
                    tvTabDetails.setTypeface(null, android.graphics.Typeface.BOLD);
                }
                break;
            case 1:
                if (tabExpenses != null) tabExpenses.setBackgroundResource(R.drawable.bg_tab_active);
                if (tvTabExpenses != null) {
                    tvTabExpenses.setTextColor(0xFF212121);
                    tvTabExpenses.setTypeface(null, android.graphics.Typeface.BOLD);
                }
                break;
            case 2:
                if (tabReview != null) tabReview.setBackgroundResource(R.drawable.bg_tab_active);
                if (tvTabReview != null) {
                    tvTabReview.setTextColor(0xFF212121);
                    tvTabReview.setTypeface(null, android.graphics.Typeface.BOLD);
                }
                break;
        }
    }

    // ─── EDIT DIALOG ─────────────────────────────────────────────
    void showEditDialog() {
        View dv = getLayoutInflater().inflate(R.layout.dialog_add_date_simple, null);

        EditText etName = dv.findViewById(R.id.etName);
        EditText etAddr = dv.findViewById(R.id.etAddress);
        EditText etDesc = dv.findViewById(R.id.etDescription);
        Button btnDate = dv.findViewById(R.id.btnSelectDate);
        TextView tvDate = dv.findViewById(R.id.tvSelectedDate);
        Button btnCancel = dv.findViewById(R.id.btnCancel);
        Button btnSave = dv.findViewById(R.id.btnSave);

        etName.setText(moment.getName());
        etAddr.setText(moment.getAddress());
        etDesc.setText(moment.getDescription());
        if (moment.getDate() != null) tvDate.setText(fmt.format(moment.getDate()));

        Calendar cal = Calendar.getInstance();
        if (moment.getDate() != null) cal.setTime(moment.getDate());

        btnDate.setOnClickListener(v -> new DatePickerDialog(this,
                (view, y, m, d) -> {
                    cal.set(y, m, d);
                    tvDate.setText(fmt.format(cal.getTime()));
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show());

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String addr = etAddr.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                etName.setError("Required");
                return;
            }
            if (TextUtils.isEmpty(addr)) {
                etAddr.setError("Required");
                return;
            }

            moment.setName(name);
            moment.setAddress(addr);
            moment.setDescription(etDesc.getText().toString().trim());
            moment.setDate(cal.getTime());
            dateVm.update(moment);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(name);
            dialog.dismiss();
            Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();

            // Refresh info tab
            Fragment f = getSupportFragmentManager().findFragmentByTag("f0");
            if (f instanceof InfoFragment) ((InfoFragment) f).refresh();
        });
    }

    // ─── ADD EXPENSE DIALOG ──────────────────────────────────────
    void showAddExpenseDialog() {
        View dv = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);

        EditText etDesc = dv.findViewById(R.id.etExpenseName);
        EditText etAmount = dv.findViewById(R.id.etExpenseAmount);
        Button btnCancel = dv.findViewById(R.id.btnCancelExpense);
        Button btnSave = dv.findViewById(R.id.btnSaveExpense);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String desc = etDesc.getText().toString().trim();
            String amt = etAmount.getText().toString().trim();
            if (desc.isEmpty()) {
                etDesc.setError("Required");
                return;
            }
            if (amt.isEmpty()) {
                etAmount.setError("Required");
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(amt);
            } catch (NumberFormatException e) {
                etAmount.setError("Invalid");
                return;
            }

            expenseVm.insert(new Expense(desc, amount, moment.getId()));
            dialog.dismiss();
        });
    }

    // ─── PAGER ADAPTER ───────────────────────────────────────────
    static class PagerAdapter extends FragmentStateAdapter {
        PagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 1:
                    return new ExpensesFragment();
                case 2:
                    return new ReviewFragment();
                default:
                    return new InfoFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 0 — DETAILS
    // ═══════════════════════════════════════════════════════════
    public static class InfoFragment extends Fragment {

        private TextView tvName, tvDate, tvAddress, tvDescription;
        private TextView tvRatingNumber, tvExperience, tvFees, tvAvailability, tvCategory;
        private RatingBar ratingBar;
        private ImageView ivProfile, btnFavorite, btnShare, btnEdit;
        private View mapContainer;
        private com.google.android.gms.maps.SupportMapFragment mapFragment;

        private final SimpleDateFormat fmt =
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        // Local data holders for LiveData observation
        private double totalExpensesForMoment = 0;
        private double totalFundsForMoment = 0;

        public InfoFragment() {
            super(R.layout.fragment_detail_info);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            tvName = view.findViewById(R.id.tvMomentName);
            tvDate = view.findViewById(R.id.tvDate);
            tvAddress = view.findViewById(R.id.tvAddress);
            tvDescription = view.findViewById(R.id.tvDescription);
            tvRatingNumber = view.findViewById(R.id.tvRatingNumber);
            tvExperience = view.findViewById(R.id.tvExperience);
            tvFees = view.findViewById(R.id.tvFees);
            tvAvailability = view.findViewById(R.id.tvAvailability);
            tvCategory = view.findViewById(R.id.tvCategory);
            ratingBar = view.findViewById(R.id.ratingBar);
            ivProfile = view.findViewById(R.id.ivProfile);
            btnFavorite = view.findViewById(R.id.btnFavorite);
            btnShare = view.findViewById(R.id.btnShare);
            btnEdit = view.findViewById(R.id.btnEdit);
            mapContainer = view.findViewById(R.id.mapContainer);

            // Setup action buttons
            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> {
                    if (getActivity() instanceof DetailActivity) {
                        ((DetailActivity) getActivity()).showEditDialog();
                    }
                });
            }

            if (btnFavorite != null) {
                btnFavorite.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                });
            }

            if (btnShare != null) {
                btnShare.setOnClickListener(v -> {
                    if (moment != null) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, moment.getName());
                        shareIntent.putExtra(Intent.EXTRA_TEXT,
                            moment.getName() + "\n" +
                            moment.getAddress() + "\n" +
                            "Rated: " + moment.getRating() + "/5");
                        startActivity(Intent.createChooser(shareIntent, "Share via"));
                    }
                });
            }

            // Observe real expense data for this moment
            observeExpenseData();

            // Setup Google Maps
            setupMap();

            refresh();
        }

        private void observeExpenseData() {
            if (expenseVm != null && moment != null) {
                // Observe expenses for this moment
                expenseVm.getExpensesForMoment(moment.getId()).observe(getViewLifecycleOwner(), expenses -> {
                    if (expenses != null) {
                        totalExpensesForMoment = 0;
                        totalFundsForMoment = 0;

                        for (com.example.usmentz.fina.Expense expense : expenses) {
                            if (Expense.TYPE_EXPENSES.equals(expense.getType())) {
                                totalExpensesForMoment += expense.getAmount();
                            } else if (Expense.TYPE_FUNDS.equals(expense.getType())) {
                                totalFundsForMoment += expense.getAmount();
                            }
                        }

                        // Update UI
                        if (tvFees != null) {
                            tvFees.setText(formatCurrency(totalExpensesForMoment));
                        }
                        if (tvAvailability != null) {
                            tvAvailability.setText(formatCurrency(totalFundsForMoment));
                        }
                    }
                });

                // Observe total spent for this moment
                expenseVm.getTotalSpentForMoment(moment.getId()).observe(getViewLifecycleOwner(), total -> {
                    // This gives total of all expenses (type = expenses)
                    // We use the per-type calculation above instead
                });
            }
        }

        private void setupMap() {
            if (mapContainer != null) {
                mapFragment = com.google.android.gms.maps.SupportMapFragment.newInstance();
                getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, mapFragment)
                    .commit();

                mapFragment.getMapAsync(googleMap -> {
                    if (moment != null && !TextUtils.isEmpty(moment.getAddress())) {
                        // Default location (Boracay) - you can geocode the address
                        double lat = 11.9674;
                        double lng = 121.9248;

                        com.google.android.gms.maps.model.LatLng location =
                            new com.google.android.gms.maps.model.LatLng(lat, lng);

                        googleMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions()
                            .position(location)
                            .title(moment.getName())
                            .icon(com.google.android.gms.maps.model.BitmapDescriptorFactory
                                .defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_VIOLET)));

                        googleMap.moveCamera(
                            com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 14f));

                        // Disable UI controls for embedded look
                        com.google.android.gms.maps.UiSettings ui = googleMap.getUiSettings();
                        ui.setZoomControlsEnabled(false);
                        ui.setScrollGesturesEnabled(false);
                        ui.setZoomGesturesEnabled(false);
                        ui.setTiltGesturesEnabled(false);
                        ui.setRotateGesturesEnabled(false);
                        ui.setMapToolbarEnabled(false);
                    }
                });
            }
        }

        void refresh() {
            if (moment == null) return;
            if (tvName != null) tvName.setText(moment.getName());
            if (tvAddress != null) tvAddress.setText(!TextUtils.isEmpty(moment.getAddress()) ? moment.getAddress() : "—");
            if (tvDescription != null) tvDescription.setText(!TextUtils.isEmpty(moment.getDescription())
                    ? moment.getDescription() : "No description");
            if (tvDate != null && moment.getDate() != null) tvDate.setText(fmt.format(moment.getDate()));
            if (ratingBar != null) ratingBar.setRating(moment.getRating());
            if (tvRatingNumber != null) tvRatingNumber.setText(String.valueOf(moment.getRating()));
            if (tvExperience != null) tvExperience.setText(String.valueOf((int) moment.getRating()));

            // Show real calculated values (will be updated by LiveData observer)
            if (tvFees != null) tvFees.setText(formatCurrency(totalExpensesForMoment));
            if (tvAvailability != null) tvAvailability.setText(formatCurrency(totalFundsForMoment));
        }

        private String formatCurrency(double amount) {
            return "₱" + String.format(java.util.Locale.getDefault(), "%,.0f", Math.abs(amount));
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 1 — EXPENSES
    // ═══════════════════════════════════════════════════════════
    public static class ExpensesFragment extends Fragment {

        public ExpensesFragment() {
            super(R.layout.fragment_detail_expenses);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            TextView tvTotal = view.findViewById(R.id.tvTotalSpent);
            TextView tvEmpty = view.findViewById(R.id.tvNoExpenses);
            RecyclerView rv = view.findViewById(R.id.rvExpenses);

            ExpenseAdapter adapter = new ExpenseAdapter();
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            rv.setAdapter(adapter);

            adapter.setOnExpenseDeleteListener(expense -> expenseVm.delete(expense));

            expenseVm.getExpensesForMoment(moment.getId())
                    .observe(getViewLifecycleOwner(), expenses -> {
                        adapter.setExpenses(expenses);
                        tvEmpty.setVisibility(expenses == null || expenses.isEmpty()
                                ? View.VISIBLE : View.GONE);
                    });

            expenseVm.getTotalSpentForMoment(moment.getId())
                    .observe(getViewLifecycleOwner(), total ->
                            tvTotal.setText(String.format("₱%.2f", total != null ? total : 0.0)));
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 2 — REVIEW
    // ═══════════════════════════════════════════════════════════
    public static class ReviewFragment extends Fragment {

        private ImageView ivPhoto;
        private EditText etReview;
        private RatingBar ratingBarReview;
        private TextView tvRatingLabel;
        private View noPhotoPlaceholder;

        private final String[] ratingLabels = {
                "Tap to rate", "Poor", "Fair", "Good", "Great", "Amazing!"
        };

        public ReviewFragment() {
            super(R.layout.fragment_detail_review);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            try {
                // Initialize views with null checks
                etReview = view.findViewById(R.id.etReview);
                ivPhoto = view.findViewById(R.id.ivPhoto);
                ratingBarReview = view.findViewById(R.id.ratingBarReview);
                tvRatingLabel = view.findViewById(R.id.tvRatingLabel);
                noPhotoPlaceholder = view.findViewById(R.id.noPhotoPlaceholder);


                // Load existing data
                if (moment != null) {
                    if (etReview != null && !TextUtils.isEmpty(moment.getReview())) {
                        etReview.setText(moment.getReview());
                    }

                    if (ratingBarReview != null) {
                        ratingBarReview.setRating(moment.getRating());
                        updateRatingLabel(moment.getRating());
                    }
                }

                // Rating change listener
                if (ratingBarReview != null) {
                    ratingBarReview.setOnRatingBarChangeListener((bar, rating, fromUser) ->
                            updateRatingLabel(rating));
                }

                // Load photo
                loadPhoto();

                // Photo button click
                View btnSelectPhoto = view.findViewById(R.id.btnSelectPhoto);
                if (btnSelectPhoto != null) {
                    btnSelectPhoto.setOnClickListener(v -> {
                        if (getActivity() instanceof DetailActivity)
                            ((DetailActivity) getActivity()).photoPicker.launch("image/*");
                    });
                }

                // Save button click
                View btnSaveReview = view.findViewById(R.id.btnSaveReview);
                if (btnSaveReview != null) {
                    btnSaveReview.setOnClickListener(v -> {
                        if (moment == null) return;

                        if (etReview != null) {
                            moment.setReview(etReview.getText().toString().trim());
                        }
                        moment.setPhotoPath(photoPath);
                        if (ratingBarReview != null) {
                            moment.setRating(ratingBarReview.getRating());
                        }
                        dateVm.update(moment);
                        Toast.makeText(getContext(), "Review saved", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void updateRatingLabel(float rating) {
            if (tvRatingLabel == null) return;
            int index = Math.min((int) Math.ceil(rating), ratingLabels.length - 1);
            tvRatingLabel.setText(ratingLabels[index]);
        }

        @Override
        public void onResume() {
            super.onResume();
            loadPhoto();
        }

        private void loadPhoto() {
            try {
                if (!TextUtils.isEmpty(photoPath) && ivPhoto != null) {
                    // Show photo, hide placeholder
                    ivPhoto.setVisibility(View.VISIBLE);
                    if (noPhotoPlaceholder != null) noPhotoPlaceholder.setVisibility(View.GONE);
                    
                    // Load image
                    File imgFile = new File(photoPath);
                    if (imgFile.exists()) {
                        Glide.with(this).load(imgFile).centerCrop().into(ivPhoto);
                    } else {
                        Glide.with(this).load(photoPath).centerCrop().into(ivPhoto);
                    }
                } else {
                    // Hide photo, show placeholder
                    if (ivPhoto != null) ivPhoto.setVisibility(View.GONE);
                    if (noPhotoPlaceholder != null) noPhotoPlaceholder.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Called when photo is selected to refresh the preview
        void refreshPhoto() {
            loadPhoto();
        }
    }
}




