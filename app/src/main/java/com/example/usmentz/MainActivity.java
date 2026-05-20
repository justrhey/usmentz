package com.example.usmentz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.example.usmentz.helper.EmptyStateHelper;
import com.example.usmentz.helper.NavbarAutoHideHelper;
import com.example.usmentz.helper.ViewSwitcherHelper;
import com.example.usmentz.viewmodel.CategoryViewModel;
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DateViewModel dateViewModel;
    private CategoryViewModel categoryViewModel;
    private CategoryAdapter categoryAdapter;
    private DateAdapter dateAdapter;

    private static final String TAG = "MainActivity";
    private static final long HIDE_DELAY_MS = 4000;

    // Helpers
    private ViewSwitcherHelper viewSwitcher;
    private EmptyStateHelper emptyStateHelper;
    private NavbarAutoHideHelper navbarHelper;
    private CategoryStateHelper categoryStateHelper;

    // Views
    private FloatingActionButton fabAdd;
    private MaterialToolbar toolbar;
    private AppBarLayout appBarLayout;
    private TextView tvToolbarTitle;
    private RecyclerView categoriesRecyclerView;
    private RecyclerView momentsRecyclerView;

    private LinearLayout navHome, navCalendar, navReviews, navFavorites, navCategories;
    private LinearLayout navDates, navExpenses;
    private View floatingNavbarContainer;
    private View oldNavbarContainer;
    private View momentsHeader;

    private final int[] emptyStateDrawables = { R.drawable.nocat };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(Window.FEATURE_NO_TITLE, Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_categories);
        getWindow().setBackgroundDrawableResource(android.R.color.white);

        try {
            initViews();
            initHelpers();
            setupToolbar();
            setupViewModels();
            setupRecyclerViews();
            setupClickListeners();
            setupBottomNavigation();
            restoreLastCategory();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        toolbar               = findViewById(R.id.toolbar);
        appBarLayout           = findViewById(R.id.appBarLayout);
        categoriesRecyclerView= findViewById(R.id.categoriesRecyclerView);
        momentsRecyclerView   = findViewById(R.id.momentsRecyclerView);
        fabAdd                = findViewById(R.id.fabAdd);
        LinearLayout emptyStateLayout = findViewById(R.id.emptyStateLayout);
        Button btnAddCategory = findViewById(R.id.btnAddCategory);

        navHome       = findViewById(R.id.navHome);
        navCalendar   = findViewById(R.id.navCalendar);
        navReviews    = findViewById(R.id.navReviews);
        navFavorites  = findViewById(R.id.navFavorites);
        navCategories = findViewById(R.id.navCategories);
        navDates      = findViewById(R.id.navDates);
        navExpenses   = findViewById(R.id.navExpenses);
        floatingNavbarContainer = findViewById(R.id.floatingNavbarContainer);
        oldNavbarContainer      = findViewById(R.id.oldNavbarContainer);
        momentsHeader           = findViewById(R.id.momentsHeader);
        TextView tvCategoryTitle = findViewById(R.id.tvCategoryTitle);

        if (toolbar != null) {
            tvToolbarTitle = toolbar.findViewById(R.id.tvToolbarTitle);
        }

        // Default visibility
        if (categoriesRecyclerView != null) categoriesRecyclerView.setVisibility(View.VISIBLE);
        if (momentsRecyclerView != null) momentsRecyclerView.setVisibility(View.GONE);
        if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);
        if (fabAdd != null) fabAdd.setVisibility(View.VISIBLE);
        if (btnAddCategory != null) btnAddCategory.setVisibility(View.VISIBLE);
        if (oldNavbarContainer != null) oldNavbarContainer.setVisibility(View.VISIBLE);
        if (floatingNavbarContainer != null) floatingNavbarContainer.setVisibility(View.GONE);

        // Store references for helpers via tags or keep as fields
        // We'll pass them directly to helpers
    }

    private void initHelpers() {
        LinearLayout emptyStateLayout = findViewById(R.id.emptyStateLayout);
        Button btnAddCategory = findViewById(R.id.btnAddCategory);
        TextView tvCategoryTitle = findViewById(R.id.tvCategoryTitle);

        emptyStateHelper = new EmptyStateHelper(emptyStateLayout, emptyStateDrawables);
        navbarHelper = new NavbarAutoHideHelper(floatingNavbarContainer, momentsHeader, HIDE_DELAY_MS);
        categoryStateHelper = new CategoryStateHelper(this);

        viewSwitcher = new ViewSwitcherHelper(
                categoriesRecyclerView,
                momentsRecyclerView,
                btnAddCategory,
                momentsHeader,
                tvCategoryTitle,
                floatingNavbarContainer,
                oldNavbarContainer,
                emptyStateHelper,
                navbarHelper,
                categoryStateHelper
        );

        viewSwitcher.setModeChangeListener(new ViewSwitcherHelper.OnModeChangeListener() {
            @Override
            public void onEnterMomentsMode(Category category) {
                if (dateViewModel != null && category.getId() > 0) {
                    dateViewModel.setCurrentCategory(category.getId());
                }
            }

            @Override
            public void onExitMomentsMode() {
                if (dateViewModel != null) {
                    dateViewModel.setCurrentCategory(-1);
                }
            }
        });
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (viewSwitcher.isInMomentsMode()) viewSwitcher.exitMomentsMode();
                else finish();
            });
        }
    }

    private void setupViewModels() {
        dateViewModel     = new ViewModelProvider(this).get(DateViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
    }

    private void setupRecyclerViews() {
        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        categoriesRecyclerView.setHasFixedSize(true);
        categoriesRecyclerView.setItemAnimator(null);
        categoryAdapter = new CategoryAdapter();
        categoriesRecyclerView.setAdapter(categoryAdapter);

        momentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        momentsRecyclerView.setHasFixedSize(true);
        momentsRecyclerView.setItemAnimator(null);
        dateAdapter = new DateAdapter();
        momentsRecyclerView.setAdapter(dateAdapter);

        // Observe categories
        categoryViewModel.getAllCategories().observe(this, categories -> {
            if (categories != null) categoryAdapter.setCategories(categories);
            if (viewSwitcher.isInMomentsMode()) return;
            if (categories == null || categories.isEmpty()) {
                categoriesRecyclerView.setVisibility(View.GONE);
                emptyStateHelper.show("No categories yet", "Tap + to create your first category");
                findViewById(R.id.btnAddCategory).setVisibility(View.VISIBLE);
            } else {
                categoriesRecyclerView.setVisibility(View.VISIBLE);
                emptyStateHelper.hide();
            }
        });

        // Observe moments
        dateViewModel.getMoments().observe(this, moments -> {
            if (!viewSwitcher.isInMomentsMode() || viewSwitcher.getCurrentCategory() == null) return;
            if (dateAdapter != null) dateAdapter.setDates(moments);
            updateMomentsView(moments);
        });

        categoryAdapter.setOnCategoryClickListener(viewSwitcher::enterMomentsMode);

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

    private void setupClickListeners() {
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                if (viewSwitcher.isInMomentsMode() && viewSwitcher.getCurrentCategory() != null) {
                    showAddMomentDialog();
                } else {
                    showAddCategoryDialog();
                }
            });
        }

        Button btnAddCategory = findViewById(R.id.btnAddCategory);
        if (btnAddCategory != null) {
            btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        }

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> viewSwitcher.exitMomentsMode());
        }
    }

    private void setupBottomNavigation() {
        setNavClick(navHome, HomeActivity.class);
        setNavClick(navCalendar, CalendarActivity.class);
        setNavClick(navFavorites, FavoritesActivity.class);

        if (navCategories != null) {
            navCategories.setOnClickListener(v -> viewSwitcher.exitMomentsMode());
        }
    }

    private void setNavClick(LinearLayout nav, Class<?> target) {
        if (nav != null) {
            nav.setOnClickListener(v -> {
                Intent intent = new Intent(this, target);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }
    }

    private void showAddCategoryDialog() {
        CategoryDialog.show(this, category -> {
            categoryViewModel.insert(category);
            Toast.makeText(this, "Category created: " + category.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showAddMomentDialog() {
        AddMomentDialog dialog = AddMomentDialog.newInstance(moment -> {
            Toast.makeText(this, "Added to " + viewSwitcher.getCurrentCategory().getName(), Toast.LENGTH_SHORT).show();
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
        Category saved = categoryStateHelper.restoreCategory();
        if (saved != null) {
            viewSwitcher.enterMomentsMode(saved);
        }
    }

    private void updateMomentsView(List<DateLocation> moments) {
        if (moments == null || moments.isEmpty()) {
            emptyStateHelper.show("No moments yet", "Tap + to add your first moment");
            momentsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateHelper.hide();
            momentsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewSwitcher.isInMomentsMode()) navbarHelper.startHideTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        navbarHelper.cleanup();
    }
}
