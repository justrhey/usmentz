package com.example.usmentz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.adapter.CalendarAdapter;
import com.example.usmentz.adapter.ExpenseAdapter;
import com.example.usmentz.adapter.DateAdapter;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.fina.Expense;
import com.example.usmentz.model.CalendarDay;
import com.example.usmentz.viewmodel.DateViewModel;
import com.example.usmentz.viewmodel.ExpenseViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private DateViewModel dateViewModel;
    private ExpenseViewModel expenseViewModel;

    private TextView tvMonth, tvYear, tvSelectedDate;
    private TextView tvNoMomentsDay, tvNoExpensesDay, tvDayTotal;
    private TextView tvCalendarHint;
    private RecyclerView rvCalendar, rvDayMoments, rvDayExpenses;
    private View cardDayEvents;

    private CalendarAdapter calendarAdapter;
    private DateAdapter momentsAdapter;
    private ExpenseAdapter expenseAdapter;

    private final Calendar currentMonth = Calendar.getInstance();
    private CalendarDay selectedDay = null;

    private final SimpleDateFormat monthFmt = new SimpleDateFormat("MMMM", Locale.getDefault());
    private final SimpleDateFormat dayFmt   = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
    private final SimpleDateFormat keyFmt   = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // FIX: Cache all moments and expenses
    private List<DateLocation> allMoments = new ArrayList<>();
    private Map<Integer, List<Expense>> expensesCache = new HashMap<>();
    private boolean dataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        dateViewModel    = new ViewModelProvider(this).get(DateViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        // ── Toolbar with back ─────────────────────────────────────
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // ── Bottom nav — highlight Calendar tab ───────────────────
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.navigation_calendar);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_categories) {
                startActivity(new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                return true;
            } else if (id == R.id.navigation_favorites) {
                startActivity(new Intent(this, FavoritesActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                return true;
            } else if (id == R.id.navigation_reviews) {
                startActivity(new Intent(this, ReviewsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                return true;
            } else if (id == R.id.navigation_calendar) {
                return true;
            }
            return false;
        });

        // ── Views ─────────────────────────────────────────────────
        tvMonth        = findViewById(R.id.tvMonth);
        tvYear         = findViewById(R.id.tvYear);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvCalendarHint = findViewById(R.id.tvCalendarHint);
        tvNoMomentsDay = findViewById(R.id.tvNoMomentsDay);
        tvNoExpensesDay= findViewById(R.id.tvNoExpensesDay);
        tvDayTotal     = findViewById(R.id.tvDayTotal);
        cardDayEvents  = findViewById(R.id.cardDayEvents);
        rvCalendar     = findViewById(R.id.rvCalendar);
        rvDayMoments   = findViewById(R.id.rvDayMoments);
        rvDayExpenses  = findViewById(R.id.rvDayExpenses);

        // ── Calendar grid ─────────────────────────────────────────
        calendarAdapter = new CalendarAdapter();
        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        rvCalendar.setAdapter(calendarAdapter);

        // ── Moments adapter for day details ───────────────────────
        momentsAdapter = new DateAdapter();
        momentsAdapter.setOnItemClickListener(dateLocation -> {
            Intent intent = new Intent(this, MomentsDetailActivity.class);
            intent.putExtra("moment_id", dateLocation.getId());
            startActivity(intent);
        });
        rvDayMoments.setLayoutManager(new LinearLayoutManager(this));
        rvDayMoments.setAdapter(momentsAdapter);

        // ── Expenses adapter ──────────────────────────────────────
        expenseAdapter = new ExpenseAdapter();
        expenseAdapter.setOnExpenseDeleteListener(expense -> {
            // Handle delete if needed
        });
        rvDayExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvDayExpenses.setAdapter(expenseAdapter);

        // ── Month nav ─────────────────────────────────────────────
        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateCalendarDisplay(); // FIX: Use cached data
        });
        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateCalendarDisplay(); // FIX: Use cached data
        });

        // ── Year nav ─────────────────────────────────────────────
        findViewById(R.id.btnPrevYear).setOnClickListener(v -> {
            currentMonth.add(Calendar.YEAR, -1);
            updateCalendarDisplay(); // FIX: Use cached data
        });
        findViewById(R.id.btnNextYear).setOnClickListener(v -> {
            currentMonth.add(Calendar.YEAR, 1);
            updateCalendarDisplay(); // FIX: Use cached data
        });
        tvYear.setOnClickListener(v -> showYearPickerDialog());

        // ── Today ─────────────────────────────────────────────────
        findViewById(R.id.btnToday).setOnClickListener(v -> {
            currentMonth.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
            selectedDay = null;
            cardDayEvents.setVisibility(View.GONE);
            tvCalendarHint.setVisibility(View.VISIBLE);
            updateCalendarDisplay(); // FIX: Use cached data
        });

        // ── Day click ─────────────────────────────────────────────
        calendarAdapter.setOnDayClickListener(day -> {
            if (selectedDay != null) selectedDay.setSelected(false);
            day.setSelected(true);
            selectedDay = day;
            calendarAdapter.notifyDataSetChanged();
            showDayEvents(day);
        });

        // FIX: Load data ONCE and cache it
        loadAllDataOnce();
    }

    // FIX: Load all data once and observe permanently
    private void loadAllDataOnce() {
        dateViewModel.getAllMoments().observe(this, moments -> {
            if (moments != null) {
                allMoments = moments;
                dataLoaded = true;
                updateCalendarDisplay();

                // Pre-load expenses for all moments
                for (DateLocation moment : moments) {
                    loadExpensesForMoment(moment.getId());
                }
            }
        });
    }

    // FIX: Cache expenses per moment
    private void loadExpensesForMoment(int momentId) {
        expenseViewModel.getExpensesForMoment(momentId).observe(this, expenses -> {
            if (expenses != null) {
                expensesCache.put(momentId, expenses);
                // If this is the currently selected day, update display
                if (selectedDay != null) {
                    String selectedKey = keyFmt.format(selectedDay.getDate().getTime());
                    String momentKey = getMomentKey(momentId);
                    if (momentKey != null && momentKey.equals(selectedKey)) {
                        showDayEvents(selectedDay);
                    }
                }
            }
        });
    }

    private String getMomentKey(int momentId) {
        for (DateLocation moment : allMoments) {
            if (moment.getId() == momentId && moment.getDate() != null) {
                return keyFmt.format(moment.getDate());
            }
        }
        return null;
    }

    // FIX: Update calendar instantly using cached data
    private void updateCalendarDisplay() {
        tvMonth.setText(monthFmt.format(currentMonth.getTime()));
        tvYear.setText(String.valueOf(currentMonth.get(Calendar.YEAR)));

        if (dataLoaded) {
            buildGridInstant();
        }
    }

    // FIX: Build grid without database queries
    private void buildGridInstant() {
        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDow = cal.get(Calendar.DAY_OF_WEEK);
        int leadingDays = (firstDow == Calendar.SUNDAY) ? 0 : firstDow - 1;

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        String todayStr = keyFmt.format(Calendar.getInstance().getTime());

        // Create a set of moment dates for quick lookup
        Map<String, Boolean> momentDateMap = new HashMap<>();
        for (DateLocation moment : allMoments) {
            if (moment.getDate() != null) {
                momentDateMap.put(keyFmt.format(moment.getDate()), true);
            }
        }

        List<CalendarDay> days = new ArrayList<>();

        // Empty cells
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
        tvSelectedDate.setText(dayFmt.format(day.getDate().getTime()));
        cardDayEvents.setVisibility(View.VISIBLE);
        tvCalendarHint.setVisibility(View.GONE);

        String dayKey = keyFmt.format(day.getDate().getTime());

        // Use cached moments
        List<DateLocation> dayMoments = new ArrayList<>();
        for (DateLocation m : allMoments) {
            if (m.getDate() != null && keyFmt.format(m.getDate()).equals(dayKey)) {
                dayMoments.add(m);
            }
        }

        momentsAdapter.setDates(dayMoments);
        tvNoMomentsDay.setVisibility(dayMoments.isEmpty() ? View.VISIBLE : View.GONE);

        // Use cached expenses
        showDayExpenses(dayMoments);
    }

    private void showDayExpenses(List<DateLocation> dayMoments) {
        List<Expense> allExpenses = new ArrayList<>();
        double total = 0.0;

        for (DateLocation moment : dayMoments) {
            List<Expense> expenses = expensesCache.get(moment.getId());
            if (expenses != null) {
                allExpenses.addAll(expenses);
                for (Expense e : expenses) {
                    total += e.getAmount();
                }
            }
        }

        expenseAdapter.setExpenses(allExpenses);
        tvNoExpensesDay.setVisibility(allExpenses.isEmpty() ? View.VISIBLE : View.GONE);
        tvDayTotal.setText(String.format(Locale.getDefault(), "₱%.2f", total));
    }

    private void showYearPickerDialog() {
        int currentYear = currentMonth.get(Calendar.YEAR);

        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(2000);
        picker.setMaxValue(2100);
        picker.setValue(currentYear);
        picker.setWrapSelectorWheel(false);

        new AlertDialog.Builder(this)
                .setTitle("Select Year")
                .setView(picker)
                .setPositiveButton("OK", (dialog, which) -> {
                    currentMonth.set(Calendar.YEAR, picker.getValue());
                    updateCalendarDisplay();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}