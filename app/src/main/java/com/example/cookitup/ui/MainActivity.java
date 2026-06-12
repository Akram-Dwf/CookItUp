package com.example.cookitup.ui;

import android.os.Bundle;

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

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MUST apply theme BEFORE super.onCreate to prevent flash/glitch
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("cookitup_prefs", android.content.Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.view_pager);
        bottomNav = findViewById(R.id.bottom_navigation);

        // Set up ViewPager2 with adapter
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

        // Sync ViewPager2 swipe → BottomNav selection
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

        // Sync BottomNav tap → ViewPager2 page
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
