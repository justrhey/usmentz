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
import com.example.usmentz.category.CategoryDialog;
import com.example.usmentz.category.CategoryEditDialog;
import com.example.usmentz.viewmodel.CategoryViewModel;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import com.example.usmentz.AddMomentDialog;

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
    private boolean isNavVisible = true;
    private Handler hideHandler;
    private Runnable hideRunnable;
    private static final long HIDE_DELAY_MS = 4000;

    private boolean isInMomentsMode = false;
    private boolean isInSpecialView = false;
    private boolean isFabVisible = true;

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

    private final int[] emptyStateDrawables = { R.drawable.nocat };

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

        // FAB + navbar auto-hide on scroll for momentsRecyclerView
        if (momentsRecyclerView != null) {
            momentsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0 && isFabVisible) {
                        isFabVisible = false;
                        if (fabAdd != null) {
                            fabAdd.animate().alpha(0f).setDuration(200).withEndAction(() -> fabAdd.setVisibility(View.GONE)).start();
                        }
                    } else if (dy < 0 && !isFabVisible) {
                        isFabVisible = true;
                        if (fabAdd != null) {
                            fabAdd.setVisibility(View.VISIBLE);
                            fabAdd.animate().alpha(1f).setDuration(200).start();
                        }
                    }
                }
            });
        }
    }

    // ── Navbar auto-hide ───────────────────────────

    private void resetHideTimer() {
        if (hideHandler != null && hideRunnable != null) {
            hideHandler.removeCallbacks(hideRunnable);
        }
        startHideTimer();
    }

    private void startHideTimer() {
        if (hideHandler == null) {
            hideHandler = new Handler();
        }
        hideRunnable = () -> {
            isNavVisible = false;
            if (floatingNavbarContainer != null) {
                floatingNavbarContainer.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> floatingNavbarContainer.setVisibility(View.GONE))
                        .start();
            }
            if (momentsHeader != null) {
                momentsHeader.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> momentsHeader.setVisibility(View.GONE))
                        .start();
            }
        };
        hideHandler.postDelayed(hideRunnable, HIDE_DELAY_MS);
    }

    private void showNav() {
        if (!isNavVisible) {
            isNavVisible = true;
            if (floatingNavbarContainer != null) {
                floatingNavbarContainer.setVisibility(View.VISIBLE);
                floatingNavbarContainer.animate().alpha(1f).setDuration(200).start();
            }
            if (momentsHeader != null) {
                momentsHeader.setVisibility(View.VISIBLE);
                momentsHeader.animate().alpha(1f).setDuration(200).start();
            }
            resetHideTimer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isInMomentsMode) startHideTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (hideHandler != null && hideRunnable != null) {
            hideHandler.removeCallbacks(hideRunnable);
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

            // Re-create category object for enterMomentsMode
            Category cat = new Category(savedName, savedIcon, savedColor);
            cat.setId(savedId);
            enterMomentsMode(cat);
        } else {
            switchToCategoriesView();
        }
    }

    private void showAddCategoryDialog() {
        CategoryDialog.show(this, category -> {
            categoryViewModel.insert(category);
            Toast.makeText(this, "Category created: " + category.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showAddDialog() {
        AddMomentDialog dialog = AddMomentDialog.newInstance(moment -> {
            Toast.makeText(this, "Added to " + currentCategory.getName(), Toast.LENGTH_SHORT).show();
        });
        dialog.show(getSupportFragmentManager(), "AddMomentDialog");
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

    // Missing methods that need to be implemented

    private void setupClickListeners() {
        // FAB click listener for adding moments
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                if (isInMomentsMode && currentCategory != null) {
                    showAddDialog();
                } else {
                    showAddCategoryDialog();
                }
            });
        }

        // Add category button
        if (btnAddCategory != null) {
            btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        }

        // Moments header back button
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> exitMomentsMode());
        }
    }

    private void setupItemTouchHelpers() {
        // Setup item touch helpers for swipe to delete if needed
    }

    private void setupBottomNavigation() {
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }
        if (navCalendar != null) {
            navCalendar.setOnClickListener(v -> {
                Intent intent = new Intent(this, CalendarActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }
        if (navReviews != null) {
            navReviews.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReviewsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }
        if (navFavorites != null) {
            navFavorites.setOnClickListener(v -> {
                Intent intent = new Intent(this, FavoritesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }
        if (navCategories != null) {
            navCategories.setOnClickListener(v -> {
                switchToCategoriesView();
            });
        }
        if (navDates != null) {
            navDates.setOnClickListener(v -> {
                // All moments view - stay in categories but switch RecyclerView
            });
        }
        if (navExpenses != null) {
            navExpenses.setOnClickListener(v -> {
                // Expenses view - could navigate to expenses screen later
            });
        }
    }

    private void enterMomentsMode(Category category) {
        if (category == null) return;

        currentCategory = category;
        currentCategoryId = category.getId();
        isInMomentsMode = true;

        // Switch RecyclerView visibility
        if (categoriesRecyclerView != null) {
            categoriesRecyclerView.setVisibility(View.GONE);
        }
        if (momentsRecyclerView != null) {
            momentsRecyclerView.setVisibility(View.VISIBLE);
        }
        if (btnAddCategory != null) {
            btnAddCategory.setVisibility(View.GONE);
        }
        hideEmptyState();

        // Show moments header
        if (momentsHeader != null) {
            momentsHeader.setVisibility(View.VISIBLE);
            momentsHeader.setAlpha(0f);
            momentsHeader.animate().alpha(1f).setDuration(200).start();
        }

        // Show category title
        if (tvCategoryTitle != null) {
            tvCategoryTitle.setText(category.getName());
        }

        // Show floating navbar
        if (floatingNavbarContainer != null) {
            floatingNavbarContainer.setVisibility(View.VISIBLE);
            floatingNavbarContainer.setAlpha(0f);
            floatingNavbarContainer.animate().alpha(1f).setDuration(200).start();
        }

        // Hide old navbar
        if (oldNavbarContainer != null) {
            oldNavbarContainer.setVisibility(View.GONE);
        }

        // Load moments for this category
        if (dateViewModel != null && category.getId() > 0) {
            dateViewModel.setCurrentCategory(category.getId());
        }

        // Start hide timer
        startHideTimer();

        // Save current category
        saveCurrentCategory();

        currentView = 1; // Moments view
    }

    private void exitMomentsMode() {
        isInMomentsMode = false;
        currentCategory = null;
        currentCategoryId = -1;

        // Cancel hide timer
        if (hideHandler != null && hideRunnable != null) {
            hideHandler.removeCallbacks(hideRunnable);
        }

        // Switch RecyclerView visibility
        if (momentsRecyclerView != null) {
            momentsRecyclerView.setVisibility(View.GONE);
        }
        if (categoriesRecyclerView != null) {
            categoriesRecyclerView.setVisibility(View.VISIBLE);
        }
        if (btnAddCategory != null) {
            btnAddCategory.setVisibility(View.VISIBLE);
        }

        // Hide moments header
        if (momentsHeader != null) {
            momentsHeader.setVisibility(View.GONE);
        }

        // Hide floating navbar, show old navbar
        if (floatingNavbarContainer != null) {
            floatingNavbarContainer.setVisibility(View.GONE);
        }
        if (oldNavbarContainer != null) {
            oldNavbarContainer.setVisibility(View.VISIBLE);
        }

        // Reset dateViewModel
        if (dateViewModel != null) {
            dateViewModel.setCurrentCategory(-1);
        }

        currentView = VIEW_CATEGORIES;
    }

    private void switchToCategoriesView() {
        exitMomentsMode();
    }

    private void updateMomentsView(List<DateLocation> moments) {
        if (moments == null || moments.isEmpty()) {
            setEmptyText("No moments yet", "Tap + to add your first moment");
            if (momentsRecyclerView != null) momentsRecyclerView.setVisibility(View.GONE);
        } else {
            hideEmptyState();
            if (momentsRecyclerView != null) momentsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}