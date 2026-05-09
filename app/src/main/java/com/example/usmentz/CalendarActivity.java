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

    private TextView tvMonth, tvYear, tvNavLabel;
    private TextView tvSelectedDate, tvDayTotal;
    private TextView tvNoMomentsDay;
    private RecyclerView rvCalendar, rvDayMoments;
    private View cardDayEvents, cardGrid;
    private View fabAdd;
    private View navHome, navCategories, navReviews;
    private View btnPrevMonth, btnNextMonth, btnAdd;
    private View segList, segGrid, segBoard, segCalendar;
    private ImageButton btnFilter, btnSort, btnAnalytics;

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
        tvNavLabel = findViewById(R.id.tvNavLabel);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvDayTotal = findViewById(R.id.tvDayTotal);
        tvNoMomentsDay = findViewById(R.id.tvNoMomentsDay);
        cardDayEvents = findViewById(R.id.cardDayEvents);
        cardGrid = findViewById(R.id.cardGrid);
        rvCalendar = findViewById(R.id.rvCalendar);
        rvDayMoments = findViewById(R.id.rvDayMoments);
        fabAdd = findViewById(R.id.fabAdd);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnAdd = findViewById(R.id.btnAdd);
        btnFilter = findViewById(R.id.btnFilter);
        btnSort = findViewById(R.id.btnSort);
        btnAnalytics = findViewById(R.id.btnAnalytics);
        segList = findViewById(R.id.segList);
        segGrid = findViewById(R.id.segGrid);
        segBoard = findViewById(R.id.segBoard);
        segCalendar = findViewById(R.id.segCalendar);
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
        // Month navigation
        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            selectedDay = null;
            hideDayEvents();
            updateCalendarDisplay();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            selectedDay = null;
            hideDayEvents();
            updateCalendarDisplay();
        });

        // Today button
        ((Button) findViewById(R.id.btnToday)).setOnClickListener(v -> {
            currentMonth.setTimeInMillis(System.currentTimeMillis());
            selectedDay = null;
            hideDayEvents();
            updateCalendarDisplay();
        });

        // Add button
        btnAdd.setOnClickListener(v -> showAddMomentDialog());

        // FAB
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showAddMomentDialog());
        }

        // Segmented control - view modes
        segList.setOnClickListener(v -> switchViewMode(0));
        segGrid.setOnClickListener(v -> switchViewMode(1));
        segBoard.setOnClickListener(v -> switchViewMode(2));
        segCalendar.setOnClickListener(v -> switchViewMode(3));

        // Filter, Sort, Analytics buttons
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                // TODO: Filter action
            });
        }
        if (btnSort != null) {
            btnSort.setOnClickListener(v -> {
                // TODO: Sort action
            });
        }
        if (btnAnalytics != null) {
            btnAnalytics.setOnClickListener(v -> {
                // TODO: Analytics action
            });
        }

        // Navbar
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
        updateSegmentedControl();
    }

    private void updateSegmentedControl() {
        // Reset all to inactive
        segList.setBackgroundResource(R.drawable.bg_segment_item);
        segGrid.setBackgroundResource(R.drawable.bg_segment_item);
        segBoard.setBackgroundResource(R.drawable.bg_segment_item);
        segCalendar.setBackgroundResource(R.drawable.bg_segment_item);

        ((TextView) segList).setTextColor(getColor(R.color.text_secondary));
        ((TextView) segGrid).setTextColor(getColor(R.color.text_secondary));
        ((TextView) segBoard).setTextColor(getColor(R.color.text_secondary));
        ((TextView) segCalendar).setTextColor(getColor(R.color.text_secondary));

        // Set active
        View activeTab;
        switch (currentViewMode) {
            case 0: activeTab = segList; break;
            case 1: activeTab = segGrid; break;
            case 2: activeTab = segBoard; break;
            default: activeTab = segCalendar; break;
        }
        activeTab.setBackgroundResource(R.drawable.bg_segment_item_active);
        ((TextView) activeTab).setTextColor(getColor(android.R.color.white));
    }

    private void hideDayEvents() {
        if (cardDayEvents != null) cardDayEvents.setVisibility(View.GONE);
    }

    private void showAddMomentDialog() {
        AddMomentDialog dialog = AddMomentDialog.newInstance(() -> {
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
        tvYear.setText(yearFmt.format(currentMonth.getTime()));
        tvNavLabel.setText(navFmt.format(currentMonth.getTime()));

        if (dataLoaded) {
            buildGrid();
        }

        updateSegmentedControl();
    }

    private void buildGrid() {
        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDow = cal.get(Calendar.DAY_OF_WEEK);
        int leadingDays = (firstDow == Calendar.SUNDAY) ? 0 : firstDow - 1;

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        String todayStr = keyFmt.format(Calendar.getInstance().getTime());

        // Build moment dates map - only track moments, not full objects
        Map<String, String> momentLabelMap = new HashMap<>();
        for (DateLocation moment : allMoments) {
            if (moment.getDate() != null) {
                momentLabelMap.put(keyFmt.format(moment.getDate()), moment.getName());
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
            cd.setHasMoment(momentLabelMap.containsKey(key));
            cd.setMomentLabel(momentLabelMap.get(key));
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

    @Override
    protected void onResume() {
        super.onResume();
        if (dataLoaded) {
            updateCalendarDisplay();
        }
    }
}