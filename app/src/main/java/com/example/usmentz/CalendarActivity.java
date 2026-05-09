package com.example.usmentz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.adapter.CalendarAdapter;
import com.example.usmentz.adapter.DateAdapter;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.model.CalendarDay;
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private DateViewModel dateViewModel;

    private TextView tvMonth, tvYear, tvSelectedDate, tvDayTotal;
    private TextView tvNoMomentsDay, tvCalendarHint;
    private RecyclerView rvCalendar, rvDayMoments;
    private View cardDayEvents, fabAdd, navAdd;

    private CalendarAdapter calendarAdapter;
    private DateAdapter momentsAdapter;

    private final Calendar currentMonth = Calendar.getInstance();
    private CalendarDay selectedDay = null;

    private final SimpleDateFormat monthFmt = new SimpleDateFormat("MMMM", Locale.getDefault());
    private final SimpleDateFormat dayFmt = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
    private final SimpleDateFormat keyFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private List<DateLocation> allMoments = new ArrayList<>();
    private boolean dataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);

        initViews();
        setupCalendar();
        setupClickListeners();
        loadData();
    }

    private void initViews() {
        tvMonth = findViewById(R.id.tvMonth);
        tvYear = findViewById(R.id.tvYear);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvDayTotal = findViewById(R.id.tvDayTotal);
        tvNoMomentsDay = findViewById(R.id.tvNoMomentsDay);
        tvCalendarHint = findViewById(R.id.tvCalendarHint);
        cardDayEvents = findViewById(R.id.cardDayEvents);
        rvCalendar = findViewById(R.id.rvCalendar);
        rvDayMoments = findViewById(R.id.rvDayMoments);
        fabAdd = findViewById(R.id.fabAdd);
        navAdd = findViewById(R.id.navAdd);
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

        // Day click
        calendarAdapter.setOnDayClickListener(day -> {
            if (day.getDay() == 0) return; // Empty cell
            
            if (selectedDay != null) selectedDay.setSelected(false);
            day.setSelected(true);
            selectedDay = day;
            calendarAdapter.notifyDataSetChanged();
            showDayEvents(day);
        });

        updateCalendarDisplay();
    }

    private void setupClickListeners() {
        // Month navigation
        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            selectedDay = null;
            cardDayEvents.setVisibility(View.GONE);
            tvCalendarHint.setVisibility(View.VISIBLE);
            updateCalendarDisplay();
        });

        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            selectedDay = null;
            cardDayEvents.setVisibility(View.GONE);
            tvCalendarHint.setVisibility(View.VISIBLE);
            updateCalendarDisplay();
        });

        // Today button
        ((Button) findViewById(R.id.btnToday)).setOnClickListener(v -> {
            currentMonth.setTimeInMillis(System.currentTimeMillis());
            selectedDay = null;
            cardDayEvents.setVisibility(View.GONE);
            tvCalendarHint.setVisibility(View.VISIBLE);
            updateCalendarDisplay();
        });

        // FAB and Add button
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showAddMomentDialog());
        }
        if (navAdd != null) {
            navAdd.setOnClickListener(v -> showAddMomentDialog());
        }

        // Navbar
        View navHome = findViewById(R.id.navHome);
        View navCategories = findViewById(R.id.navCategories);
        View navReviews = findViewById(R.id.navReviews);

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

    private void showAddMomentDialog() {
        AddMomentDialog dialog = AddMomentDialog.newInstance(() -> {
            // Refresh data
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
        tvMonth.setText(monthFmt.format(currentMonth.getTime()));
        tvYear.setText(String.valueOf(currentMonth.get(Calendar.YEAR)));

        if (dataLoaded) {
            buildGrid();
        }
    }

    private void buildGrid() {
        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDow = cal.get(Calendar.DAY_OF_WEEK);
        int leadingDays = (firstDow == Calendar.SUNDAY) ? 0 : firstDow - 1;

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        String todayStr = keyFmt.format(Calendar.getInstance().getTime());

        // Build moment dates map
        Map<String, Boolean> momentDateMap = new HashMap<>();
        for (DateLocation moment : allMoments) {
            if (moment.getDate() != null) {
                momentDateMap.put(keyFmt.format(moment.getDate()), true);
            }
        }

        List<CalendarDay> days = new ArrayList<>();

        // Empty cells before first day
        for (int i = 0; i < leadingDays; i++) {
            days.add(new CalendarDay(0, null));
        }

        // Actual days
        for (int d = 1; d <= daysInMonth; d++) {
            Calendar dayCal = (Calendar) currentMonth.clone();
            dayCal.set(Calendar.DAY_OF_MONTH, d);

            String key = keyFmt.format(dayCal.getTime());

            CalendarDay cd = new CalendarDay(d, dayCal);
            cd.setHasMoment(momentDateMap.containsKey(key));
            cd.setToday(key.equals(todayStr));

            // Check if this was previously selected
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
        tvCalendarHint.setVisibility(View.GONE);

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
            tvDayTotal.setText("₱0.00");
        } else {
            tvNoMomentsDay.setVisibility(View.GONE);
            rvDayMoments.setVisibility(View.VISIBLE);
            
            // Calculate total (simplified)
            tvDayTotal.setText(dayMoments.size() + " moment" + (dayMoments.size() > 1 ? "s" : ""));
        }
    }
}