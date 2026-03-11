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
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private DateViewModel dateViewModel;
    private DateAdapter dateAdapter;
    private RecyclerView recyclerView;
    private View emptyState;
    private TextView tvEmptyTitle, tvEmptySubtitle;
    private ImageView ivEmpty;

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

        // Bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.navigation_favorites);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_categories) {
                startActivity(new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish(); return true;
            } else if (id == R.id.navigation_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
                finish(); return true;
            } else if (id == R.id.navigation_reviews) {
                startActivity(new Intent(this, ReviewsActivity.class));
                finish(); return true;
            } else if (id == R.id.navigation_favorites) {
                return true;
            }
            return false;
        });

        // Observe
        dateViewModel.getAllMoments().observe(this, moments -> {
            List<DateLocation> favorites = new ArrayList<>();
            if (moments != null) {
                for (DateLocation m : moments) {
                    if (m.getRating() >= 4.0f) favorites.add(m);
                }
            }
            dateAdapter.setDates(favorites);
            if (favorites.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
                if (tvEmptyTitle != null) tvEmptyTitle.setText("No favorites yet");
                if (tvEmptySubtitle != null) tvEmptySubtitle.setText("Rate moments 4+ stars to see them here");
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
        });
    }
}