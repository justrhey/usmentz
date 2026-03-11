package com.example.usmentz;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.adapter.DateAdapter;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MomentsActivity extends AppCompatActivity {

    private DateViewModel dateViewModel;
    private RecyclerView recyclerView;
    private DateAdapter dateAdapter;
    private LinearLayout emptyStateLayout;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private Date selectedDate;
    private static final String TAG = "MomentsActivity";

    private int categoryId;
    private String categoryName;
    private String categoryEmoji;
    private int categoryColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_moments);

            // Get category data from intent
            categoryId = getIntent().getIntExtra("category_id", -1);
            categoryName = getIntent().getStringExtra("category_name");
            categoryEmoji = getIntent().getStringExtra("category_emoji");
            categoryColor = getIntent().getIntExtra("category_color", 0xFF6C3483);

            if (categoryId == -1) {
                finish();
                return;
            }

            // Initialize date formatters
            calendar = Calendar.getInstance();
            selectedDate = calendar.getTime();
            dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            // Setup toolbar
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(categoryEmoji + " " + categoryName);
            toolbar.setTitleTextColor(getColor(android.R.color.white));
            toolbar.setNavigationOnClickListener(v -> finish());

            // Setup RecyclerView
            recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setHasFixedSize(true);

            dateAdapter = new DateAdapter();
            recyclerView.setAdapter(dateAdapter);

            // Empty state layout
            emptyStateLayout = findViewById(R.id.emptyStateLayout);

            // Setup ViewModel
            dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);
            dateViewModel.setCurrentCategory(categoryId);

            // Observe moments for this category
            dateViewModel.getMoments().observe(this, dateLocations -> {
                dateAdapter.setDates(dateLocations);

                if (dateLocations == null || dateLocations.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyStateLayout.setVisibility(View.VISIBLE);

                    TextView emptyText = emptyStateLayout.findViewById(R.id.emptyStateText);
                    TextView emptySubtext = emptyStateLayout.findViewById(R.id.emptyStateSubtext);
                    emptyText.setText("No moments in " + categoryName);
                    emptySubtext.setText("Tap + to add your first moment");
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyStateLayout.setVisibility(View.GONE);
                }
            });

            // Setup click listeners
            setupClickListeners();

            // Setup swipe to delete and drag drop
            setupItemTouchHelpers();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners() {
        dateAdapter.setOnItemClickListener(dateLocation -> {
            if (dateLocation != null) {
                Intent intent = new Intent(MomentsActivity.this, DetailActivity.class);
                intent.putExtra("date_location", dateLocation);
                startActivity(intent);
            }
        });

        dateAdapter.setOnItemDeleteListener(this::deleteDateLocation);

        dateAdapter.setOnItemCompleteListener((dateLocation, isCompleted) -> {
            if (dateLocation != null && dateViewModel != null) {
                dateViewModel.update(dateLocation);
            }
        });

        dateAdapter.setOnRatingChangeListener((dateLocation, rating) -> {
            if (dateLocation != null && dateViewModel != null) {
                dateViewModel.update(dateLocation);
            }
        });

        dateAdapter.setOnReviewClickListener(dateLocation -> {
            if (dateLocation != null) {
                Intent intent = new Intent(MomentsActivity.this, ReviewsActivity.class);
                intent.putExtra("date_location", dateLocation);
                startActivity(intent);
            }
        });

        // FAB to add new moment
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void setupItemTouchHelpers() {
        if (recyclerView == null || dateAdapter == null) return;

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                List<DateLocation> dates = dateAdapter.getDates();
                if (dates != null && position >= 0 && position < dates.size()) {
                    DateLocation dateLocation = dates.get(position);
                    deleteDateLocation(dateLocation);
                }
            }
        }).attachToRecyclerView(recyclerView);

        ItemTouchHelper dragHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        int fromPosition = viewHolder.getAdapterPosition();
                        int toPosition = target.getAdapterPosition();
                        dateAdapter.onItemMove(fromPosition, toPosition);
                        return true;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

                    @Override
                    public boolean isLongPressDragEnabled() {
                        return false;
                    }
                });

        dragHelper.attachToRecyclerView(recyclerView);
    }

    private void showAddDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_add_date_simple, null);
            builder.setView(dialogView);

            EditText etName = dialogView.findViewById(R.id.etName);
            EditText etAddress = dialogView.findViewById(R.id.etAddress);
            EditText etDescription = dialogView.findViewById(R.id.etDescription);
            Button btnDate = dialogView.findViewById(R.id.btnSelectDate);
            TextView tvSelectedDate = dialogView.findViewById(R.id.tvSelectedDate);
            Button btnCancel = dialogView.findViewById(R.id.btnCancel);
            Button btnSave = dialogView.findViewById(R.id.btnSave);

            tvSelectedDate.setText(dateFormat.format(selectedDate));

            btnDate.setOnClickListener(v -> {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        MomentsActivity.this,
                        (view, year, month, dayOfMonth) -> {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            selectedDate = calendar.getTime();
                            tvSelectedDate.setText(dateFormat.format(selectedDate));
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnSave.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String address = etAddress.getText().toString().trim();
                String description = etDescription.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    etName.setError("Name is required");
                    return;
                }

                if (TextUtils.isEmpty(address)) {
                    etAddress.setError("Address is required");
                    return;
                }

                if (dateViewModel != null) {
                    DateLocation newDate = new DateLocation(name, address, description, selectedDate);
                    newDate.setCategoryId(categoryId);
                    dateViewModel.insert(newDate);
                    Toast.makeText(MomentsActivity.this, "Moment added to " + categoryName, Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in showAddDialog", e);
        }
    }

    private void deleteDateLocation(DateLocation dateLocation) {
        if (dateLocation == null || recyclerView == null || dateViewModel == null) return;

        try {
            Snackbar snackbar = Snackbar.make(recyclerView, "Deleting moment...", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", v -> {
                if (dateViewModel != null) {
                    dateViewModel.insert(dateLocation);
                }
            });
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION && dateViewModel != null) {
                        dateViewModel.delete(dateLocation);
                    }
                }
            });
            snackbar.show();
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteDateLocation", e);
        }
    }
}