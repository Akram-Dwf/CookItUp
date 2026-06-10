package com.example.cookitup.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.cookitup.R;
import com.example.cookitup.ui.favorite.FavoriteFragment;
import com.example.cookitup.ui.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("cookitup_prefs", android.content.Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_favorite) {
                selectedFragment = new FavoriteFragment();
            }
            if (selectedFragment != null) {
                return loadFragment(selectedFragment);
            }
            return false;
        });

        // Load default fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            if (fragment instanceof HomeFragment) {
                toolbar.setVisibility(android.view.View.GONE);
            } else if (fragment instanceof FavoriteFragment) {
                toolbar.setVisibility(android.view.View.VISIBLE);
                toolbar.setTitle(getString(R.string.nav_favorite));
                toolbar.setSubtitle("");
            }
            return true;
        }
        return false;
    }
}
