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

        // Toolbar with back button only
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Views
        recyclerView = findViewById(R.id.rvReviews);
        emptyState = findViewById(R.id.emptyStateLayout);
        tvEmptyTitle = findViewById(R.id.emptyStateText);
        tvEmptySubtitle = findViewById(R.id.emptyStateSubtext);

        // Setup RecyclerView
        reviewAdapter = new ReviewAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(reviewAdapter);

        // Click listener to go to detail
        reviewAdapter.setOnReviewClickListener(review -> {
            // Find the DateLocation for this review and open detail
            // Use SingleLiveEvent-style: observe with a guard so it only fires once
            dateViewModel.getDateById(review.getMomentId()).observe(ReviewsActivity.this, moment -> {
                if (moment != null) {
                    Intent intent = new Intent(ReviewsActivity.this, DetailActivity.class);
                    intent.putExtra("date_location", moment);
                    startActivity(intent);
                }
            });
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
                                m.getReview() != null ? m.getReview() : "",
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
                if (tvEmptySubtitle != null) tvEmptySubtitle.setText("Add reviews to moments to see them here");
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // LiveData observers will automatically refresh since Room queries update in real-time
    }
}