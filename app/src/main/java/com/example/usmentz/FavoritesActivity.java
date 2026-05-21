package com.example.usmentz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.adapter.DateAdapter;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.helper.NavbarScrollHelper;
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private DateViewModel dateViewModel;
    private DateAdapter dateAdapter;
    private RecyclerView recyclerView;
    private View emptyState;
    private TextView tvEmptyTitle, tvEmptySubtitle;
    private ImageView ivEmpty;

    // Expanding pill navbar
    private View navbarContainer;
    private View navItemHome, navItemCategories, navItemCalendar;
    private View navHomeActive, navHomeInactive;
    private View navCategoriesActive, navCategoriesInactive;
    private View navCalendarActive, navCalendarInactive;
    private int activeNavSlot = 1; // Categories active by default (came from categories)

    // Scroll-based navbar animation
    private NavbarScrollHelper navbarScrollHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // RecyclerView
        recyclerView   = findViewById(R.id.rvFavorites);
        emptyState     = findViewById(R.id.emptyStateLayout);
        tvEmptyTitle   = findViewById(R.id.emptyStateText);
        tvEmptySubtitle= findViewById(R.id.emptyStateSubtext);

        dateAdapter = new DateAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(dateAdapter);

        dateAdapter.setOnItemClickListener(moment -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("date_location", moment);
            startActivity(intent);
        });

        // Expanding pill navbar
        navbarContainer = findViewById(R.id.navbarContainer);
        navItemHome = findViewById(R.id.navItemHome);
        navItemCategories = findViewById(R.id.navItemCategories);
        navItemCalendar = findViewById(R.id.navItemCalendar);
        navHomeActive = findViewById(R.id.navHomeActive);
        navHomeInactive = findViewById(R.id.navHomeInactive);
        navCategoriesActive = findViewById(R.id.navCategoriesActive);
        navCategoriesInactive = findViewById(R.id.navCategoriesInactive);
        navCalendarActive = findViewById(R.id.navCalendarActive);
        navCalendarInactive = findViewById(R.id.navCalendarInactive);
        setActiveNavSlot(1); // Categories active by default

        // Scroll-based navbar/FAB slide animation
        View fabAdd = findViewById(R.id.fabAdd);
        navbarScrollHelper = new NavbarScrollHelper(navbarContainer, fabAdd);
        navbarScrollHelper.attachToRecyclerView(recyclerView);

        // Setup click listeners for navbar
        setupNavigation();

        // Observe - show moments marked as "do again"
        dateViewModel.getAllMoments().observe(this, moments -> {
            List<DateLocation> favorites = new ArrayList<>();
            if (moments != null) {
                for (DateLocation m : moments) {
                    if (m.isDoAgain()) favorites.add(m);
                }
            }
            dateAdapter.setDates(favorites);
            if (favorites.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
                if (tvEmptyTitle != null) tvEmptyTitle.setText("No favorites yet");
                if (tvEmptySubtitle != null) tvEmptySubtitle.setText("Mark moments as \"do again\" to see them here");
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
        });
    }

    private void setupNavigation() {
        navItemHome.setOnClickListener(v -> {
            setActiveNavSlot(0);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        navItemCategories.setOnClickListener(v -> {
            setActiveNavSlot(1);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        navItemCalendar.setOnClickListener(v -> {
            setActiveNavSlot(2);
            startActivity(new Intent(this, CalendarActivity.class));
            finish();
        });
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

    @Override
    protected void onResume() {
        super.onResume();
        if (navbarScrollHelper != null) navbarScrollHelper.forceShow();
    }
}
