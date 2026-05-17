package com.example.usmentz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.usmentz.database.DateDatabase;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UsmentzPrefs";

    private ImageView ivAvatar;
    private TextView tvUserName, tvUserEmail;
    private SwitchMaterial switchNotifications, switchDarkMode;
    private MaterialButton btnLogout;

    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.ivAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        btnLogout = findViewById(R.id.btnLogout);

        // Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Load from Firebase
            String name = user.getDisplayName();
            String email = user.getEmail();
            String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;

            if (name != null && !name.isEmpty()) {
                tvUserName.setText(name);
            } else {
                String savedName = sharedPreferences.getString("user_name", "User");
                tvUserName.setText(savedName);
            }

            if (email != null) {
                tvUserEmail.setText(email);
            } else {
                String savedEmail = sharedPreferences.getString("user_email", "");
                tvUserEmail.setText(savedEmail);
            }

            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .into(ivAvatar);
            }

            // Show logout button
            btnLogout.setVisibility(View.VISIBLE);
        } else {
            // Guest user
            String savedName = sharedPreferences.getString("user_name", "Guest User");
            String savedEmail = sharedPreferences.getString("user_email", "Tap to sign in");

            tvUserName.setText(savedName);
            tvUserEmail.setText(savedEmail);

            String photoUrl = sharedPreferences.getString("user_photo", null);
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .into(ivAvatar);
            }

            // Hide logout for guests
            btnLogout.setVisibility(View.GONE);
        }

        // Load preferences
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications", true);
        switchNotifications.setChecked(notificationsEnabled);
    }

    private void setupClickListeners() {
        // Profile card - Go to Profile
        View cardProfile = findViewById(R.id.cardProfile);
        if (cardProfile != null) {
            cardProfile.setOnClickListener(v -> {
                if (mAuth.getCurrentUser() != null) {
                    startActivity(new Intent(this, ProfileActivity.class));
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }
            });
        }

        // Notifications switch
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("notifications", isChecked).apply();
            Toast.makeText(this, isChecked ? "Notifications enabled" : "Notifications disabled", Toast.LENGTH_SHORT).show();
        });

        // Dark mode switch
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();
            // TODO: Implement dark mode theme change
            Toast.makeText(this, isChecked ? "Dark mode enabled" : "Dark mode disabled", Toast.LENGTH_SHORT).show();
        });

        // Language row
        View rowLanguage = findViewById(R.id.rowLanguage);
        if (rowLanguage != null) {
            rowLanguage.setOnClickListener(v -> {
                Toast.makeText(this, "Language settings coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Export row
        View rowExport = findViewById(R.id.rowExport);
        if (rowExport != null) {
            rowExport.setOnClickListener(v -> {
                Toast.makeText(this, "Export feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Backup row
        View rowBackup = findViewById(R.id.rowBackup);
        if (rowBackup != null) {
            rowBackup.setOnClickListener(v -> {
                Toast.makeText(this, "Backup & Sync coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Privacy row
        View rowPrivacy = findViewById(R.id.rowPrivacy);
        if (rowPrivacy != null) {
            rowPrivacy.setOnClickListener(v -> {
                Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show();
            });
        }

        // Terms row
        View rowTerms = findViewById(R.id.rowTerms);
        if (rowTerms != null) {
            rowTerms.setOnClickListener(v -> {
                Toast.makeText(this, "Terms of Service", Toast.LENGTH_SHORT).show();
            });
        }

        // Logout button
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> logout())
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