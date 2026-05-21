package com.example.usmentz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.usmentz.adapter.CategoryHomeAdapter;
import com.example.usmentz.adapter.DateAdapter;
import com.example.usmentz.category.Category;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.helper.CapsuleNavbarHelper;
import com.example.usmentz.helper.SwipeBackHelper;
import com.example.usmentz.helper.SuggestionHelper;
import com.example.usmentz.helper.SuggestionHelper.Suggestion;
import com.example.usmentz.viewmodel.CategoryViewModel;
import com.example.usmentz.viewmodel.DateViewModel;
import com.example.usmentz.databinding.ActivityHomeBinding;
import com.example.usmentz.databinding.BottomNavCapsuleBinding;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    // ViewModels
    private CategoryViewModel categoryViewModel;
    private DateViewModel dateViewModel;

    // Binding
    private ActivityHomeBinding binding;

    // Navigation
    private LinearLayout navContainer;
    private LinearLayout navItemHome, navItemCategories, navItemCalendar, navItemFavorites, navItemSettings;
    private ImageView navIconHome, navIconCategories, navIconCalendar, navIconFavorites, navIconSettings;
    private TextView navLabelHome, navLabelCategories, navLabelCalendar, navLabelFavorites, navLabelSettings;

    // Adapters
    private DateAdapter recentAdapter;

    // Data
    private List<Category> allCategories = new ArrayList<>();
    private List<DateLocation> allMoments = new ArrayList<>();

    // State
    private Handler timeHandler;
    private Runnable timeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setBackgroundDrawableResource(android.R.color.white);

        try {
            new SwipeBackHelper(this);
            initViews();
            setupViewModels();
            setupRecyclerViews();
            setupClickListeners();
            startLiveTime();
            startEntryAnimations();
            
            // Show loading initially
            binding.progressLoading.setVisibility(View.VISIBLE);
            binding.emptyStateHome.setVisibility(View.GONE);
            binding.categoriesGrid.setVisibility(View.GONE);
            binding.rvRecentActivity.setVisibility(View.GONE);
            binding.streakCard.setVisibility(View.GONE);
            
            // Timeout fallback: if nothing loads in 5s, show empty state
            new Handler().postDelayed(() -> {
                if (binding.progressLoading.getVisibility() == View.VISIBLE) {
                    binding.progressLoading.setVisibility(View.GONE);
                    updateEmptyState();
                }
            }, 5000);
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        // Navbar
        BottomNavCapsuleBinding navBinding = binding.capsuleNavbar;
        navContainer = navBinding.navContainer;
        navItemHome = navBinding.navItemHome;
        navItemCategories = navBinding.navItemCategories;
        navItemCalendar = navBinding.navItemCalendar;
        navItemFavorites = navBinding.navItemFavorites;
        navItemSettings = navBinding.navItemSettings;
        navIconHome = navBinding.navIconHome;
        navIconCategories = navBinding.navIconCategories;
        navIconCalendar = navBinding.navIconCalendar;
        navIconFavorites = navBinding.navIconFavorites;
        navIconSettings = navBinding.navIconSettings;
        navLabelHome = navBinding.navLabelHome;
        navLabelCategories = navBinding.navLabelCategories;
        navLabelCalendar = navBinding.navLabelCalendar;
        navLabelFavorites = navBinding.navLabelFavorites;
        navLabelSettings = navBinding.navLabelSettings;

        CapsuleNavbarHelper.setup(this, navContainer,
            navItemHome, navItemCategories, navItemCalendar, navItemFavorites, navItemSettings,
            navIconHome, navIconCategories, navIconCalendar, navIconFavorites, navIconSettings,
            navLabelHome, navLabelCategories, navLabelCalendar, navLabelFavorites, navLabelSettings,
            0);

        binding.fabAdd.setOnClickListener(v -> showAddMomentDialog());
    }

    private void setupViewModels() {
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);

        categoryViewModel.syncFromFirestore(null);
        dateViewModel.syncFromFirestore(null);

        dateViewModel.getAllMoments().observe(this, moments -> {
            binding.progressLoading.setVisibility(View.GONE);
            if (moments != null) {
                allMoments = moments;
                recentAdapter.setDates(moments);
                updateStreak(moments);
                updateWeekDots(moments);
                updateCategoriesGrid(allCategories, moments);
            }
            updateEmptyState();
        });

        categoryViewModel.getAllCategories().observe(this, categories -> {
            binding.progressLoading.setVisibility(View.GONE);
            if (categories != null) {
                allCategories = categories;
                updateCategoriesGrid(categories, allMoments);
            }
            updateEmptyState();
        });
    }

    private void setupRecyclerViews() {
        recentAdapter = new DateAdapter();
        binding.rvRecentActivity.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentActivity.setAdapter(recentAdapter);

        recentAdapter.setOnItemClickListener(dateLocation -> {
            Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
            intent.putExtra("date_location", dateLocation);
            startActivity(intent);
        });

        recentAdapter.setOnItemDeleteListener(this::showDeleteConfirmation);
    }

    private void setupClickListeners() {
        // Quick Actions
        binding.actionPhoto.setOnClickListener(v -> showAddMomentDialog());
        binding.actionNote.setOnClickListener(v -> showAddMomentDialog());
        binding.actionVoice.setOnClickListener(v -> showAddMomentDialog());
        binding.actionLocation.setOnClickListener(v -> showAddMomentDialog());

        binding.tvSeeAllCategories.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        
        binding.btnRefreshHome.setOnClickListener(v -> {
            binding.progressLoading.setVisibility(View.VISIBLE);
            binding.emptyStateHome.setVisibility(View.GONE);
            categoryViewModel.syncFromFirestore(null);
            dateViewModel.syncFromFirestore(null);
        });
    }

    private void updateStreak(List<DateLocation> moments) {
        if (moments == null || moments.isEmpty()) {
            binding.tvStreakCount.setText("0");
            binding.tvBestStreak.setText("Best: 0");
            return;
        }

        Set<String> uniqueDays = new HashSet<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (DateLocation m : moments) {
            if (m.getDate() != null) uniqueDays.add(fmt.format(m.getDate()));
        }

        List<String> sortedDays = new ArrayList<>(uniqueDays);
        sortedDays.sort((a, b) -> b.compareTo(a)); // newest first

        int streak = 0;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        // Check if today has a moment, if not start from yesterday
        String todayStr = fmt.format(cal.getTime());
        if (!sortedDays.contains(todayStr)) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        while (sortedDays.contains(fmt.format(cal.getTime()))) {
            streak++;
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        binding.tvStreakCount.setText(String.valueOf(streak));
        
        // Best streak (simplified: total unique days / 7 weeks max)
        int bestStreak = Math.max(streak, uniqueDays.size() > 7 ? 7 : uniqueDays.size());
        binding.tvBestStreak.setText("Best: " + bestStreak);
    }

    private void updateWeekDots(List<DateLocation> moments) {
        if (moments == null) return;

        Set<String> momentDays = new HashSet<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (DateLocation m : moments) {
            if (m.getDate() != null) momentDays.add(fmt.format(m.getDate()));
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        
        TextView[] dots = {
            binding.dotMon, binding.dotTue, binding.dotWed,
            binding.dotThu, binding.dotFri, binding.dotSat, binding.dotSun
        };

        int todayDow = cal.get(Calendar.DAY_OF_WEEK);
        // Adjust for Monday start
        int todayIndex = (todayDow == Calendar.SUNDAY) ? 6 : todayDow - 2;

        for (int i = 0; i < 7; i++) {
            String dayStr = fmt.format(cal.getTime());
            boolean hasMoment = momentDays.contains(dayStr);
            boolean isToday = (i == todayIndex);

            if (hasMoment) {
                dots[i].setBackgroundResource(R.drawable.bg_streak_dot_done);
                dots[i].setTextColor(getResources().getColor(R.color.text_primary, null));
            } else if (isToday) {
                dots[i].setBackgroundResource(R.drawable.bg_streak_dot_today);
                dots[i].setTextColor(getResources().getColor(R.color.black_pure, null));
            } else {
                dots[i].setBackgroundResource(R.drawable.bg_streak_dot_empty);
                dots[i].setTextColor(getResources().getColor(R.color.text_hint, null));
            }

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void updateCategoriesGrid(List<Category> categories, List<DateLocation> moments) {
        MaterialCardView[] cards = {
            binding.catCard1, binding.catCard2, binding.catCard3, binding.catCard4
        };
        ImageView[] icons = {
            binding.catIcon1, binding.catIcon2, binding.catIcon3, binding.catIcon4
        };
        TextView[] labels = {
            binding.catLabel1, binding.catLabel2, binding.catLabel3, binding.catLabel4
        };
        TextView[] counts = {
            binding.catCount1, binding.catCount2, binding.catCount3, binding.catCount4
        };
        TextView[] subs = {
            binding.catSub1, binding.catSub2, binding.catSub3, binding.catSub4
        };

        int[] iconBackgrounds = {
            R.drawable.bg_icon_orange, R.drawable.bg_icon_purple,
            R.drawable.bg_icon_teal, R.drawable.bg_icon_pink
        };
        int[] iconsRes = {
            R.drawable.ic_heart_outline, R.drawable.ic_calendar_outline,
            R.drawable.ic_folder_outline, R.drawable.ic_info_outline
        };

        // Sort categories by moment count (descending)
        List<Category> sorted = new ArrayList<>(categories != null ? categories : new ArrayList<Category>());
        sorted.sort((a, b) -> {
            long countA = moments != null ? moments.stream().filter(m -> m.getCategoryId() == a.getId()).count() : 0;
            long countB = moments != null ? moments.stream().filter(m -> m.getCategoryId() == b.getId()).count() : 0;
            return Long.compare(countB, countA);
        });

        for (int i = 0; i < 4; i++) {
            if (i < sorted.size()) {
                Category cat = sorted.get(i);
                cards[i].setVisibility(View.VISIBLE);
                
                icons[i].setBackgroundResource(iconBackgrounds[i % iconBackgrounds.length]);
                icons[i].setImageResource(iconsRes[i % iconsRes.length]);
                
                labels[i].setText(cat.getName());
                
                long count = moments != null ? moments.stream()
                    .filter(m -> m.getCategoryId() == cat.getId())
                    .count() : 0;
                counts[i].setText(String.valueOf(count));
                subs[i].setText("moments");

                int finalCatId = cat.getId();
                cards[i].setOnClickListener(v -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("category_id", finalCatId);
                    startActivity(intent);
                });
            } else {
                cards[i].setVisibility(View.GONE);
            }
        }
    }

    private void updateEmptyState() {
        boolean hasData = !allCategories.isEmpty() || !allMoments.isEmpty();
        
        binding.progressLoading.setVisibility(View.GONE);
        binding.emptyStateHome.setVisibility(hasData ? View.GONE : View.VISIBLE);
        binding.streakCard.setVisibility(hasData ? View.VISIBLE : View.GONE);
        binding.quickActionsRow.setVisibility(hasData ? View.VISIBLE : View.GONE);
        binding.categoriesGrid.setVisibility(hasData ? View.VISIBLE : View.GONE);
        binding.rvRecentActivity.setVisibility(!allMoments.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void startEntryAnimations() {
        View[] views = {
            binding.tvGreeting, binding.tvTitle, binding.tvDate,
            binding.streakCard, binding.quickActionsRow, binding.categoriesGrid, binding.rvRecentActivity
        };
        for (int i = 0; i < views.length; i++) {
            View v = views[i];
            if (v != null && v.getVisibility() == View.VISIBLE) {
                v.setAlpha(0f);
                v.setTranslationY(16f);
                v.animate().alpha(1f).translationY(0f)
                    .setStartDelay(80 + i * 60)
                    .setDuration(350)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            }
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        Date now = new Date();
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        binding.tvDate.setText(dateFormat.format(now));
        
        String greeting;
        if (hour < 12) greeting = "GOOD MORNING";
        else if (hour < 17) greeting = "GOOD AFTERNOON";
        else if (hour < 21) greeting = "GOOD EVENING";
        else greeting = "GOOD NIGHT";
        binding.tvGreeting.setText(greeting);
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
        if (timeHandler != null) timeHandler.removeCallbacks(timeRunnable);
    }

    private void showAddMomentDialog() {
        AddMomentDialog dialog = AddMomentDialog.newInstance(moment -> {});
        dialog.show(getSupportFragmentManager(), "AddMomentDialog");
    }

    private void showDeleteConfirmation(DateLocation dateLocation) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Moment")
                .setMessage("Are you sure you want to delete \"" + dateLocation.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> dateViewModel.delete(dateLocation))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
