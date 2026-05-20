package com.example.usmentz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.adapter.DateAdapter;
import com.example.usmentz.category.Category;
import com.example.usmentz.category.CategoryAdapter;
import com.example.usmentz.category.CategoryDialog;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.helper.CategoryStateHelper;
import com.example.usmentz.helper.SuggestionHelper;
import com.example.usmentz.helper.SuggestionHelper.Suggestion;
import com.example.usmentz.viewmodel.CategoryViewModel;
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DateViewModel dateViewModel;
    private CategoryViewModel categoryViewModel;
    private CategoryAdapter categoryAdapter;
    private DateAdapter dateAdapter;

    private static final String TAG = "MainActivity";

    // Helpers
    private CategoryStateHelper categoryStateHelper;

    // State
    private boolean isInMomentsMode = false;
    private Category currentCategory;

    // Header views
    private View headerContainer;
    private View categoriesHeader;
    private View momentsHeader;
    private TextView tvDate, tvGreeting;
    private TextView tvCategoryTitle, tvMomentCount;

    // Content views
    private RecyclerView categoriesRecyclerView;
    private RecyclerView momentsRecyclerView;
    private View emptyStateNoCategories;
    private View emptyStateNoMoments;
    private View suggestionCardContainer;
    private TextView tvSuggestionTitle, tvSuggestionText;

    // Navigation - slot containers
    private View navbarContainer;
    private View navItemHome, navItemCategories, navItemCalendar;
    // Active states (nested pills)
    private View navHomeActive, navCategoriesActive, navCalendarActive;
    private ImageView navCategoriesActiveIcon;
    private TextView navCategoriesActiveLabel;
    // Inactive states (icons only)
    private View navHomeInactive, navCategoriesInactive, navCalendarInactive;

    // Track active nav slot (0=Home, 1=Categories, 2=Calendar)
    private int activeNavSlot = 1; // Default: Categories active

    // FAB
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_categories);
        getWindow().setBackgroundDrawableResource(android.R.color.white);

        try {
            initViews();
            initHelpers();
            setupViewModels();
            setupRecyclerViews();
            setupClickListeners();
            setupDateHeader();
            restoreLastCategory();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        // Header
        headerContainer = findViewById(R.id.headerContainer);
        categoriesHeader = findViewById(R.id.categoriesHeader);
        momentsHeader = findViewById(R.id.momentsHeader);
        tvDate = findViewById(R.id.tvDate);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        tvMomentCount = findViewById(R.id.tvMomentCount);

        // Content
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        momentsRecyclerView = findViewById(R.id.momentsRecyclerView);
        emptyStateNoCategories = findViewById(R.id.emptyStateNoCategories);
        emptyStateNoMoments = findViewById(R.id.emptyStateNoMoments);
        fabAdd = findViewById(R.id.fabAdd);

        // Suggestion card
        suggestionCardContainer = findViewById(R.id.suggestionCardContainer);
        if (suggestionCardContainer != null) {
            tvSuggestionTitle = suggestionCardContainer.findViewById(R.id.tvSuggestionTitle);
            tvSuggestionText = suggestionCardContainer.findViewById(R.id.tvSuggestionText);
            View btnDismiss = suggestionCardContainer.findViewById(R.id.btnDismissSuggestion);
            if (btnDismiss != null) {
                btnDismiss.setOnClickListener(v -> suggestionCardContainer.setVisibility(View.GONE));
            }
        }

        // Navbar - slot containers
        navbarContainer = findViewById(R.id.navbarContainer);
        navItemHome = findViewById(R.id.navItemHome);
        navItemCategories = findViewById(R.id.navItemCategories);
        navItemCalendar = findViewById(R.id.navItemCalendar);

        // Active states (nested pills)
        navHomeActive = findViewById(R.id.navHomeActive);
        navCategoriesActive = findViewById(R.id.navCategoriesActive);
        navCalendarActive = findViewById(R.id.navCalendarActive);
        navCategoriesActiveIcon = findViewById(R.id.navCategoriesActiveIcon);
        navCategoriesActiveLabel = findViewById(R.id.navCategoriesActiveLabel);

        // Inactive states (icons only)
        navHomeInactive = findViewById(R.id.navHomeInactive);
        navCategoriesInactive = findViewById(R.id.navCategoriesInactive);
        navCalendarInactive = findViewById(R.id.navCalendarInactive);

        // Default visibility
        categoriesRecyclerView.setVisibility(View.VISIBLE);
        momentsRecyclerView.setVisibility(View.GONE);
        emptyStateNoCategories.setVisibility(View.GONE);
        emptyStateNoMoments.setVisibility(View.GONE);
        categoriesHeader.setVisibility(View.VISIBLE);
        momentsHeader.setVisibility(View.GONE);

        // Set initial navbar state (Categories active by default)
        setActiveNavSlot(1);
    }

    private void initHelpers() {
        categoryStateHelper = new CategoryStateHelper(this);
    }

    private void setupDateHeader() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        tvDate.setText(dateFormat.format(new Date()));
    }

    private void setupViewModels() {
        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
    }

    private void setupRecyclerViews() {
        // Categories grid
        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        categoriesRecyclerView.setHasFixedSize(true);
        categoriesRecyclerView.setItemAnimator(null);
        categoryAdapter = new CategoryAdapter();
        categoriesRecyclerView.setAdapter(categoryAdapter);

        // Moments list
        momentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        momentsRecyclerView.setHasFixedSize(true);
        momentsRecyclerView.setItemAnimator(null);
        dateAdapter = new DateAdapter();
        momentsRecyclerView.setAdapter(dateAdapter);

        // Observe categories
        categoryViewModel.getAllCategories().observe(this, categories -> {
            if (categories != null) categoryAdapter.setCategories(categories);
            if (isInMomentsMode) return;
            updateCategoriesView(categories);
        });

        // Observe moments
        dateViewModel.getMoments().observe(this, moments -> {
            if (!isInMomentsMode || currentCategory == null) return;
            if (dateAdapter != null) dateAdapter.setDates(moments);
            updateMomentsView(moments);
            updateSuggestion(moments);
            if (tvMomentCount != null) {
                int count = moments != null ? moments.size() : 0;
                tvMomentCount.setText(count + " moment" + (count != 1 ? "s" : ""));
            }
        });

        categoryAdapter.setOnCategoryClickListener(this::enterMomentsMode);

        // Moments item click → open detail
        dateAdapter.setOnItemClickListener(dateLocation -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("date_location", dateLocation);
            startActivity(intent);
        });

        // Moments item delete → undo snackbar
        dateAdapter.setOnItemDeleteListener(this::deleteDateLocation);

        // FAB auto-hide on scroll
        momentsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fabAdd.getVisibility() == View.VISIBLE) {
                    fabAdd.animate().alpha(0f).setDuration(200)
                            .withEndAction(() -> fabAdd.setVisibility(View.GONE)).start();
                } else if (dy < 0 && fabAdd.getVisibility() == View.GONE) {
                    fabAdd.setVisibility(View.VISIBLE);
                    fabAdd.animate().alpha(1f).setDuration(200).start();
                }
            }
        });
    }

    private void updateCategoriesView(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            categoriesRecyclerView.setVisibility(View.GONE);
            emptyStateNoCategories.setVisibility(View.VISIBLE);
            emptyStateNoMoments.setVisibility(View.GONE);
        } else {
            categoriesRecyclerView.setVisibility(View.VISIBLE);
            emptyStateNoCategories.setVisibility(View.GONE);
            emptyStateNoMoments.setVisibility(View.GONE);
        }
    }

    private void updateMomentsView(List<DateLocation> moments) {
        if (moments == null || moments.isEmpty()) {
            momentsRecyclerView.setVisibility(View.GONE);
            emptyStateNoMoments.setVisibility(View.VISIBLE);
        } else {
            momentsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateNoMoments.setVisibility(View.GONE);
        }
    }

    private void updateSuggestion(List<DateLocation> moments) {
        if (suggestionCardContainer == null || tvSuggestionTitle == null || tvSuggestionText == null) return;

        Suggestion suggestion = SuggestionHelper.generateSuggestion(moments);
        if (suggestion != null) {
            tvSuggestionTitle.setText(suggestion.title);
            tvSuggestionText.setText(suggestion.description);
            suggestionCardContainer.setVisibility(View.VISIBLE);
        } else {
            suggestionCardContainer.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // FAB: add category or add moment depending on mode
        fabAdd.setOnClickListener(v -> {
            if (isInMomentsMode && currentCategory != null) {
                showAddMomentDialog();
            } else {
                showAddCategoryDialog();
            }
        });

        // Empty state button
        Button btnAddCategory = findViewById(R.id.btnAddCategory);
        if (btnAddCategory != null) {
            btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        }

        // Back button in moments header
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> exitMomentsMode());
        }

        // Navbar
        setupNavigation();
    }

    private void setupNavigation() {
        // Home slot
        navItemHome.setOnClickListener(v -> {
            setActiveNavSlot(0);
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        // Calendar slot
        navItemCalendar.setOnClickListener(v -> {
            setActiveNavSlot(2);
            Intent intent = new Intent(this, CalendarActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        // Categories / Back slot
        navItemCategories.setOnClickListener(v -> {
            if (isInMomentsMode) {
                exitMomentsMode();
            } else {
                setActiveNavSlot(1);
                categoriesRecyclerView.smoothScrollToPosition(0);
            }
        });
    }

    /**
     * Sets which nav slot is active (expanded pill with icon+text).
     * All other slots show only their icon.
     * @param slotIndex 0=Home, 1=Categories, 2=Calendar
     */
    private void setActiveNavSlot(int slotIndex) {
        activeNavSlot = slotIndex;

        // Home slot
        if (slotIndex == 0) {
            navHomeActive.setVisibility(View.VISIBLE);
            navHomeInactive.setVisibility(View.GONE);
        } else {
            navHomeActive.setVisibility(View.GONE);
            navHomeInactive.setVisibility(View.VISIBLE);
        }

        // Categories slot
        if (slotIndex == 1) {
            navCategoriesActive.setVisibility(View.VISIBLE);
            navCategoriesInactive.setVisibility(View.GONE);
        } else {
            navCategoriesActive.setVisibility(View.GONE);
            navCategoriesInactive.setVisibility(View.VISIBLE);
        }

        // Calendar slot
        if (slotIndex == 2) {
            navCalendarActive.setVisibility(View.VISIBLE);
            navCalendarInactive.setVisibility(View.GONE);
        } else {
            navCalendarActive.setVisibility(View.GONE);
            navCalendarInactive.setVisibility(View.VISIBLE);
        }
    }

    private void enterMomentsMode(Category category) {
        if (category == null) return;
        currentCategory = category;
        isInMomentsMode = true;

        // Switch views
        categoriesRecyclerView.setVisibility(View.GONE);
        momentsRecyclerView.setVisibility(View.VISIBLE);
        emptyStateNoCategories.setVisibility(View.GONE);
        emptyStateNoMoments.setVisibility(View.GONE);

        // Switch headers
        categoriesHeader.setVisibility(View.GONE);
        momentsHeader.setVisibility(View.VISIBLE);
        momentsHeader.setAlpha(0f);
        momentsHeader.animate().alpha(1f).setDuration(200).start();

        // Update navbar: change Categories slot to Back
        if (navCategoriesActiveIcon != null) {
            navCategoriesActiveIcon.setImageResource(R.drawable.ic_back);
        }
        if (navCategoriesActiveLabel != null) {
            navCategoriesActiveLabel.setText("Back");
        }
        // Also update inactive icon
        if (navCategoriesInactive != null) {
            ((ImageView) navCategoriesInactive).setImageResource(R.drawable.ic_back);
        }

        // Set category title
        if (tvCategoryTitle != null) {
            tvCategoryTitle.setText(category.getName());
        }

        // Switch ViewModel to this category
        if (category.getId() > 0) {
            dateViewModel.setCurrentCategory(category.getId());
        }

        // Save state
        categoryStateHelper.saveCategory(category);
    }

    void exitMomentsMode() {
        isInMomentsMode = false;
        currentCategory = null;

        // Switch views
        momentsRecyclerView.setVisibility(View.GONE);
        emptyStateNoMoments.setVisibility(View.GONE);

        // Switch headers
        momentsHeader.setVisibility(View.GONE);
        categoriesHeader.setVisibility(View.VISIBLE);
        categoriesHeader.setAlpha(0f);
        categoriesHeader.animate().alpha(1f).setDuration(200).start();

        // Restore navbar: change Back to Categories
        if (navCategoriesActiveIcon != null) {
            navCategoriesActiveIcon.setImageResource(R.drawable.ic_category);
        }
        if (navCategoriesActiveLabel != null) {
            navCategoriesActiveLabel.setText("Categories");
        }
        // Also update inactive icon
        if (navCategoriesInactive != null) {
            ((ImageView) navCategoriesInactive).setImageResource(R.drawable.ic_category);
        }

        // Switch ViewModel back to all moments
        dateViewModel.setCurrentCategory(-1);

        // Clear saved state
        categoryStateHelper.clearCategory();

        // Refresh categories view
        categoryViewModel.getAllCategories().observe(this, categories -> {
            if (categories != null) categoryAdapter.setCategories(categories);
            updateCategoriesView(categories);
        });

        // Set Categories as active slot
        setActiveNavSlot(1);
    }

    private void showAddCategoryDialog() {
        CategoryDialog.show(this, category -> {
            categoryViewModel.insert(category);
            Toast.makeText(this, "Category created: " + category.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showAddMomentDialog() {
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
            Log.e(TAG, "Error deleting moment", e);
        }
    }

    private void restoreLastCategory() {
        // First check if we came from HomeActivity with a specific category
        int intentCategoryId = getIntent().getIntExtra("category_id", -1);
        if (intentCategoryId > 0) {
            categoryViewModel.getAllCategories().observe(this, categories -> {
                if (categories != null) {
                    for (Category cat : categories) {
                        if (cat.getId() == intentCategoryId) {
                            enterMomentsMode(cat);
                            break;
                        }
                    }
                }
            });
            return;
        }

        // Otherwise restore from SharedPreferences
        Category saved = categoryStateHelper.restoreCategory();
        if (saved != null) {
            enterMomentsMode(saved);
        }
    }

    @Override
    public void onBackPressed() {
        if (isInMomentsMode) {
            exitMomentsMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
