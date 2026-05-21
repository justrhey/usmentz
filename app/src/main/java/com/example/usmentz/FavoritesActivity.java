package com.example.usmentz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.adapter.DateAdapter;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.helper.CapsuleNavbarHelper;
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private DateViewModel dateViewModel;
    private DateAdapter dateAdapter;
    private RecyclerView recyclerView;
    private View emptyState;
    private TextView tvEmptyTitle, tvEmptySubtitle;
    private ImageView ivEmpty;
    private FloatingActionButton fabAdd;

    // Navigation - Capsule Navbar
    private LinearLayout navContainer;
    private LinearLayout navItemHome, navItemCategories, navItemCalendar, navItemFavorites, navItemSettings;
    private ImageView navIconHome, navIconCategories, navIconCalendar, navIconFavorites, navIconSettings;
    private TextView navLabelHome, navLabelCategories, navLabelCalendar, navLabelFavorites, navLabelSettings;
    private int activeNavSlot = 3; // Favorites active by default

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

        // FAB
        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            // Navigate to Home to add, or show a dialog if preferred
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        // Capsule Navbar
        com.google.android.material.card.MaterialCardView capsuleCard = findViewById(R.id.capsuleNavbar);
        navContainer = capsuleCard.findViewById(R.id.navContainer);
        navItemHome = navContainer.findViewById(R.id.navItemHome);
        navItemCategories = navContainer.findViewById(R.id.navItemCategories);
        navItemCalendar = navContainer.findViewById(R.id.navItemCalendar);
        navItemFavorites = navContainer.findViewById(R.id.navItemFavorites);
        navItemSettings = navContainer.findViewById(R.id.navItemSettings);
        navIconHome = navContainer.findViewById(R.id.navIconHome);
        navIconCategories = navContainer.findViewById(R.id.navIconCategories);
        navIconCalendar = navContainer.findViewById(R.id.navIconCalendar);
        navIconFavorites = navContainer.findViewById(R.id.navIconFavorites);
        navIconSettings = navContainer.findViewById(R.id.navIconSettings);
        navLabelHome = navContainer.findViewById(R.id.navLabelHome);
        navLabelCategories = navContainer.findViewById(R.id.navLabelCategories);
        navLabelCalendar = navContainer.findViewById(R.id.navLabelCalendar);
        navLabelFavorites = navContainer.findViewById(R.id.navLabelFavorites);
        navLabelSettings = navContainer.findViewById(R.id.navLabelSettings);
        setActiveNavSlot(3); // Favorites active by default

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

    private void setActiveNavSlot(int slotIndex) {
        activeNavSlot = slotIndex;
        CapsuleNavbarHelper.updateState(navContainer,
            new LinearLayout[]{navItemHome, navItemCategories, navItemCalendar, navItemFavorites, navItemSettings},
            new ImageView[]{navIconHome, navIconCategories, navIconCalendar, navIconFavorites, navIconSettings},
            new TextView[]{navLabelHome, navLabelCategories, navLabelCalendar, navLabelFavorites, navLabelSettings},
            slotIndex, this, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
