package com.example.usmentz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.adapter.CategoryHomeAdapter;
import com.example.usmentz.adapter.DateAdapter;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.helper.SuggestionHelper;
import com.example.usmentz.helper.SuggestionHelper.Suggestion;
import com.example.usmentz.viewmodel.CategoryViewModel;
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    // ViewModels
    private CategoryViewModel categoryViewModel;
    private DateViewModel dateViewModel;

    // Views
    private TextView tvDate, tvGreeting;
    private RecyclerView rvCategories, rvRecentActivity;
    private FloatingActionButton fabAdd;
    private ImageButton btnNotifications, btnProfile;
    private View suggestionCardContainer;
    private TextView tvSuggestionTitle, tvSuggestionText;

    // Expanding pill navbar
    private View navItemHome, navItemCategories, navItemCalendar;
    private View navHomeActive, navHomeInactive;
    private View navCategoriesActive, navCategoriesInactive;
    private View navCalendarActive, navCalendarInactive;
    private int activeNavSlot = 0; // Home is active by default

    // FAB scroll state
    private boolean isFabVisible = true;

    // Adapters
    private DateAdapter recentAdapter;
    private CategoryHomeAdapter categoryAdapter;

    // State
    private Handler timeHandler;
    private Runnable timeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().setBackgroundDrawableResource(android.R.color.white);

        try {
            initViews();
            setupViewModels();
            setupRecyclerViews();
            setupClickListeners();
            startLiveTime();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        tvDate = findViewById(R.id.tvDate);
        tvGreeting = findViewById(R.id.tvGreeting);

        rvCategories = findViewById(R.id.rvCategories);
        rvRecentActivity = findViewById(R.id.rvRecentActivity);

        btnNotifications = findViewById(R.id.btnNotifications);
        btnProfile = findViewById(R.id.btnProfile);

        // Expanding pill navbar
        navItemHome = findViewById(R.id.navItemHome);
        navItemCategories = findViewById(R.id.navItemCategories);
        navItemCalendar = findViewById(R.id.navItemCalendar);
        navHomeActive = findViewById(R.id.navHomeActive);
        navHomeInactive = findViewById(R.id.navHomeInactive);
        navCategoriesActive = findViewById(R.id.navCategoriesActive);
        navCategoriesInactive = findViewById(R.id.navCategoriesInactive);
        navCalendarActive = findViewById(R.id.navCalendarActive);
        navCalendarInactive = findViewById(R.id.navCalendarInactive);
        setActiveNavSlot(0); // Home active by default

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
    }

    private void setupViewModels() {
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);

        // Sync data from Firestore on login
        categoryViewModel.syncFromFirestore(null);
        dateViewModel.syncFromFirestore(null);

        // Observe all moments for recent activity + suggestions
        dateViewModel.getAllMoments().observe(this, moments -> {
            if (moments != null && recentAdapter != null) {
                recentAdapter.setDates(moments);
                updateSuggestion(moments);
            }
        });

        // Observe categories for adapter
        categoryViewModel.getAllCategories().observe(this, categories -> {
            if (categories != null) {
                categoryAdapter.setCategories(categories);
            }
        });
    }

    private void setupRecyclerViews() {
        // Categories horizontal carousel
        categoryAdapter = new CategoryHomeAdapter();
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        categoryAdapter.setOnCategoryClickListener(cat -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("category_id", cat.getId());
            startActivity(intent);
        });

        // Recent activity list (scrapbook cards)
        recentAdapter = new DateAdapter();
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(this));
        rvRecentActivity.setAdapter(recentAdapter);

        recentAdapter.setOnItemClickListener(dateLocation -> {
            Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
            intent.putExtra("date_location", dateLocation);
            startActivity(intent);
        });

        recentAdapter.setOnItemDeleteListener(dateLocation -> {
            dateViewModel.delete(dateLocation);
        });

        // FAB auto-hide on scroll
        rvRecentActivity.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && isFabVisible) {
                    isFabVisible = false;
                    fabAdd.animate().alpha(0f).setDuration(200)
                            .withEndAction(() -> fabAdd.setVisibility(View.GONE)).start();
                } else if (dy < 0 && !isFabVisible) {
                    isFabVisible = true;
                    fabAdd.setVisibility(View.VISIBLE);
                    fabAdd.animate().alpha(1f).setDuration(200).start();
                }
            }
        });
    }

    private void setupClickListeners() {
        navItemHome.setOnClickListener(v -> setActiveNavSlot(0));

        navItemCategories.setOnClickListener(v -> {
            setActiveNavSlot(1);
            startActivity(new Intent(this, MainActivity.class));
        });

        navItemCalendar.setOnClickListener(v -> {
            setActiveNavSlot(2);
            startActivity(new Intent(this, CalendarActivity.class));
        });

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
            });
        }

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showAddMomentDialog());
        }
    }

    private void setActiveNavSlot(int slotIndex) {
        activeNavSlot = slotIndex;
        navHomeActive.setVisibility(slotIndex == 0 ? View.VISIBLE : View.GONE);
        navHomeInactive.setVisibility(slotIndex == 0 ? View.GONE : View.VISIBLE);
        navCategoriesActive.setVisibility(slotIndex == 1 ? View.VISIBLE : View.GONE);
        navCategoriesInactive.setVisibility(slotIndex == 1 ? View.GONE : View.VISIBLE);
        navCalendarActive.setVisibility(slotIndex == 2 ? View.VISIBLE : View.GONE);
        navCalendarInactive.setVisibility(slotIndex == 2 ? View.GONE : View.VISIBLE);
    }

    private void updateSuggestion(List<DateLocation> moments) {
        if (suggestionCardContainer == null || tvSuggestionTitle == null || tvSuggestionText == null) return;

        Suggestion suggestion = SuggestionHelper.generateSuggestion(moments);
        if (suggestion != null) {
            tvSuggestionTitle.setText(suggestion.title);
            tvSuggestionText.setText(suggestion.description);
            suggestionCardContainer.setVisibility(View.VISIBLE);
        }
    }

    private void startLiveTime() {
        timeHandler = new Handler();
        timeRunnable = () -> {
            updateDateTime();
            timeHandler.postDelayed(this::updateDateTime, 60000);
        };
        timeHandler.post(timeRunnable);
    }

    private void updateDateTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        SimpleDateFormat dateFormatNew = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        Date now = new Date();

        if (tvDate != null) {
            tvDate.setText(dateFormatNew.format(now));
        }
        if (tvGreeting != null) {
            tvGreeting.setText(timeFormat.format(now));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDateTime();
        if (timeHandler != null && timeRunnable != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
        startLiveTime();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timeHandler != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
    }

    private void showAddMomentDialog() {
        AddMomentDialog dialog = AddMomentDialog.newInstance(moment -> {
            // Observers will handle refresh
        });
        dialog.show(getSupportFragmentManager(), "AddMomentDialog");
    }
}
