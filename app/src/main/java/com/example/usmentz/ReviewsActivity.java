package com.example.usmentz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.adapter.ReviewAdapter;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.model.Review;
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReviewsActivity extends AppCompatActivity {

    private DateViewModel dateViewModel;
    private ReviewAdapter reviewAdapter;
    private RecyclerView recyclerView;
    private View emptyState;
    private TextView tvEmptyTitle, tvEmptySubtitle;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

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
        recyclerView    = findViewById(R.id.rvReviews);
        emptyState      = findViewById(R.id.emptyStateLayout);
        tvEmptyTitle    = findViewById(R.id.emptyStateText);
        tvEmptySubtitle = findViewById(R.id.emptyStateSubtext);

        reviewAdapter = new ReviewAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(reviewAdapter);

        reviewAdapter.setOnReviewClickListener(review -> {
            Intent intent = new Intent(this, MomentsDetailActivity.class);
            intent.putExtra("moment_id", review.getMomentId());
            startActivity(intent);
        });

        // Bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.navigation_reviews);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_categories) {
                startActivity(new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                return true;
            } else if (id == R.id.navigation_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
                finish();
                return true;
            } else if (id == R.id.navigation_favorites) {
                startActivity(new Intent(this, FavoritesActivity.class));
                finish();
                return true;
            } else if (id == R.id.navigation_reviews) {
                return true;
            }
            return false;
        });

        // Observe and convert moments to reviews
        dateViewModel.getAllMoments().observe(this, moments -> {
            List<Review> reviews = new ArrayList<>();
            if (moments != null) {
                for (DateLocation m : moments) {
                    // Only show moments that have reviews OR photos
                    if ((m.getReview() != null && !m.getReview().isEmpty()) ||
                            (m.getPhotoPath() != null && !m.getPhotoPath().isEmpty())) {

                        String date = m.getDate() != null ? dateFormat.format(m.getDate()) : "No date";

                        Review review = new Review(
                                m.getId(),
                                m.getName(),
                                m.getReview() != null ? m.getReview() : "No review text",
                                m.getRating(),
                                m.getPhotoPath(),
                                date
                        );
                        reviews.add(review);
                    }
                }
            }

            reviewAdapter.setReviews(reviews);

            if (reviews.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
                if (tvEmptyTitle != null) tvEmptyTitle.setText("No reviews yet");
                if (tvEmptySubtitle != null) tvEmptySubtitle.setText("Add reviews or photos to moments to see them here");
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
        });
    }
}