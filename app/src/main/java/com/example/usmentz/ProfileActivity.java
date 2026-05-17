package com.example.usmentz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.usmentz.database.DateDatabase;
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UsmentzPrefs";

    private ImageView ivAvatar;
    private TextView tvName, tvEmail, tvMomentsCount, tvReviewsCount;
    private ImageButton btnBack;
    private Button btnLogout;
    private View cardEditProfile, cardNotifications, cardPrivacy, cardHelp, cardAbout;

    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private DateViewModel dateViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);

        initViews();
        loadUserData();
        loadStats();
        setupClickListeners();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.ivAvatar);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvMomentsCount = findViewById(R.id.tvMomentsCount);
        tvReviewsCount = findViewById(R.id.tvReviewsCount);
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);
        cardEditProfile = findViewById(R.id.cardEditProfile);
        cardNotifications = findViewById(R.id.cardNotifications);
        cardPrivacy = findViewById(R.id.cardPrivacy);
        cardHelp = findViewById(R.id.cardHelp);
        cardAbout = findViewById(R.id.cardAbout);
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        
        if (user != null) {
            // Load from Firebase
            String name = user.getDisplayName();
            String email = user.getEmail();
            String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;

            if (name != null && !name.isEmpty()) {
                tvName.setText(name);
            } else {
                // Fallback to SharedPreferences
                String savedName = sharedPreferences.getString("user_name", "User");
                tvName.setText(savedName);
            }

            if (email != null) {
                tvEmail.setText(email);
            } else {
                String savedEmail = sharedPreferences.getString("user_email", "");
                tvEmail.setText(savedEmail);
            }

            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .into(ivAvatar);
            }
        } else {
            // Load from SharedPreferences
            String name = sharedPreferences.getString("user_name", "User");
            String email = sharedPreferences.getString("user_email", "");

            tvName.setText(name);
            tvEmail.setText(email);

            String photoUrl = sharedPreferences.getString("user_photo", null);
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .into(ivAvatar);
            }
        }
    }

    private void loadStats() {
        // Count moments
        dateViewModel.getAllMoments().observe(this, moments -> {
            if (moments != null) {
                tvMomentsCount.setText(String.valueOf(moments.size()));
            } else {
                tvMomentsCount.setText("0");
            }
        });

        // Count reviews
        dateViewModel.getAllMoments().observe(this, moments -> {
            if (moments != null) {
                int reviewCount = 0;
                for (var moment : moments) {
                    if (moment.getReview() != null && !moment.getReview().isEmpty()) {
                        reviewCount++;
                    }
                }
                tvReviewsCount.setText(String.valueOf(reviewCount));
            }
        });
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Edit Profile
        cardEditProfile.setOnClickListener(v -> {
            // TODO: Navigate to Edit Profile screen
        });

        // Notifications
        cardNotifications.setOnClickListener(v -> {
            // TODO: Navigate to Notifications settings
        });

        // Privacy & Security
        cardPrivacy.setOnClickListener(v -> {
            // TODO: Navigate to Privacy settings
        });

        // Help & Support
        cardHelp.setOnClickListener(v -> {
            // TODO: Navigate to Help screen
        });

        // About
        cardAbout.setOnClickListener(v -> {
            // TODO: Navigate to About screen
        });

        // Logout
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    logout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        // Clear Room database first (wait for it to complete before navigating)
        new Thread(() -> {
            DateDatabase db = DateDatabase.getDatabase(getApplicationContext());
            db.clearAllTables();
            runOnUiThread(() -> {
                // Sign out from Firebase
                FirebaseAuth.getInstance().signOut();

                // Google Sign Out
                com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this,
                        new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                                com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .build()
                ).signOut();

                // Clear SharedPreferences
                sharedPreferences.edit().clear().apply();

                // Navigate to Login
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }).start();
    }
}