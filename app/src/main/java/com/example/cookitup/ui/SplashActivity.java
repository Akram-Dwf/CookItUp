package com.example.cookitup.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.cookitup.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before rendering
        android.content.SharedPreferences prefs = getSharedPreferences("cookitup_prefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.splash_logo);
        TextView title = findViewById(R.id.splash_title);
        TextView subtitle = findViewById(R.id.splash_subtitle);

        // Logo scale + fade animation
        AnimationSet logoAnim = new AnimationSet(true);
        ScaleAnimation scale = new ScaleAnimation(
                0.5f, 1f, 0.5f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(800);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(800);
        logoAnim.addAnimation(scale);
        logoAnim.addAnimation(fadeIn);
        logo.startAnimation(logoAnim);

        // Title slide up + fade
        AlphaAnimation titleFade = new AlphaAnimation(0f, 1f);
        titleFade.setDuration(600);
        titleFade.setStartOffset(400);
        title.startAnimation(titleFade);

        // Subtitle fade
        AlphaAnimation subtitleFade = new AlphaAnimation(0f, 1f);
        subtitleFade.setDuration(600);
        subtitleFade.setStartOffset(700);
        subtitle.startAnimation(subtitleFade);

        // Navigate to main after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}
