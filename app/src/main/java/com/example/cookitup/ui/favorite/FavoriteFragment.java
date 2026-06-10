package com.example.cookitup.ui.favorite;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookitup.R;
import com.example.cookitup.adapter.FavoriteAdapter;
import com.example.cookitup.database.MappingHelper;
import com.example.cookitup.database.MealHelper;
import com.example.cookitup.model.Meal;
import com.example.cookitup.ui.DetailActivity;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyStateLayout;
    private FavoriteAdapter adapter;
    private MealHelper mealHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view_favorite);
        emptyStateLayout = view.findViewById(R.id.layout_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoriteAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickCallback(data -> {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("meal_id", data.getIdMeal());
            intent.putExtra("meal_name", data.getStrMeal());
            startActivity(intent);
        });

        mealHelper = MealHelper.getInstance(getContext());

        android.widget.ImageView btnThemeToggle = view.findViewById(R.id.btn_theme_toggle);
        android.content.SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("cookitup_prefs", android.content.Context.MODE_PRIVATE);
        btnThemeToggle.setOnClickListener(v -> {
            boolean currentMode = sharedPreferences.getBoolean("dark_mode", false);
            boolean newMode = !currentMode;
            android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", newMode);
            editor.apply();

            if (newMode) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            mealHelper.open();
            ArrayList<Meal> favorites = MappingHelper.mapCursorToArrayList(mealHelper.queryAll());
            mealHelper.close();

            handler.post(() -> {
                adapter.setData(favorites);
                if (favorites.size() == 0) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                }
            });
        });
    }
}
