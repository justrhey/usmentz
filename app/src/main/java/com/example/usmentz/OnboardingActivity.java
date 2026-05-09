package com.example.usmentz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Button btnContinue = findViewById(R.id.btnContinue);
        Button btnSkip = findViewById(R.id.btnSkip);

        btnContinue.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        btnSkip.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}