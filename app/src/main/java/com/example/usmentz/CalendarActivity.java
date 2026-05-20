package com.example.usmentz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    // Header
    private TextView tvMonthYear;

    // Calendar grid
    private RecyclerView rvCalendar;

    // List section
    private RecyclerView rvMoments;
    private TextView tvListHeader;
    private TextView tvNoMoments;

    // Navigation
    private View btnPrevMonth, btnNextMonth, btnToday;
    private View btnBack, btnOpenDrawer;
    private FloatingActionButton fabAdd;
    private View navbarContainer;

    // Expanding pill navbar items
    private View navItemHome, navItemCategories, navItemCalendar, navItemFavorites;

    private DrawerLayout drawerLayout;
    private NavigationView navDrawer;

    private CalendarAdapter calendarAdapter;
    private DateAdapter momentsAdapter;

    private final Calendar currentMonth = Calendar.getInstance();
    private CalendarDay selectedDay = null;

    private final SimpleDateFormat monthYearFmt = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private final SimpleDateFormat monthYearCapsFmt = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private final SimpleDateFormat dayFmt = new SimpleDateFormat("MMM d", Locale.getDefault());
    private final SimpleDateFormat keyFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private List<DateLocation> allMoments = new ArrayList<>();
    private boolean dataLoaded = false;
    private int currentViewMode = 3; // Calendar = 3

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
    }

    private void initViews() {
        tvMonthYear = findViewById(R.id.tvMonthYear);
        rvCalendar = findViewById(R.id.rvCalendar);
        rvMoments = findViewById(R.id.rvMoments);
        tvListHeader = findViewById(R.id.tvListHeader);
        tvNoMoments = findViewById(R.id.tvNoMoments);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnToday = findViewById(R.id.btnToday);
        btnBack = findViewById(R.id.btnBack);
        btnOpenDrawer = findViewById(R.id.btnOpenDrawer);
        fabAdd = findViewById(R.id.fabAdd);
        navbarContainer = findViewById(R.id.navbarContainer);

        navItemHome = findViewById(R.id.navItemHome);
        navItemCategories = findViewById(R.id.navItemCategories);
        navItemCalendar = findViewById(R.id.navItemCalendar);
        navItemFavorites = findViewById(R.id.navItemFavorites);
    }

    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navDrawer = findViewById(R.id.navDrawer);

        if (btnOpenDrawer != null) {
            btnOpenDrawer.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

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
        rvMoments.setLayoutManager(new LinearLayoutManager(this));
        rvMoments.setAdapter(momentsAdapter);

        calendarAdapter.setOnDayClickListener(day -> {
            if (day.getDay() == 0) return;

            if (selectedDay != null) selectedDay.setSelected(false);
            day.setSelected(true);
            selectedDay = day;
            calendarAdapter.notifyDataSetChanged();
            showDayMoments(day);
        });

        updateCalendarDisplay();
    }

    private void setupClickListeners() {
        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            selectedDay = null;
            updateCalendarDisplay();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            selectedDay = null;
            updateCalendarDisplay();
        });

        ((Button) btnToday).setOnClickListener(v -> {
            currentMonth.setTimeInMillis(System.currentTimeMillis());
            selectedDay = null;
            updateCalendarDisplay();
        });

        btnBack.setOnClickListener(v -> finish());

        fabAdd.setOnClickListener(v -> showAddMomentDialog());

        // Navbar items
        navItemHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
        });
        navItemCategories.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });
        navItemCalendar.setOnClickListener(v -> {}); // Already on calendar
        navItemFavorites.setOnClickListener(v -> {
            startActivity(new Intent(this, FavoritesActivity.class));
        });
    }

    private void switchViewMode(int mode) {
        currentViewMode = mode;
        if (navDrawer != null) {
            int[] ids = {R.id.nav_list, R.id.nav_grid, R.id.nav_board, R.id.nav_calendar};
            if (mode >= 0 && mode < ids.length) {
                navDrawer.setCheckedItem(ids[mode]);
            }
        }
    }

    private void showAddMomentDialog() {
        AddMomentDialog dialog = AddMomentDialog.newInstance(moment -> {
            loadData();
        });
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
        // Header: "JULY 2026" uppercase
        String headerText = monthYearCapsFmt.format(currentMonth.getTime()).toUpperCase(Locale.getDefault());
        tvMonthYear.setText(headerText);

        if (dataLoaded) {
            buildGrid();
        }

        // Sync nav drawer
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

        // Group moments by date key
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

    private void showDayMoments(CalendarDay day) {
        if (day.getDay() == 0) return;

        String dayKey = keyFmt.format(day.getDate().getTime());
        tvListHeader.setText(dayFmt.format(day.getDate().getTime()));

        List<DateLocation> dayMoments = new ArrayList<>();
        for (DateLocation m : allMoments) {
            if (m.getDate() != null && keyFmt.format(m.getDate()).equals(dayKey)) {
                dayMoments.add(m);
            }
        }

        momentsAdapter.setDates(dayMoments);

        if (dayMoments.isEmpty()) {
            tvNoMoments.setVisibility(View.VISIBLE);
            rvMoments.setVisibility(View.GONE);
        } else {
            tvNoMoments.setVisibility(View.GONE);
            rvMoments.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataLoaded) {
            updateCalendarDisplay();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
