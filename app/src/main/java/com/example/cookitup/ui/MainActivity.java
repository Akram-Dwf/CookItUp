package com.example.cookitup.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cookitup.R;
import com.example.cookitup.ui.favorite.FavoriteFragment;
import com.example.cookitup.ui.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500;

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;
    private View splashOverlay;
    private View mainContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Terapkan tema sebelum super.onCreate agar tidak glitch
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("cookitup_prefs", android.content.Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        splashOverlay = findViewById(R.id.splash_overlay);
        mainContent = findViewById(R.id.main_content);

        // Tampilkan splash screen hanya saat pertama kali dibuka
        if (savedInstanceState == null) {
            showSplash();
        } else {
            // Lewati splash jika activity dibuat ulang (misal ganti tema)
            splashOverlay.setVisibility(View.GONE);
            mainContent.setVisibility(View.VISIBLE);
        }

        setupMainContent();
    }

    private void showSplash() {
        splashOverlay.setVisibility(View.VISIBLE);
        mainContent.setVisibility(View.GONE);

        ImageView logo = findViewById(R.id.splash_logo);
        TextView title = findViewById(R.id.splash_title);
        TextView subtitle = findViewById(R.id.splash_subtitle);

        // Animasi Logo: membesar dan muncul
        logo.setAlpha(0f);
        logo.setScaleX(0.5f);
        logo.setScaleY(0.5f);
        logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setStartDelay(200)
                .start();

        // Animasi Judul: naik ke atas dan muncul
        title.setAlpha(0f);
        title.setTranslationY(30f);
        title.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(600)
                .start();

        // Animasi Subjudul: naik ke atas dan muncul
        subtitle.setAlpha(0f);
        subtitle.setTranslationY(20f);
        subtitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(900)
                .start();

        // Pindah ke konten utama setelah jeda waktu (Crossfade)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            mainContent.setVisibility(View.VISIBLE);
            mainContent.setAlpha(0f);
            mainContent.animate().alpha(1f).setDuration(400).start();
            splashOverlay.animate().alpha(0f).setDuration(300).withEndAction(() ->
                    splashOverlay.setVisibility(View.GONE)
            ).start();
        }, SPLASH_DURATION);
    }

    private void setupMainContent() {
        viewPager = findViewById(R.id.view_pager);
        bottomNav = findViewById(R.id.bottom_navigation);

        // Setup ViewPager2 dengan FragmentStateAdapter
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 1) {
                    return new FavoriteFragment();
                }
                return new HomeFragment();
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        // Sinkronisasi geser ViewPager2 -> navigasi bawah
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                } else {
                    bottomNav.setSelectedItemId(R.id.nav_favorite);
                }
            }
        });

        // Sinkronisasi klik navigasi bawah -> geser ViewPager2
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                viewPager.setCurrentItem(0, true);
                return true;
            } else if (id == R.id.nav_favorite) {
                viewPager.setCurrentItem(1, true);
                return true;
            }
            return false;
        });
    }
}
