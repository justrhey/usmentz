package com.example.usmentz;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.date.DateAdapter;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.date.DateViewModel;
import com.example.usmentz.review.ReviewActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DateViewModel dateViewModel;
    private CategoryViewModel categoryViewModel;
    private RecyclerView categoriesRecyclerView;
    private RecyclerView momentsRecyclerView;
    private CategoryPortraitAdapter categoryAdapter;
    private DateAdapter dateAdapter;
    private LinearLayout emptyStateLayout;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private Date selectedDate;
    private static final String TAG = "MainActivity";

    private int currentCategoryId = -1;
    private Category currentCategory = null;
    private FloatingActionButton fabAdd;
    private MaterialToolbar toolbar;
    private Button btnAddCategory;
    private boolean isInMomentsMode = false; // Track if we're viewing moments

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main_categories);
            Log.d(TAG, "Layout inflated successfully");

            // Initialize date formatters
            calendar = Calendar.getInstance();
            selectedDate = calendar.getTime();
            dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            // Initialize views
            initViews();

            // Setup toolbar
            setupToolbar();

            // Setup ViewModels
            setupViewModels();

            // Setup RecyclerViews
            setupRecyclerViews();

            // Setup click listeners
            setupClickListeners();

            // Setup swipe to delete and drag drop for moments
            setupItemTouchHelpers();

            Log.d(TAG, "MainActivity initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        momentsRecyclerView = findViewById(R.id.momentsRecyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        // Hide moments view and FAB initially
        momentsRecyclerView.setVisibility(View.GONE);
        fabAdd.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Usmentz");
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    private void setupViewModels() {
        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
    }

    private void setupRecyclerViews() {
        // Setup categories grid (2 columns)
        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        categoriesRecyclerView.setHasFixedSize(true);
        categoryAdapter = new CategoryPortraitAdapter();
        categoriesRecyclerView.setAdapter(categoryAdapter);

        // Setup moments list
        momentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        momentsRecyclerView.setHasFixedSize(true);
        dateAdapter = new DateAdapter();
        momentsRecyclerView.setAdapter(dateAdapter);

        // Observe categories
        categoryViewModel.getAllCategories().observe(this, categories -> {
            if (categories == null || categories.isEmpty()) {
                // No categories - show empty state
                categoriesRecyclerView.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.VISIBLE);

                TextView emptyText = emptyStateLayout.findViewById(R.id.emptyStateText);
                TextView emptySubtext = emptyStateLayout.findViewById(R.id.emptyStateSubtext);
                if (emptyText != null && emptySubtext != null) {
                    emptyText.setText("No categories yet");
                    emptySubtext.setText("Tap + to create your first category");
                }
            } else {
                // Has categories - show categories grid
                categoriesRecyclerView.setVisibility(View.VISIBLE);
                emptyStateLayout.setVisibility(View.GONE);
                categoryAdapter.setCategories(categories);
            }
        });

        // Observe moments for selected category
        dateViewModel.getMoments().observe(this, moments -> {
            if (dateAdapter != null) {
                dateAdapter.setDates(moments);
            }

            // Update UI based on moments and mode
            if (isInMomentsMode && currentCategory != null) {
                if (moments == null || moments.isEmpty()) {
                    // No moments - show empty state for moments
                    momentsRecyclerView.setVisibility(View.GONE);
                    emptyStateLayout.setVisibility(View.VISIBLE);

                    TextView emptyText = emptyStateLayout.findViewById(R.id.emptyStateText);
                    TextView emptySubtext = emptyStateLayout.findViewById(R.id.emptyStateSubtext);
                    if (emptyText != null && emptySubtext != null) {
                        emptyText.setText("No moments in " + currentCategory.getName());
                        emptySubtext.setText("Tap + to add your first moment");
                    }
                } else {
                    // Has moments - show moments list
                    momentsRecyclerView.setVisibility(View.VISIBLE);
                    emptyStateLayout.setVisibility(View.GONE);
                }
            }
        });

        // Category click listener - ENTER MOMENTS MODE
        categoryAdapter.setOnCategoryClickListener(category -> {
            enterMomentsMode(category);
        });

        // Category edit listener
        categoryAdapter.setOnCategoryEditListener(category -> {
            if (category.getId() <= 6) {
                Toast.makeText(this, "Default categories cannot be edited", Toast.LENGTH_SHORT).show();
                return;
            }

            CategoryEditDialog.show(this, category, updatedCategory -> {
                categoryViewModel.update(updatedCategory);
                Toast.makeText(this, "Category updated: " + updatedCategory.getName(), Toast.LENGTH_SHORT).show();
            });
        });

        // Category delete listener
        categoryAdapter.setOnCategoryDeleteListener(category -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Category")
                    .setMessage("Are you sure you want to delete '" + category.getName() + "'?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        categoryViewModel.delete(category);
                        Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show();

                        // If we were viewing this category, go back to categories
                        if (currentCategory != null && currentCategory.getId() == category.getId()) {
                            exitMomentsMode();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void enterMomentsMode(CategoryDialog.Category category) {
        // Hide categories, show moments for this category
        currentCategory = category;
        currentCategoryId = category.getId();
        isInMomentsMode = true; // Set flag to true

        categoriesRecyclerView.setVisibility(View.GONE);

        // Check if category has moments (will be updated by observer)
        List<DateLocation> moments = dateAdapter.getDates();
        if (moments == null || moments.isEmpty()) {
            // No moments - show empty state
            momentsRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);

            TextView emptyText = emptyStateLayout.findViewById(R.id.emptyStateText);
            TextView emptySubtext = emptyStateLayout.findViewById(R.id.emptyStateSubtext);
            if (emptyText != null && emptySubtext != null) {
                emptyText.setText("No moments in " + category.getName());
                emptySubtext.setText("Tap + to add your first moment");
            }
        } else {
            // Has moments - show moments list
            momentsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }

        fabAdd.setVisibility(View.VISIBLE);

        // Update toolbar title and add back arrow
        toolbar.setTitle(category.getEmoji() + " " + category.getName());
        toolbar.setNavigationIcon(getDrawable(R.drawable.ic_back));

        // Load moments for this category
        dateViewModel.setCurrentCategory(currentCategoryId);
    }

    private void exitMomentsMode() {
        // Go back to categories view
        momentsRecyclerView.setVisibility(View.GONE);
        categoriesRecyclerView.setVisibility(View.VISIBLE);

        // Check if categories exist for empty state
        if (categoryAdapter.getItemCount() == 0) {
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
        }

        fabAdd.setVisibility(View.GONE);
        isInMomentsMode = false; // Reset flag

        // Reset toolbar title and remove back arrow
        if (toolbar != null) {
            toolbar.setTitle("Usmentz");
            toolbar.setNavigationIcon(null);
        }

        // Clear selected category
        currentCategory = null;
        currentCategoryId = -1;
    }

    private void setupClickListeners() {
        // Add category button
        if (btnAddCategory != null) {
            btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        }

        // Back button in toolbar to return to categories
        toolbar.setNavigationOnClickListener(v -> {
            if (isInMomentsMode) {
                exitMomentsMode();
                Log.d(TAG, "Toolbar back: redirected to categories");
            } else {
                // If already in categories, finish activity
                Log.d(TAG, "Toolbar back: exiting app");
                finish();
            }
        });

        // FAB click - Add moment
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                if (currentCategoryId == -1 || currentCategory == null) {
                    Toast.makeText(this, "Please select a category first", Toast.LENGTH_SHORT).show();
                } else {
                    showAddDialog();
                }
            });
        }

        // Date adapter listeners
        if (dateAdapter != null) {
            dateAdapter.setOnItemClickListener(dateLocation -> {
                if (dateLocation != null) {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
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
                    Intent intent = new Intent(MainActivity.this, ReviewActivity.class);
                    intent.putExtra("date_location", dateLocation);
                    startActivity(intent);
                }
            });
        }
    }

    private void setupItemTouchHelpers() {
        if (momentsRecyclerView == null || dateAdapter == null) return;

        // Swipe to delete for moments
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
        }).attachToRecyclerView(momentsRecyclerView);

        // Drag and drop for moments
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

        dragHelper.attachToRecyclerView(momentsRecyclerView);
    }

    @Override
    public void onBackPressed() {
        // If we're in moments mode, go back to categories
        if (isInMomentsMode) {
            exitMomentsMode();
            Log.d(TAG, "Back pressed: redirected to categories");
        } else {
            // If we're in categories view, exit the app
            Log.d(TAG, "Back pressed: exiting app from categories");
            super.onBackPressed();
        }
    }

    private void createDefaultCategories() {
        if (categoryViewModel == null) return;

        CategoryDialog.Category[] defaultCategories = {
                new CategoryDialog.Category("All", "📁", 0xFF6C3483),
                new CategoryDialog.Category("Favorites", "❤️", 0xFFC0392B),
                new CategoryDialog.Category("Food", "🍕", 0xFFE67E22),
                new CategoryDialog.Category("Travel", "✈️", 0xFF2980B9),
                new CategoryDialog.Category("Movies", "🎬", 0xFF27AE60),
                new CategoryDialog.Category("Music", "🎵", 0xFF8E44AD)
        };

        for (CategoryDialog.Category category : defaultCategories) {
            categoryViewModel.insert(category);
        }
    }

    private void showAddCategoryDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_add_category, null);
            builder.setView(dialogView);

            EditText etName = dialogView.findViewById(R.id.etCategoryName);
            Button btnEmojiDropdown = dialogView.findViewById(R.id.btnEmojiDropdown);
            Spinner spinnerColor = dialogView.findViewById(R.id.spinnerColor);
            Button btnCancel = dialogView.findViewById(R.id.btnCancel);
            Button btnCreate = dialogView.findViewById(R.id.btnCreate);

            if (etName == null || btnEmojiDropdown == null || spinnerColor == null ||
                    btnCancel == null || btnCreate == null) {
                Toast.makeText(this, "Dialog views not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Emoji options
            String[] emojis = {"📁", "❤️", "⭐", "🍕", "✈️", "🎬", "🎵", "📚", "🎁", "👤", "🏠", "💼", "🎓", "⚽", "🎨"};

            btnEmojiDropdown.setOnClickListener(v -> {
                AlertDialog.Builder emojiBuilder = new AlertDialog.Builder(this);
                emojiBuilder.setTitle("Choose Emoji");
                emojiBuilder.setItems(emojis, (dialog, which) -> {
                    btnEmojiDropdown.setText(emojis[which]);
                });
                emojiBuilder.show();
            });

            String[] colors = {"Purple", "Red", "Blue", "Green", "Orange", "Pink"};
            int[] colorValues = {0xFF9C27B0, 0xFFF44336, 0xFF2196F3, 0xFF4CAF50, 0xFFFF9800, 0xFFE91E63};

            ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, colors);
            colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerColor.setAdapter(colorAdapter);

            AlertDialog dialog = builder.create();
            dialog.show();

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnCreate.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) {
                    etName.setError("Category name required");
                    return;
                }

                String selectedEmoji = btnEmojiDropdown.getText().toString();
                int selectedColor = colorValues[spinnerColor.getSelectedItemPosition()];

                if (categoryViewModel != null) {
                    CategoryDialog.Category category = new CategoryDialog.Category(name, selectedEmoji, selectedColor);
                    categoryViewModel.insert(category);
                    Toast.makeText(this, "Category created: " + name, Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in showAddCategoryDialog", e);
            Toast.makeText(this, "Dialog error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddDialog() {
        if (currentCategoryId == -1 || currentCategory == null) {
            Toast.makeText(this, "Please select a category first", Toast.LENGTH_SHORT).show();
            return;
        }

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

            if (etName == null || etAddress == null || btnDate == null ||
                    tvSelectedDate == null || btnCancel == null || btnSave == null) {
                Toast.makeText(this, "Dialog views not found", Toast.LENGTH_SHORT).show();
                return;
            }

            tvSelectedDate.setText(dateFormat.format(selectedDate));

            btnDate.setOnClickListener(v -> {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        MainActivity.this,
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
                    newDate.setCategoryId(currentCategoryId);
                    dateViewModel.insert(newDate);
                    Toast.makeText(MainActivity.this, "Moment added to " + currentCategory.getName(), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in showAddDialog", e);
            Toast.makeText(this, "Dialog error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteDateLocation(DateLocation dateLocation) {
        if (dateLocation == null || momentsRecyclerView == null || dateViewModel == null) return;

        try {
            Snackbar snackbar = Snackbar.make(momentsRecyclerView, "Deleting moment...", Snackbar.LENGTH_LONG);
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
            Toast.makeText(this, "Delete error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}