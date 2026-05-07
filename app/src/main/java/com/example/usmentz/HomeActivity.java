package com.example.usmentz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.adapter.CategoryPortraitAdapter;
import com.example.usmentz.category.Category;
import com.example.usmentz.category.CategoryAdapter;
import com.example.usmentz.viewmodel.CategoryViewModel;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.viewmodel.DateViewModel;
import com.example.usmentz.adapter.DateAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    
    // ViewModels
    private CategoryViewModel categoryViewModel;
    private DateViewModel dateViewModel;
    
    // Views
    private TextView tvTime, tvDate, tvGreeting;
    private TextView tvMomentsCount, tvCategoriesCount, tvAvgRating;
    private TextView tvUpcomingCount, tvFavoritesCount;
    private RecyclerView rvCategories, rvRecentActivity;
    private LinearLayout navHome, navCategories, navExpenses, navCalendar;
    private View floatingNavbarContainer;
    private FloatingActionButton fabAdd;
    private ImageButton btnNotifications;
    private TextView btnViewAllCategories;
    private View cardQuickAdd, cardUpcoming, cardFavorites;
    
    // Adapters
    private CategoryAdapter categoryAdapter;
    private CategoryPortraitAdapter categoryPortraitAdapter;
    private DateAdapter recentAdapter;
    
    // State
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "UsmentzPrefs";
    private Handler timeHandler;
    private Runnable timeRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(Window.FEATURE_NO_TITLE, Window.FEATURE_NO_TITLE);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        
        try {
            sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            calendar = Calendar.getInstance();
            dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            
            initViews();
            setupViewModels();
            setupRecyclerViews();
            setupClickListeners();
            updateDateTime();
            startLiveTime();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void initViews() {
        tvTime = findViewById(R.id.tvTime);
        tvDate = findViewById(R.id.tvDate);
        tvGreeting = findViewById(R.id.tvGreeting);
        
        tvMomentsCount = findViewById(R.id.tvMomentsCount);
        tvCategoriesCount = findViewById(R.id.tvCategoriesCount);
        tvAvgRating = findViewById(R.id.tvAvgRating);
        
        tvUpcomingCount = findViewById(R.id.tvUpcomingCount);
        tvFavoritesCount = findViewById(R.id.tvFavoritesCount);
        
        rvCategories = findViewById(R.id.rvCategories);
        rvRecentActivity = findViewById(R.id.rvRecentActivity);
        
        navHome = findViewById(R.id.navHome);
        navCategories = findViewById(R.id.navCategories);
        navExpenses = findViewById(R.id.navExpenses);
        navCalendar = findViewById(R.id.navCalendar);
        
        floatingNavbarContainer = findViewById(R.id.floatingNavbarContainer);
        fabAdd = findViewById(R.id.fabAdd);
        
        btnNotifications = findViewById(R.id.btnNotifications);
        btnViewAllCategories = findViewById(R.id.btnViewAllCategories);
        
        cardQuickAdd = findViewById(R.id.cardQuickAdd);
        cardUpcoming = findViewById(R.id.cardUpcoming);
        cardFavorites = findViewById(R.id.cardFavorites);
    }
    
    private void setupViewModels() {
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);
        
        categoryViewModel.getAllCategories().observe(this, categories -> {
            if (categories != null) {
                updateCategoriesCount(categories.size());
                if (categoryAdapter != null) {
                    categoryAdapter.setCategories(categories);
                }
            }
        });
        
        dateViewModel.getAllMoments().observe(this, dates -> {
            if (dates != null) {
                updateMomentsCount(dates.size());
                if (recentAdapter != null) {
                    recentAdapter.setDates(dates);
                }
            }
        });
    }
    
    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter();
        rvCategories.setLayoutManager(new GridLayoutManager(this, 2));
        rvCategories.setAdapter(categoryAdapter);
        
        recentAdapter = new DateAdapter();
        rvRecentActivity.setLayoutManager(new GridLayoutManager(this, 1));
        rvRecentActivity.setAdapter(recentAdapter);
    }
    
    private void setupClickListeners() {
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // Already on home
            });
        }
        
        if (navCategories != null) {
            navCategories.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class));
            });
        }
        
        if (navExpenses != null) {
            navExpenses.setOnClickListener(v -> {
                // Go to detail/expenses
                Toast.makeText(this, "Expenses", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (navCalendar != null) {
            navCalendar.setOnClickListener(v -> {
                startActivity(new Intent(this, CalendarActivity.class));
            });
        }
        
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                showAddMomentDialog();
            });
        }
        
        if (cardQuickAdd != null) {
            cardQuickAdd.setOnClickListener(v -> {
                showAddMomentDialog();
            });
        }
        
        if (cardUpcoming != null) {
            cardUpcoming.setOnClickListener(v -> {
                startActivity(new Intent(this, CalendarActivity.class));
            });
        }
        
        if (cardFavorites != null) {
            cardFavorites.setOnClickListener(v -> {
                startActivity(new Intent(this, FavoritesActivity.class));
            });
        }
        
        if (btnViewAllCategories != null) {
            btnViewAllCategories.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class));
            });
        }
    }
    
    private void startLiveTime() {
        timeHandler = new Handler();
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                updateDateTime();
                timeHandler.postDelayed(this, 60000); // Update every minute
            }
        };
        timeHandler.post(timeRunnable);
    }
    
    private void updateDateTime() {
        if (tvTime != null) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            tvTime.setText(timeFormat.format(new Date()));
        }
        
        if (tvDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
            tvDate.setText(dateFormat.format(calendar.getTime()));
        }
        
        if (tvGreeting != null) {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            String greeting;
            if (hour < 12) greeting = "Good morning";
            else if (hour < 17) greeting = "Good afternoon";
            else greeting = "Good evening";
            tvGreeting.setText(greeting);
        }
    }
    
    private void updateMomentsCount(int count) {
        if (tvMomentsCount != null) {
            tvMomentsCount.setText(String.valueOf(count));
        }
    }
    
    private void updateCategoriesCount(int count) {
        if (tvCategoriesCount != null) {
            tvCategoriesCount.setText(String.valueOf(count));
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateDateTime();
        if (timeHandler != null) {
            timeHandler.post(timeRunnable);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (timeHandler != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
    }
    
    private void showAddMomentDialog() {
        AddMomentDialog dialog = AddMomentDialog.newInstance(() -> {
            dateViewModel.getAllMoments().observe(this, dates -> {
                if (dates != null) updateMomentsCount(dates.size());
            });
        });
        dialog.show(getSupportFragmentManager(), "AddMomentDialog");
    }
}