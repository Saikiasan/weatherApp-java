package com.example.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button btnContinue = findViewById(R.id.btn_continue);

        btnContinue.setOnClickListener(v -> {
            // Save that user has visited
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("isFirstRun", false).apply();

            // Move to MainActivity
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
        });
    }
}
