package com.example.usmentz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.adapter.CalendarAdapter;
import com.example.usmentz.adapter.DateAdapter;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.model.CalendarDay;
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private DateViewModel dateViewModel;

    private TextView tvMonth, tvYear, tvNavLabel;
    private TextView tvSelectedDate, tvDayTotal;
    private TextView tvNoMomentsDay;
    private RecyclerView rvCalendar, rvDayMoments;
    private View cardDayEvents;
    private View fabAdd;
    private View navHome, navCategories, navReviews;
    private View btnPrevMonth, btnNextMonth, btnAdd;
    private View topHeader, todayBar;
    private View btnOpenDrawer, btnCloseDrawer;
    private View floatingNavbarContainer;

    private DrawerLayout drawerLayout;
    private NavigationView navDrawer;

    private CalendarAdapter calendarAdapter;
    private DateAdapter momentsAdapter;

    private final Calendar currentMonth = Calendar.getInstance();
    private CalendarDay selectedDay = null;

    private final SimpleDateFormat monthFmt = new SimpleDateFormat("MMMM", Locale.getDefault());
    private final SimpleDateFormat yearFmt = new SimpleDateFormat("yyyy", Locale.getDefault());
    private final SimpleDateFormat navFmt = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private final SimpleDateFormat dayFmt = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
    private final SimpleDateFormat keyFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private List<DateLocation> allMoments = new ArrayList<>();
    private boolean dataLoaded = false;
    private int currentViewMode = 3; // Calendar = 3

    // ── Auto-hide navbar ──────────────────────────
    private final Handler hideHandler = new Handler(Looper.getMainLooper());
    private Runnable hideRunnable;
    private boolean navbarVisible = true;
    private static final long HIDE_DELAY_MS = 5000;

    // Touch delegate to catch all screen touches
    private final View.OnTouchListener screenTouchListener = (v, event) -> {
        showNavbar();
        resetHideTimer();
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);

        initViews();
        setupDrawer();
        setupCalendar();
        setupClickListeners();
        loadData();
        startHideTimer();

        // Intercept all touches on the main content to show navbar
        View root = findViewById(android.R.id.content);
        root.setOnTouchListener(screenTouchListener);

        // Handle back press to close drawer
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void initViews() {
        tvMonth = findViewById(R.id.tvMonth);
        tvYear = findViewById(R.id.tvYear);
        tvNavLabel = findViewById(R.id.tvNavLabel);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvDayTotal = findViewById(R.id.tvDayTotal);
        tvNoMomentsDay = findViewById(R.id.tvNoMomentsDay);
        cardDayEvents = findViewById(R.id.cardDayEvents);
        rvCalendar = findViewById(R.id.rvCalendar);
        rvDayMoments = findViewById(R.id.rvDayMoments);
        fabAdd = findViewById(R.id.fabAdd);
        topHeader = findViewById(R.id.topHeader);
        todayBar = findViewById(R.id.todayBar);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnAdd = findViewById(R.id.btnAdd);
        btnOpenDrawer = findViewById(R.id.btnOpenDrawer);
        floatingNavbarContainer = findViewById(R.id.floatingNavbarContainer);
    }

    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navDrawer = findViewById(R.id.navDrawer);

        // Open drawer
        if (btnOpenDrawer != null) {
            btnOpenDrawer.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        // Close button in drawer header
        View headerView = navDrawer != null ? navDrawer.getHeaderView(0) : null;
        if (headerView != null) {
            btnCloseDrawer = headerView.findViewById(R.id.btnCloseDrawer);
            if (btnCloseDrawer != null) {
                btnCloseDrawer.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
            }
        }

        // Nav item clicks
        if (navDrawer != null) {
            navDrawer.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_list) {
                    switchViewMode(0);
                } else if (id == R.id.nav_grid) {
                    switchViewMode(1);
                } else if (id == R.id.nav_board) {
                    switchViewMode(2);
                } else if (id == R.id.nav_calendar) {
                    switchViewMode(3);
                } else if (id == R.id.nav_filter) {
                    // TODO: filter action
                } else if (id == R.id.nav_sort) {
                    // TODO: sort action
                } else if (id == R.id.nav_analytics) {
                    // TODO: analytics action
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }

    private void setupCalendar() {
        calendarAdapter = new CalendarAdapter();
        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        rvCalendar.setAdapter(calendarAdapter);

        momentsAdapter = new DateAdapter();
        momentsAdapter.setOnItemClickListener(dateLocation -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("date_location", dateLocation);
            startActivity(intent);
        });
        rvDayMoments.setLayoutManager(new LinearLayoutManager(this));
        rvDayMoments.setAdapter(momentsAdapter);

        calendarAdapter.setOnDayClickListener(day -> {
            if (day.getDay() == 0) return;

            if (selectedDay != null) selectedDay.setSelected(false);
            day.setSelected(true);
            selectedDay = day;
            calendarAdapter.notifyDataSetChanged();
            showDayEvents(day);
        });

        updateCalendarDisplay();
    }

    private void setupClickListeners() {
        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            selectedDay = null;
            hideDayEvents();
            updateCalendarDisplay();
            resetHideTimer();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            selectedDay = null;
            hideDayEvents();
            updateCalendarDisplay();
            resetHideTimer();
        });

        ((Button) findViewById(R.id.btnToday)).setOnClickListener(v -> {
            currentMonth.setTimeInMillis(System.currentTimeMillis());
            selectedDay = null;
            hideDayEvents();
            updateCalendarDisplay();
            resetHideTimer();
        });

        btnAdd.setOnClickListener(v -> {
            showAddMomentDialog();
            resetHideTimer();
        });

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                showAddMomentDialog();
                resetHideTimer();
            });
        }

        // Navbar items
        navHome = findViewById(R.id.navHome);
        navCategories = findViewById(R.id.navCategories);
        navReviews = findViewById(R.id.navReviews);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            });
        }
        if (navCategories != null) {
            navCategories.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        }
        if (navReviews != null) {
            navReviews.setOnClickListener(v -> {
                startActivity(new Intent(this, ReviewsActivity.class));
                finish();
            });
        }
    }

    private void switchViewMode(int mode) {
        currentViewMode = mode;
        // Update nav drawer check state
        if (navDrawer != null) {
            int[] ids = {R.id.nav_list, R.id.nav_grid, R.id.nav_board, R.id.nav_calendar};
            if (mode >= 0 && mode < ids.length) {
                navDrawer.setCheckedItem(ids[mode]);
            }
        }
    }

    private void hideDayEvents() {
        if (cardDayEvents != null) cardDayEvents.setVisibility(View.GONE);
    }

    private void showAddMomentDialog() {
        AddMomentDialog dialog = AddMomentDialog.newInstance(this::loadData);
        dialog.show(getSupportFragmentManager(), "AddMomentDialog");
    }

    private void loadData() {
        dateViewModel.getAllMoments().observe(this, moments -> {
            if (moments != null) {
                allMoments = moments;
                dataLoaded = true;
                updateCalendarDisplay();
            }
        });
    }

    private void updateCalendarDisplay() {
        tvMonth.setText(monthFmt.format(currentMonth.getTime()));
        tvYear.setText(yearFmt.format(currentMonth.getTime()));
        tvNavLabel.setText(navFmt.format(currentMonth.getTime()));

        if (dataLoaded) {
            buildGrid();
        }

        // Sync nav drawer checked state
        if (navDrawer != null) {
            int[] ids = {R.id.nav_list, R.id.nav_grid, R.id.nav_board, R.id.nav_calendar};
            if (currentViewMode >= 0 && currentViewMode < ids.length) {
                navDrawer.setCheckedItem(ids[currentViewMode]);
            }
        }
    }

    private void buildGrid() {
        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDow = cal.get(Calendar.DAY_OF_WEEK);
        int leadingDays = (firstDow == Calendar.SUNDAY) ? 0 : firstDow - 1;

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        String todayStr = keyFmt.format(Calendar.getInstance().getTime());

        // Group moments by date key - supports multiple moments per day
        Map<String, List<String>> momentGroupMap = new HashMap<>();
        for (DateLocation moment : allMoments) {
            if (moment.getDate() != null) {
                String key = keyFmt.format(moment.getDate());
                momentGroupMap.computeIfAbsent(key, k -> new ArrayList<>()).add(moment.getName());
            }
        }

        List<CalendarDay> days = new ArrayList<>();

        for (int i = 0; i < leadingDays; i++) {
            days.add(new CalendarDay(0, null));
        }

        for (int d = 1; d <= daysInMonth; d++) {
            Calendar dayCal = (Calendar) currentMonth.clone();
            dayCal.set(Calendar.DAY_OF_MONTH, d);
            String key = keyFmt.format(dayCal.getTime());

            CalendarDay cd = new CalendarDay(d, dayCal);

            List<String> labels = momentGroupMap.get(key);
            if (labels != null && !labels.isEmpty()) {
                cd.setMomentLabels(labels);
            }

            cd.setToday(key.equals(todayStr));

            if (selectedDay != null && selectedDay.getDay() == d &&
                    selectedDay.getDate() != null &&
                    selectedDay.getDate().get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                    selectedDay.getDate().get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR)) {
                cd.setSelected(true);
                selectedDay = cd;
            }

            days.add(cd);
        }

        calendarAdapter.setDays(days);
    }

    private void showDayEvents(CalendarDay day) {
        if (day.getDay() == 0) return;

        tvSelectedDate.setText(dayFmt.format(day.getDate().getTime()));
        cardDayEvents.setVisibility(View.VISIBLE);

        String dayKey = keyFmt.format(day.getDate().getTime());

        List<DateLocation> dayMoments = new ArrayList<>();
        for (DateLocation m : allMoments) {
            if (m.getDate() != null && keyFmt.format(m.getDate()).equals(dayKey)) {
                dayMoments.add(m);
            }
        }

        momentsAdapter.setDates(dayMoments);

        if (dayMoments.isEmpty()) {
            tvNoMomentsDay.setVisibility(View.VISIBLE);
            rvDayMoments.setVisibility(View.GONE);
            tvDayTotal.setText("0 moments");
        } else {
            tvNoMomentsDay.setVisibility(View.GONE);
            rvDayMoments.setVisibility(View.VISIBLE);
            tvDayTotal.setText(dayMoments.size() + " moment" + (dayMoments.size() > 1 ? "s" : ""));
        }
    }

    // ── Auto-hide navbar ───────────────────────────

    private void startHideTimer() {
        hideRunnable = () -> {
            navbarVisible = false;
            if (floatingNavbarContainer != null) {
                floatingNavbarContainer.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> floatingNavbarContainer.setVisibility(View.GONE))
                        .start();
            }
            if (fabAdd != null) {
                fabAdd.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> fabAdd.setVisibility(View.GONE))
                        .start();
            }
        };
        hideHandler.postDelayed(hideRunnable, HIDE_DELAY_MS);
    }

    private void resetHideTimer() {
        if (hideRunnable != null) hideHandler.removeCallbacks(hideRunnable);
        startHideTimer();
    }

    private void showNavbar() {
        if (!navbarVisible) {
            navbarVisible = true;
            if (floatingNavbarContainer != null) {
                floatingNavbarContainer.setVisibility(View.VISIBLE);
                floatingNavbarContainer.animate().alpha(1f).setDuration(200).start();
            }
            if (fabAdd != null) {
                fabAdd.setVisibility(View.VISIBLE);
                fabAdd.animate().alpha(1f).setDuration(200).start();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataLoaded) {
            updateCalendarDisplay();
        }
        resetHideTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideHandler.removeCallbacks(hideRunnable);
    }
}