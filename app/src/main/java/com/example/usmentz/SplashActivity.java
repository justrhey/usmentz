package com.example.usmentz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 1500; // 1.5 seconds
    private static final String PREFS_NAME = "UsmentzPrefs";
    private static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";
    
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fullscreen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_splash);
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        Button btnLogin = findViewById(R.id.btnLogin);

        // Get Started -> Go to Onboarding (skip if already completed)
        btnGetStarted.setOnClickListener(v -> {
            boolean onboardingDone = sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETE, false);
            if (onboardingDone) {
                startActivity(new Intent(this, RegisterActivity.class));
            } else {
                startActivity(new Intent(this, OnboardingActivity.class));
            }
        });

        // Already have account -> Go to Login
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        // Check for existing user session
        checkUserSession();
    }
    
    private void checkUserSession() {
        // First check if we have a local session saved
        boolean wasLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);
        String savedUserId = sharedPreferences.getString("user_id", null);
        
        // Also check Firebase Auth state
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        
        Log.d(TAG, "Local session: wasLoggedIn=" + wasLoggedIn + ", userId=" + savedUserId);
        Log.d(TAG, "Firebase session: " + (firebaseUser != null ? firebaseUser.getUid() : "null"));
        
        if (firebaseUser != null) {
            // User is authenticated via Firebase - this persists across app reinstalls
            // Firebase Auth tokens are stored in SharedPrefs by the Firebase SDK
            Log.d(TAG, "User authenticated via Firebase, userId: " + firebaseUser.getUid());
            
            // Update local prefs to ensure consistency
            if (!wasLoggedIn || savedUserId == null) {
                sharedPreferences.edit()
                        .putString("user_id", firebaseUser.getUid())
                        .putString("user_email", firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "")
                        .putString("user_name", firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "")
                        .putBoolean("is_logged_in", true)
                        .apply();
            }
            
            // Navigate to Home after brief delay
            new Handler().postDelayed(() -> {
                goToHome();
            }, SPLASH_DELAY);
        } else if (wasLoggedIn && savedUserId != null) {
            // Local session exists but Firebase session is lost
            // This can happen if auth token expired or was cleared
            // Clear local session and require re-login
            Log.w(TAG, "Local session exists but Firebase session lost - clearing session");
            sharedPreferences.edit()
                    .clear()
                    .apply();
            
            // Stay on splash screen for user to login
        }
        // If neither local nor Firebase session, stay on splash screen
    }

    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
