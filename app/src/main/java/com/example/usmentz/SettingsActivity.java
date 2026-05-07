package com.example.usmentz;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        LinearLayout cardExport = findViewById(R.id.cardExport);
        if (cardExport != null) {
            cardExport.setOnClickListener(v -> {
                exportData();
            });
        }
    }

    private void exportData() {
        Toast.makeText(this, "Export feature coming soon!", Toast.LENGTH_SHORT).show();
    }
}