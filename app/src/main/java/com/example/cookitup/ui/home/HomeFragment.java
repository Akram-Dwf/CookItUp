package com.example.cookitup.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookitup.R;
import com.example.cookitup.adapter.MealAdapter;
import com.example.cookitup.model.Meal;
import com.example.cookitup.model.MealResponse;
import com.example.cookitup.network.ApiService;
import com.example.cookitup.network.RetrofitClient;
import com.example.cookitup.ui.DetailActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private EditText etIngredient;
    private Button btnSearch, btnRefresh;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private MealAdapter adapter;
    private Switch switchTheme;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etIngredient = view.findViewById(R.id.et_ingredient);
        btnSearch = view.findViewById(R.id.btn_search);
        btnRefresh = view.findViewById(R.id.btn_refresh);
        progressBar = view.findViewById(R.id.progress_bar);
        recyclerView = view.findViewById(R.id.recycler_view);
        switchTheme = view.findViewById(R.id.switch_theme);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedPreferences = requireActivity().getSharedPreferences("cookitup_prefs", Context.MODE_PRIVATE);

        // Load theme preference
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        switchTheme.setChecked(isDarkMode);
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Load last ingredient
        String lastIngredient = sharedPreferences.getString("last_ingredients", "");
        if (!lastIngredient.isEmpty()) {
            etIngredient.setText(lastIngredient);
        }

        btnSearch.setOnClickListener(v -> {
            String ingredient = etIngredient.getText().toString().trim();
            if (!ingredient.isEmpty()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("last_ingredients", ingredient);
                editor.apply();
                searchMeals(ingredient);
            } else {
                Toast.makeText(getContext(), "Please enter an ingredient", Toast.LENGTH_SHORT).show();
            }
        });

        btnRefresh.setOnClickListener(v -> {
            String ingredient = etIngredient.getText().toString().trim();
            if (!ingredient.isEmpty()) {
                searchMeals(ingredient);
            }
        });
    }

    private void searchMeals(String ingredient) {
        progressBar.setVisibility(View.VISIBLE);
        btnRefresh.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<MealResponse> call = apiService.getMealsByIngredient(ingredient);

        call.enqueue(new Callback<MealResponse>() {
            @Override
            public void onResponse(Call<MealResponse> call, Response<MealResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Meal> meals = response.body().getMeals();
                    if (meals != null) {
                        adapter = new MealAdapter(new ArrayList<>(meals));
                        recyclerView.setAdapter(adapter);

                        adapter.setOnItemClickCallback(data -> {
                            Intent intent = new Intent(getActivity(), DetailActivity.class);
                            intent.putExtra("meal_id", data.getIdMeal());
                            intent.putExtra("meal_name", data.getStrMeal());
                            startActivity(intent);
                        });
                    } else {
                        Toast.makeText(getContext(), "No meals found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MealResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnRefresh.setVisibility(View.VISIBLE); // Tampilkan tombol refresh
                Toast.makeText(getContext(), "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
