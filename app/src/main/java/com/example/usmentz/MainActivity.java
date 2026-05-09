package com.example.usmentz;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.appbar.AppBarLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.adapter.DateAdapter;
import com.example.usmentz.category.CategoryAdapter;
import com.example.usmentz.category.Category;
import com.example.usmentz.category.CategoryEditDialog;
import com.example.usmentz.viewmodel.CategoryViewModel;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private DateViewModel dateViewModel;
    private CategoryViewModel categoryViewModel;
    private RecyclerView categoriesRecyclerView;
    private RecyclerView momentsRecyclerView;
    private CategoryAdapter categoryAdapter;
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
    private AppBarLayout appBarLayout;
    private Button btnAddCategory;
    private boolean isInMomentsMode = false;
    private boolean isInSpecialView = false;

    // Category name TextView inside the toolbar custom layout
    private TextView tvToolbarTitle;
    private View momentsHeader;
    private TextView tvCategoryTitle;
    
    // Header views from layout
    private TextView tvTime;
    private TextView tvDate;
    private TextView tvGreeting;

    // Floating navbar clickable areas
    private LinearLayout navHome, navCalendar, navReviews, navFavorites, navCategories;
    private LinearLayout navDates, navExpenses;
    private View floatingNavbarContainer;
    private View oldNavbarContainer;
    private static final int VIEW_CATEGORIES = 0;
    private int currentView = VIEW_CATEGORIES;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "UsmentzPrefs";
    private static final String KEY_LAST_CATEGORY_ID = "last_category_id";
    private static final String KEY_LAST_CATEGORY_NAME = "last_category_name";
    private static final String KEY_LAST_CATEGORY_ICON = "last_category_icon";
    private static final String KEY_LAST_CATEGORY_COLOR = "last_category_color";

    private final int[] emptyStateDrawables = { R.drawable.nocat22};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Disable transitions to prevent issues on some devices
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(Window.FEATURE_NO_TITLE, Window.FEATURE_NO_TITLE);
        
        super.onCreate(savedInstanceState);
        
        // Set content view FIRST
        setContentView(R.layout.activity_main_categories);
        
        // Apply white background
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        
        try {
            sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            calendar = Calendar.getInstance();
            selectedDate = calendar.getTime();
            dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            // Initialize views with null safety
            initViews();
            
            if (toolbar != null) {
                setupToolbar();
            }
            
            setupViewModels();
            setupRecyclerViews();
            setupClickListeners();
            setupItemTouchHelpers();
            setupBottomNavigation();
            restoreLastCategory();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setEmptyText(String title, String subtitle) {
        emptyStateLayout.setVisibility(View.VISIBLE);
        TextView emptyText = emptyStateLayout.findViewById(R.id.emptyStateText);
        TextView emptySubtext = emptyStateLayout.findViewById(R.id.emptyStateSubtext);
        ImageView emptyImage = emptyStateLayout.findViewById(R.id.emptyStateImage);
        if (emptyText != null) emptyText.setText(title);
        if (emptySubtext != null) emptySubtext.setText(subtitle);
        if (emptyImage != null && emptyStateDrawables.length > 0) {
            emptyImage.setImageResource(emptyStateDrawables[new Random().nextInt(emptyStateDrawables.length)]);
        }
    }

    private void hideEmptyState() {
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void initViews() {
        // Get views with null safety
        toolbar               = findViewById(R.id.toolbar);
        appBarLayout           = findViewById(R.id.appBarLayout);
        categoriesRecyclerView= findViewById(R.id.categoriesRecyclerView);
        momentsRecyclerView   = findViewById(R.id.momentsRecyclerView);
        fabAdd                = findViewById(R.id.fabAdd);
        emptyStateLayout      = findViewById(R.id.emptyStateLayout);
        btnAddCategory        = findViewById(R.id.btnAddCategory);

        // Floating navbar clickable areas
        navHome       = findViewById(R.id.navHome);
        navCalendar   = findViewById(R.id.navCalendar);
        navReviews    = findViewById(R.id.navReviews);
        navFavorites  = findViewById(R.id.navFavorites);
        navCategories = findViewById(R.id.navCategories);
        navDates = findViewById(R.id.navDates);
        navExpenses = findViewById(R.id.navExpenses);
        floatingNavbarContainer = findViewById(R.id.floatingNavbarContainer);
        oldNavbarContainer = findViewById(R.id.oldNavbarContainer);
        
        // Header views
        tvTime = findViewById(R.id.tvTime);
        tvDate = findViewById(R.id.tvDate);
        tvGreeting = findViewById(R.id.tvGreeting);
        momentsHeader = findViewById(R.id.momentsHeader);
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);

        // Debug: Log any missing views
        if (toolbar == null) Log.w(TAG, "toolbar is null!");
        if (categoriesRecyclerView == null) Log.w(TAG, "categoriesRecyclerView is null!");

        // Get custom title from toolbar
        if (toolbar != null) {
            tvToolbarTitle = toolbar.findViewById(R.id.tvToolbarTitle);
        }

        // Set visibility with null checks
        if (categoriesRecyclerView != null) categoriesRecyclerView.setVisibility(View.VISIBLE);
        if (momentsRecyclerView != null) momentsRecyclerView.setVisibility(View.GONE);
        if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);
        if (fabAdd != null) fabAdd.setVisibility(View.VISIBLE);
        if (btnAddCategory != null) btnAddCategory.setVisibility(View.VISIBLE);
        
        // Show OLD navbar by default, hide NEW floating navbar
        if (oldNavbarContainer != null) oldNavbarContainer.setVisibility(View.VISIBLE);
        if (floatingNavbarContainer != null) floatingNavbarContainer.setVisibility(View.GONE);
    }

    private void setupToolbar() {
        // Do NOT call setSupportActionBar — that overrides the custom XML layout
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (isInMomentsMode) exitMomentsMode();
                else finish();
            });
        }
    }

    private void setupViewModels() {
        dateViewModel    = new ViewModelProvider(this).get(DateViewModel.class);
        categoryViewModel= new ViewModelProvider(this).get(CategoryViewModel.class);
    }

    private void setupRecyclerViews() {
        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        categoriesRecyclerView.setHasFixedSize(true);
        
        // Disable ALL animations on RecyclerView
        categoriesRecyclerView.setItemAnimator(null);
        
        categoryAdapter = new CategoryAdapter();
        categoriesRecyclerView.setAdapter(categoryAdapter);

        momentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        momentsRecyclerView.setHasFixedSize(true);
        
        // Disable ALL animations on RecyclerView
        momentsRecyclerView.setItemAnimator(null);
        
        dateAdapter = new DateAdapter();
        momentsRecyclerView.setAdapter(dateAdapter);

        categoryViewModel.getAllCategories().observe(this, categories -> {
            if (categories != null) categoryAdapter.setCategories(categories);
            if (isInMomentsMode || currentView != VIEW_CATEGORIES) return;
            if (categories == null || categories.isEmpty()) {
                categoriesRecyclerView.setVisibility(View.GONE);
                momentsRecyclerView.setVisibility(View.GONE);
                setEmptyText("No categories yet", "Tap + to create your first category");
                btnAddCategory.setVisibility(View.VISIBLE);
            } else {
                categoriesRecyclerView.setVisibility(View.VISIBLE);
                hideEmptyState();
            }
        });

        dateViewModel.getMoments().observe(this, moments -> {
            if (!isInMomentsMode || currentCategory == null) return;
            if (dateAdapter != null) dateAdapter.setDates(moments);
            updateMomentsView(moments);
        });

        categoryAdapter.setOnCategoryClickListener(this::enterMomentsMode);

        categoryAdapter.setOnCategoryEditListener(category ->
                CategoryEditDialog.show(this, category, updatedCategory -> {
                    categoryViewModel.update(updatedCategory);
                    Toast.makeText(this, "Category updated: " + updatedCategory.getName(), Toast.LENGTH_SHORT).show();
                })
        );

        categoryAdapter.setOnCategoryDeleteListener(category -> {
            if (category.getItemCount() > 0) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Cannot Delete")
                        .setMessage("This category has " + category.getItemCount() + " moments. Delete all moments first.")
                        .setPositiveButton("OK", null).show();
                return;
            }
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Category")
                    .setMessage("Are you sure you want to delete '" + category.getName() + "'?")
                    .setPositiveButton("Delete", (d, w) -> {
                        categoryViewModel.delete(category);
                        Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show();
                        if (currentCategory != null && currentCategory.getId() == category.getId())
                            exitMomentsMode();
                    })
                    .setNegativeButton("Cancel", null).show();
        });
    }

    // ── Simple navigation — NO overridePendingTransition (theme handles it) ──
    private void navigateTo(Class<?> target) {
        startActivity(new Intent(this, target));
    }

    private void setupBottomNavigation() {
        // Home - navigate to HomeActivity
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // Clear saved category state so Categories always starts fresh
                sharedPreferences.edit().clear().apply();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            });
        }

        // Reviews - navigate to ReviewsActivity
        if (navReviews != null) {
            navReviews.setOnClickListener(v -> navigateTo(ReviewsActivity.class));
        }

        // Favorites - navigate to FavoritesActivity
        if (navFavorites != null) {
            navFavorites.setOnClickListener(v -> navigateTo(FavoritesActivity.class));
        }

        // Categories - switch back to categories view
        if (navCategories != null) {
            navCategories.setOnClickListener(v -> {
                switchToCategoriesView();
            });
        }

        // Calendar - navigate to CalendarActivity
        if (navCalendar != null) {
            navCalendar.setOnClickListener(v -> navigateTo(CalendarActivity.class));
        }

        // Dates/Moments (NEW floating navbar) - navigate to MomentsActivity
        if (navDates != null) {
            navDates.setOnClickListener(v -> navigateTo(MomentsActivity.class));
        }

        // Expenses (NEW floating navbar) - could navigate to expenses view
        if (navExpenses != null) {
            navExpenses.setOnClickListener(v -> {
                // Could add expenses activity navigation
            });
        }
    }

    private void enterMomentsMode(Category category) {
        currentCategory  = category;
        currentCategoryId= category.getId();
        isInMomentsMode  = true;
        isInSpecialView  = false;
        saveCurrentCategory();

        // Hide main header, show moments header instead
        if (appBarLayout != null) appBarLayout.setVisibility(View.GONE);
        if (momentsHeader != null) {
            momentsHeader.setVisibility(View.VISIBLE);
            if (tvCategoryTitle != null) tvCategoryTitle.setText(category.getName());
        }
        
        // Switch navbar containers: hide OLD, show NEW
        if (oldNavbarContainer != null) oldNavbarContainer.setVisibility(View.GONE);
        if (floatingNavbarContainer != null) {
            floatingNavbarContainer.setVisibility(View.VISIBLE);
            // Animate in
            floatingNavbarContainer.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        }
        
        categoriesRecyclerView.setVisibility(View.GONE);
        btnAddCategory.setVisibility(View.GONE);
        if (fabAdd != null) fabAdd.setVisibility(View.VISIBLE);
        hideEmptyState();

        dateViewModel.setCurrentCategory(currentCategoryId);

        // Update the custom title TextView — NOT toolbar.setTitle()
        if (tvToolbarTitle != null) tvToolbarTitle.setText(category.getName());
        if (toolbar != null) toolbar.setNavigationIcon(getDrawable(R.drawable.ic_back));
    }

    private void switchToCategoriesView() {
        currentView    = VIEW_CATEGORIES;
        isInSpecialView= false;

        if (isInMomentsMode) { exitMomentsMode(); return; }

        momentsRecyclerView.setVisibility(View.GONE);
        categoriesRecyclerView.setVisibility(View.VISIBLE);
        btnAddCategory.setVisibility(View.VISIBLE);
        if (fabAdd != null) fabAdd.setVisibility(View.VISIBLE);
        if (toolbar != null) toolbar.setNavigationIcon(null);

        // Restore custom title to "Usmentz"
        if (tvToolbarTitle != null) tvToolbarTitle.setText("Usmentz");

        if (categoryAdapter != null && categoryAdapter.getItemCount() == 0) {
            categoriesRecyclerView.setVisibility(View.GONE);
            setEmptyText("No categories yet", "Tap + to create your first category");
        } else {
            hideEmptyState();
        }
    }

    private void updateMomentsView(List<DateLocation> moments) {
        if (currentCategory == null) return;
        if (moments == null || moments.isEmpty()) {
            momentsRecyclerView.setVisibility(View.GONE);
            setEmptyText("No moments in " + currentCategory.getName(), "Tap + to add your first moment");
        } else {
            momentsRecyclerView.setVisibility(View.VISIBLE);
            hideEmptyState();
        }
    }



    private void exitMomentsMode() {
        sharedPreferences.edit().clear().apply();

        // Switch navbar containers: hide NEW, show OLD
        if (floatingNavbarContainer != null) {
            floatingNavbarContainer.setVisibility(View.GONE);
        }
        if (oldNavbarContainer != null) {
            oldNavbarContainer.setVisibility(View.VISIBLE);
        }

        // Show header back
        if (appBarLayout != null) appBarLayout.setVisibility(View.VISIBLE);
        if (momentsHeader != null) momentsHeader.setVisibility(View.GONE);

        momentsRecyclerView.setVisibility(View.GONE);
        categoriesRecyclerView.setVisibility(View.VISIBLE);
        btnAddCategory.setVisibility(View.VISIBLE);
        if (fabAdd != null) fabAdd.setVisibility(View.VISIBLE);
        isInMomentsMode= false;
        currentView    = VIEW_CATEGORIES;

        if (toolbar != null) toolbar.setNavigationIcon(null);
        // Restore custom title — NOT toolbar.setTitle()
        if (tvToolbarTitle != null) tvToolbarTitle.setText("Usmentz");

        if (categoryAdapter != null && categoryAdapter.getItemCount() == 0) {
            categoriesRecyclerView.setVisibility(View.GONE);
            setEmptyText("No categories yet", "Tap + to create your first category");
        } else {
            hideEmptyState();
        }

        currentCategory  = null;
        currentCategoryId= -1;
    }

    private void setupClickListeners() {
        if (btnAddCategory != null)
            btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        toolbar.setNavigationOnClickListener(v -> {
            if (isInMomentsMode) exitMomentsMode();
            else finish();
        });

        if (fabAdd != null)
            fabAdd.setOnClickListener(v -> {
                Log.d(TAG, "FAB clicked! currentCategoryId=" + currentCategoryId + ", currentCategory=" + currentCategory);
                if (currentCategoryId == -1 || currentCategory == null) {
                    // No category selected - show dialog to add new category first
                    showAddCategoryDialog();
                } else {
                    showAddDialog();
                }
            });

        if (dateAdapter != null) {
            dateAdapter.setOnItemClickListener(d -> {
                if (d != null) {
                    Intent i = new Intent(MainActivity.this, DetailActivity.class);
                    i.putExtra("date_location", d);
                    startActivity(i);
                }
            });

            dateAdapter.setOnItemDeleteListener(this::deleteDateLocation);

            dateAdapter.setOnItemCompleteListener((d, c) -> {
                if (d != null) {
                    dateViewModel.update(d);
                    if (isInMomentsMode && currentCategory != null)
                        new Handler().postDelayed(() -> dateViewModel.setCurrentCategory(currentCategoryId), 300);
                }
            });

            dateAdapter.setOnRatingChangeListener((d, r) -> {
                if (d != null) {
                    dateViewModel.update(d);
                    if (isInMomentsMode && currentCategory != null)
                        new Handler().postDelayed(() -> dateViewModel.setCurrentCategory(currentCategoryId), 300);
                }
            });

            dateAdapter.setOnReviewClickListener(d -> {
                if (d != null) {
                    Intent i = new Intent(MainActivity.this, ReviewsActivity.class);
                    i.putExtra("date_location", d);
                    startActivity(i);
                }
            });
        }
    }

    private void setupItemTouchHelpers() {
        if (momentsRecyclerView == null || dateAdapter == null) return;

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getAdapterPosition();
                List<DateLocation> dates = dateAdapter.getDates();
                if (dates != null && pos >= 0 && pos < dates.size()) deleteDateLocation(dates.get(pos));
            }
        }).attachToRecyclerView(momentsRecyclerView);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) {
                dateAdapter.onItemMove(vh.getAdapterPosition(), t.getAdapterPosition());
                return true;
            }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {}
            @Override public boolean isLongPressDragEnabled() { return false; }
        }).attachToRecyclerView(momentsRecyclerView);
    }

    @Override
    public void onBackPressed() {
        if (isInMomentsMode) exitMomentsMode();
        else super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from other activities (e.g., DetailActivity)
        // This ensures any changes made (new moment, deleted moment, etc.) are reflected
        if (categoryViewModel != null && categoryViewModel.getAllCategories() != null) {
            categoryViewModel.getAllCategories().observe(this, categories -> {
                if (categories != null && categoryAdapter != null) {
                    categoryAdapter.setCategories(categories);
                    if (!isInMomentsMode && currentView == VIEW_CATEGORIES) {
                        if (categories == null || categories.isEmpty()) {
                            categoriesRecyclerView.setVisibility(View.GONE);
                            momentsRecyclerView.setVisibility(View.GONE);
                            setEmptyText("No categories yet", "Tap + to create your first category");
                            btnAddCategory.setVisibility(View.VISIBLE);
                        } else {
                            categoriesRecyclerView.setVisibility(View.VISIBLE);
                            hideEmptyState();
                        }
                    }
                }
            });
        }
        // Also refresh moments if in moments mode
        if (isInMomentsMode && currentCategoryId > 0 && dateViewModel != null) {
            dateViewModel.setCurrentCategory(currentCategoryId);
        }
    }

    private void saveCurrentCategory() {
        if (currentCategory == null) return;
        sharedPreferences.edit()
                .putInt(KEY_LAST_CATEGORY_ID, currentCategoryId)
                .putString(KEY_LAST_CATEGORY_NAME, currentCategory.getName())
                .putString(KEY_LAST_CATEGORY_ICON, currentCategory.getIconName())
                .putInt(KEY_LAST_CATEGORY_COLOR, currentCategory.getColor())
                .apply();
    }

    private void restoreLastCategory() {
        int savedId = sharedPreferences.getInt(KEY_LAST_CATEGORY_ID, -1);
        if (savedId == -1) { switchToCategoriesView(); return; }

        String savedName = sharedPreferences.getString(KEY_LAST_CATEGORY_NAME, "");
        String savedIcon = sharedPreferences.getString(KEY_LAST_CATEGORY_ICON, "folder");
        int savedColor   = sharedPreferences.getInt(KEY_LAST_CATEGORY_COLOR, 0xFF9C27B0);

        if (!savedName.isEmpty()) {
            currentCategory  = new Category(savedName, savedIcon, savedColor);
            currentCategory.setId(savedId);
            currentCategoryId= savedId;
            isInMomentsMode  = true;

            dateViewModel.setCurrentCategory(currentCategoryId);

            categoriesRecyclerView.setVisibility(View.GONE);
            btnAddCategory.setVisibility(View.GONE);
            if (fabAdd != null) fabAdd.setVisibility(View.VISIBLE);

            // Update custom title — NOT toolbar.setTitle()
            if (tvToolbarTitle != null) tvToolbarTitle.setText(savedName);
            if (toolbar != null) toolbar.setNavigationIcon(getDrawable(R.drawable.ic_back));
        } else {
            switchToCategoriesView();
        }
    }

    private void showAddCategoryDialog() {
        try {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(dialogView);

            EditText etName        = dialogView.findViewById(R.id.etCategoryName);
            Button btnIconDropdown = dialogView.findViewById(R.id.btnIconDropdown);
            ImageView ivIconPreview= dialogView.findViewById(R.id.ivIconPreview);
            TextView tvIconPreview = dialogView.findViewById(R.id.tvIconPreview);
            Spinner spinnerColor   = dialogView.findViewById(R.id.spinnerColor);
            Button btnCancel       = dialogView.findViewById(R.id.btnCancel);
            Button btnCreate       = dialogView.findViewById(R.id.btnCreate);

            final int[] imageResources   = { R.drawable.folder, R.drawable.heart, R.drawable.star, R.drawable.food, R.drawable.travel, R.drawable.movie, R.drawable.music, R.drawable.book, R.drawable.gift };
            final String[] imageNames    = { "folder","heart","star","food","travel","movie","music","book","gift" };
            final String[] imageDisplay  = { "Folder","Heart","Star","Food","Travel","Movie","Music","Book","Gift" };
            final int[] idx = {0};

            // Color values matching gradient circles
            final int[] colorValues = {
                0xFF9B5CFF,  // Purple
                0xFFFF5252,  // Red
                0xFF2196F3,  // Blue
                0xFF4CAF50,  // Green
                0xFFFF9800,  // Orange
                0xFFE91E63   // Pink
            };
            final int[] selectedColor = {0}; // Default to first color (purple)

            // Update selected color hint
            if (tvIconPreview != null) {
                tvIconPreview.setTextColor(colorValues[0]);
            }

            btnIconDropdown.setText(imageDisplay[0]);
            tvIconPreview.setText(imageDisplay[0]);
            if (ivIconPreview != null) {
                ivIconPreview.setImageResource(imageResources[0]);
            }

            btnIconDropdown.setOnClickListener(v -> {
                idx[0] = (idx[0] + 1) % imageResources.length;
                btnIconDropdown.setText(imageDisplay[idx[0]]);
                tvIconPreview.setText(imageDisplay[idx[0]]);
                if (ivIconPreview != null) {
                    ivIconPreview.setImageResource(imageResources[idx[0]]);
                }
            });

            // Color circle click listeners
            View colorPurple = dialogView.findViewById(R.id.colorPurple);
            View colorRed = dialogView.findViewById(R.id.colorRed);
            View colorBlue = dialogView.findViewById(R.id.colorBlue);
            View colorGreen = dialogView.findViewById(R.id.colorGreen);
            View colorOrange = dialogView.findViewById(R.id.colorOrange);
            View colorPink = dialogView.findViewById(R.id.colorPink);

            View.OnClickListener colorListener = v -> {
                if (v == colorPurple) selectedColor[0] = colorValues[0];
                else if (v == colorRed) selectedColor[0] = colorValues[1];
                else if (v == colorBlue) selectedColor[0] = colorValues[2];
                else if (v == colorGreen) selectedColor[0] = colorValues[3];
                else if (v == colorOrange) selectedColor[0] = colorValues[4];
                else if (v == colorPink) selectedColor[0] = colorValues[5];
                
                // Update preview with selected color
                if (tvIconPreview != null) {
                    tvIconPreview.setTextColor(selectedColor[0]);
                }
            };

            if (colorPurple != null) colorPurple.setOnClickListener(colorListener);
            if (colorRed != null) colorRed.setOnClickListener(colorListener);
            if (colorBlue != null) colorBlue.setOnClickListener(colorListener);
            if (colorGreen != null) colorGreen.setOnClickListener(colorListener);
            if (colorOrange != null) colorOrange.setOnClickListener(colorListener);
            if (colorPink != null) colorPink.setOnClickListener(colorListener);

            AlertDialog dialog = builder.create();
            dialog.show();

            btnCancel.setOnClickListener(v -> dialog.dismiss());
            btnCreate.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) { etName.setError("Category name required"); return; }
                categoryViewModel.insert(new Category(name, imageNames[idx[0]], selectedColor[0]));
                Toast.makeText(this, "Category created: " + name, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in showAddCategoryDialog", e);
        }
    }

    private void showAddDialog() {
        if (currentCategoryId == -1 || currentCategory == null) return;
        try {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_date_simple, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(dialogView);

            EditText etName        = dialogView.findViewById(R.id.etName);
            EditText etAddress     = dialogView.findViewById(R.id.etAddress);
            EditText etDescription = dialogView.findViewById(R.id.etDescription);
            Button btnDate         = dialogView.findViewById(R.id.btnSelectDate);
            TextView tvDate        = dialogView.findViewById(R.id.tvSelectedDate);
            Button btnCancel       = dialogView.findViewById(R.id.btnCancel);
            Button btnSave         = dialogView.findViewById(R.id.btnSave);

            tvDate.setText(dateFormat.format(selectedDate));
            btnDate.setOnClickListener(v -> new DatePickerDialog(this, (view, y, m, d) -> {
                calendar.set(y, m, d);
                selectedDate = calendar.getTime();
                tvDate.setText(dateFormat.format(selectedDate));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show());

            AlertDialog dialog = builder.create();
            dialog.show();

            btnCancel.setOnClickListener(v -> dialog.dismiss());
            btnSave.setOnClickListener(v -> {
                String name    = etName.getText().toString().trim();
                String address = etAddress.getText().toString().trim();
                String desc    = etDescription.getText().toString().trim();
                if (TextUtils.isEmpty(name))    { etName.setError("Name is required"); return; }
                if (TextUtils.isEmpty(address)) { etAddress.setError("Address is required"); return; }
                DateLocation newDate = new DateLocation(name, address, desc, selectedDate);
                newDate.setCategoryId(currentCategoryId);
                dateViewModel.insert(newDate);
                dialog.dismiss();
                Toast.makeText(this, "Moment added to " + currentCategory.getName(), Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in showAddDialog", e);
        }
    }

    private void deleteDateLocation(DateLocation d) {
        if (d == null || dateViewModel == null) return;
        try {
            View anchor = momentsRecyclerView != null ? momentsRecyclerView : findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(anchor, "Moment deleted", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", v -> dateViewModel.insert(d));
            snackbar.addCallback(new Snackbar.Callback() {
                @Override public void onDismissed(Snackbar bar, int event) {
                    if (event != DISMISS_EVENT_ACTION) dateViewModel.delete(d);
                }
            });
            snackbar.show();
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteDateLocation", e);
        }
    }
}