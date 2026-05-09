package com.example.usmentz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fullscreen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_splash);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        Button btnLogin = findViewById(R.id.btnLogin);

        // Get Started → Go to Onboarding
        btnGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(this, OnboardingActivity.class));
        });

        // Already have account → Go to Login
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        // Auto-navigate if already logged in
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            new Handler().postDelayed(() -> {
                goToHome();
            }, SPLASH_DELAY);
        }
    }

    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}