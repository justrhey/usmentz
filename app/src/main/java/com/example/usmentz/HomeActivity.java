package com.example.usmentz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.viewmodel.CategoryViewModel;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.viewmodel.DateViewModel;
import com.example.usmentz.viewmodel.ExpenseViewModel;
import com.example.usmentz.adapter.DateAdapter;
import com.example.usmentz.adapter.CategoryHomeAdapter;
import com.example.usmentz.fina.Expense;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.usmentz.widget.DonutChartView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    // ViewModels
    private CategoryViewModel categoryViewModel;
    private DateViewModel dateViewModel;
    private ExpenseViewModel expenseViewModel;

    // Views
    private TextView tvTime, tvDate, tvGreeting;
    private TextView tvTotalBalance, tvIncome, tvExpenses, tvSavings;
    private DonutChartView donutBudget;
    private TextView tvBudgetPercent, tvThisMonth;
    private TextView tvUpcomingCount, tvFavoritesCount, tvChartPeak;
    private BarChart barChart;
    private RecyclerView rvRecentActivity;
    private View navHome, navCategories, navReviews, navCalendar;
    private View floatingNavbarContainer;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabAdd;
    private ImageButton btnNotifications, btnProfile;
    private View cardQuickAdd, cardUpcoming, cardFavorites;
    private com.google.android.material.appbar.MaterialToolbar toolbar;

    // FAB scroll state
    private boolean isFabVisible = true;

    // Adapters
    private DateAdapter recentAdapter;
    private CategoryHomeAdapter categoryAdapter;

    // State
    private Handler timeHandler;
    private Runnable timeRunnable;

    // Live data holders
    private double totalExpenses = 0;
    private double totalFunds = 0;
    private double totalSavings = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().setBackgroundDrawableResource(android.R.color.white);

        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat dateFmt = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            initViews();
            setupViewModels();
            setupRecyclerViews();
            setupClickListeners();
            setupChart();
            updateDateTime(cal);
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

        // Balance card
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpenses = findViewById(R.id.tvExpenses);
        tvSavings = findViewById(R.id.tvSavings);

        // Stat cards
        donutBudget = findViewById(R.id.donutBudget);
        tvBudgetPercent = findViewById(R.id.tvBudgetPercent);
        tvThisMonth = findViewById(R.id.tvThisMonth);
        tvChartPeak = findViewById(R.id.tvChartPeak);

        // Chart
        barChart = findViewById(R.id.barChart);

        // Quick actions
        tvUpcomingCount = findViewById(R.id.tvUpcomingCount);
        tvFavoritesCount = findViewById(R.id.tvFavoritesCount);

        rvRecentActivity = findViewById(R.id.rvRecentActivity);

        btnNotifications = findViewById(R.id.btnNotifications);
        btnProfile = findViewById(R.id.btnProfile);

        cardQuickAdd = findViewById(R.id.cardAddExpense);
        cardUpcoming = findViewById(R.id.cardUpcoming);
        cardFavorites = findViewById(R.id.cardFavorites);

        navHome = findViewById(R.id.navHome);
        navCategories = findViewById(R.id.navCategories);
        navReviews = findViewById(R.id.navReviews);
        navCalendar = findViewById(R.id.navCalendar);
        floatingNavbarContainer = findViewById(R.id.floatingNavbarContainer);
        fabAdd = findViewById(R.id.fabAdd);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupViewModels() {
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        // Sync data from Firestore on login (Room stays source of truth, Firestore adds cloud backup)
        // This ensures data persists across app reinstalls
        categoryViewModel.syncFromFirestore(null);
        dateViewModel.syncFromFirestore(null);
        expenseViewModel.syncFromFirestore(null);

        // Observe all moments for recent activity
        dateViewModel.getAllMoments().observe(this, dates -> {
            if (dates != null && recentAdapter != null) {
                recentAdapter.setDates(dates);

                // Calculate upcoming count (future dates)
                int upcomingCount = 0;
                Calendar today = Calendar.getInstance();
                for (DateLocation date : dates) {
                    if (date.getDate() != null) {
                        Calendar dateCal = Calendar.getInstance();
                        dateCal.setTime(date.getDate());
                        if (dateCal.after(today)) {
                            upcomingCount++;
                        }
                    }
                }
                if (tvUpcomingCount != null) {
                    tvUpcomingCount.setText(upcomingCount + " scheduled");
                }
            }
        });

        // Observe total expenses
        expenseViewModel.getTotalSpent().observe(this, total -> {
            totalExpenses = total != null ? total : 0;
            updateBalanceCard();
        });

        // Observe funds (type = funds)
        expenseViewModel.getTotalByType(Expense.TYPE_FUNDS).observe(this, total -> {
            totalFunds = total != null ? total : 0;
            updateBalanceCard();
        });

        // Observe savings (type = savings)
        expenseViewModel.getTotalByType(Expense.TYPE_SAVINGS).observe(this, total -> {
            totalSavings = total != null ? total : 0;
            updateBalanceCard();
        });

        // Observe all expenses for chart
        expenseViewModel.getAllExpenses().observe(this, expenses -> {
            if (expenses != null) {
                updateChart(expenses);
                updateThisMonth(expenses);
            }
        });
    }

    private void updateBalanceCard() {
        double balance = totalFunds - totalExpenses + totalSavings;

        if (tvTotalBalance != null) {
            tvTotalBalance.setText(formatCurrency(balance));
        }
        if (tvIncome != null) {
            tvIncome.setText(formatCurrency(totalFunds));
        }
        if (tvExpenses != null) {
            tvExpenses.setText(formatCurrency(totalExpenses));
        }
        if (tvSavings != null) {
            tvSavings.setText(formatCurrency(totalSavings));
        }

        // Calculate budget percentage
        float budgetFraction = totalFunds > 0 ? (float) (totalExpenses / totalFunds) : 0f;
        int percent = (int) (budgetFraction * 100);
        if (tvBudgetPercent != null) {
            tvBudgetPercent.setText(percent + "%");
        }
        if (donutBudget != null) {
            donutBudget.setProgress(budgetFraction);
        }
    }

    private void updateThisMonth(List<Expense> expenses) {
        double monthlyTotal = 0;
        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH);
        int currentYear = now.get(Calendar.YEAR);

        for (Expense expense : expenses) {
            if (expense.getType().equals(Expense.TYPE_EXPENSES)) {
                Calendar expCal = Calendar.getInstance();
                expCal.setTimeInMillis(expense.getCreatedAt());
                if (expCal.get(Calendar.MONTH) == currentMonth && expCal.get(Calendar.YEAR) == currentYear) {
                    monthlyTotal += expense.getAmount();
                }
            }
        }

        if (tvThisMonth != null) {
            tvThisMonth.setText(formatShortCurrency(monthlyTotal));
        }
    }

    private void updateChart(List<Expense> expenses) {
        if (barChart == null) return;

        // Always draw chart, even with zero data
        double[] dailyTotals = new double[7];
        String[] dayLabels = new String[7];
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        for (int i = 6; i >= 0; i--) {
            Calendar dayCal = Calendar.getInstance();
            dayCal.add(Calendar.DAY_OF_YEAR, -i);
            dayLabels[6 - i] = dayFormat.format(dayCal.getTime());
        }

        // Sum expenses per day
        if (expenses != null) {
            for (Expense expense : expenses) {
                if (expense.getType().equals(Expense.TYPE_EXPENSES)) {
                    long expenseTime = expense.getCreatedAt();
                    if (expenseTime <= 0) continue;

                    long today = System.currentTimeMillis();
                    long sevenDaysAgo = today - (6L * 24 * 60 * 60 * 1000);

                    if (expenseTime < sevenDaysAgo) continue;
                    // Calculate days ago using time difference (fixed calculation)
                    Calendar expCal = Calendar.getInstance();
                    expCal.setTimeInMillis(expenseTime);
                    // Normalize both to start of day for accurate day calculation
                    expCal.set(Calendar.HOUR_OF_DAY, 0);
                    expCal.set(Calendar.MINUTE, 0);
                    expCal.set(Calendar.SECOND, 0);
                    expCal.set(Calendar.MILLISECOND, 0);

                    Calendar todayCal = Calendar.getInstance();
                    todayCal.set(Calendar.HOUR_OF_DAY, 0);
                    todayCal.set(Calendar.MINUTE, 0);
                    todayCal.set(Calendar.SECOND, 0);
                    todayCal.set(Calendar.MILLISECOND, 0);

                    long diffInMillis = todayCal.getTimeInMillis() - expCal.getTimeInMillis();
                    int daysAgo = (int) (diffInMillis / (24 * 60 * 60 * 1000));

                    if (daysAgo >= 0 && daysAgo < 7) {
                        dailyTotals[6 - daysAgo] += expense.getAmount();
                    }
                }
            }
        }

        // Find peak day
        double maxTotal = 0;
        int peakDay = 0;
        for (int i = 0; i < 7; i++) {
            if (dailyTotals[i] > maxTotal) {
                maxTotal = dailyTotals[i];
                peakDay = i;
            }
        }

        if (tvChartPeak != null && maxTotal > 0) {
            tvChartPeak.setText("Highest: " + dayLabels[peakDay] + " " + formatCurrency(maxTotal));
            tvChartPeak.setVisibility(View.VISIBLE);
        } else if (tvChartPeak != null) {
            tvChartPeak.setVisibility(View.GONE);
        }

        // Always build chart entries (even if all zero)
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, (float) dailyTotals[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Spending");
        dataSet.setColor(0xFF9B5CFF);
        dataSet.setDrawValues(false);
        dataSet.setHighLightColor(0x00FFFFFF);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getXAxis().setEnabled(false);
        barChart.setTouchEnabled(false);
        barChart.setDrawValueAboveBar(false);
        barChart.setFitBars(true);
        barChart.invalidate();
    }

    private void setupChart() {
        if (barChart != null) {
            barChart.getDescription().setEnabled(false);
            barChart.setDrawGridBackground(false);
            barChart.getAxisRight().setEnabled(false);
            barChart.getAxisLeft().setEnabled(false);
            barChart.getXAxis().setEnabled(false);
            barChart.setTouchEnabled(false);
            barChart.setDrawBarShadow(false);
            barChart.setFitBars(true);
        }
    }

    private void setupRecyclerViews() {
        // Categories horizontal carousel
        categoryAdapter = new CategoryHomeAdapter();
        RecyclerView rvCategories = findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        categoryAdapter.setOnCategoryClickListener(cat -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("category_id", cat.getId());
            startActivity(intent);
        });

        categoryViewModel.getAllCategories().observe(this, categories -> {
            if (categories != null) {
                categoryAdapter.setCategories(categories);
            }
        });

        // Recent activity list
        recentAdapter = new DateAdapter();
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(this));
        rvRecentActivity.setAdapter(recentAdapter);

        // Click listener for recent activity items
        recentAdapter.setOnItemClickListener(dateLocation -> {
            Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
            intent.putExtra("date_location", dateLocation);
            startActivity(intent);
        });

        // Delete listener
        recentAdapter.setOnItemDeleteListener(dateLocation -> {
            dateViewModel.delete(dateLocation);
        });

        // Completion listener
        recentAdapter.setOnItemCompleteListener((dateLocation, isCompleted) -> {
            dateViewModel.update(dateLocation);
        });

        // Note: setOnRatingChangeListener exists but is not connected to any UI in DateAdapter
        // This was a pre-existing unused callback in the code

        // FAB auto-hide on scroll
        RecyclerView rvRecent = findViewById(R.id.rvRecentActivity);
        if (rvRecent != null) {
            rvRecent.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    private void setupClickListeners() {
        if (navHome != null) {
            navHome.setOnClickListener(v -> {});
        }

        if (navCategories != null) {
            navCategories.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class));
            });
        }

        if (navReviews != null) {
            navReviews.setOnClickListener(v -> {
                startActivity(new Intent(this, ReviewsActivity.class));
            });
        }

        if (navCalendar != null) {
            navCalendar.setOnClickListener(v -> {
                startActivity(new Intent(this, CalendarActivity.class));
            });
        }

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
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
    }

    private void startLiveTime() {
        timeHandler = new Handler();
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                Calendar cal = Calendar.getInstance();
                updateDateTime(cal);
                timeHandler.postDelayed(this, 60000); // Update every minute
            }
        };
        timeHandler.post(timeRunnable);
    }

    private void updateDateTime(Calendar cal) {
        if (tvTime != null) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            tvTime.setText(timeFormat.format(new Date()));
        }

        if (tvDate != null) {
            SimpleDateFormat dateFormatNew = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
            tvDate.setText(dateFormatNew.format(cal.getTime()));
        }

        if (tvGreeting != null) {
            SimpleDateFormat greetingTimeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            tvGreeting.setText(greetingTimeFormat.format(cal.getTime()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Calendar cal = Calendar.getInstance();
        updateDateTime(cal);
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
            // Refresh data - observers will handle it
        });
        dialog.show(getSupportFragmentManager(), "AddMomentDialog");
    }

    // Helper method to format currency
    private String formatCurrency(double amount) {
        return "₱" + String.format(Locale.getDefault(), "%,.0f", Math.abs(amount));
    }

    private String formatShortCurrency(double amount) {
        if (amount >= 1000000) {
            return "₱" + String.format(Locale.getDefault(), "%.1fM", amount / 1000000);
        } else if (amount >= 1000) {
            return "₱" + String.format(Locale.getDefault(), "%.1fk", amount / 1000);
        } else {
            return "₱" + String.format(Locale.getDefault(), "%.0f", amount);
        }
    }
}
