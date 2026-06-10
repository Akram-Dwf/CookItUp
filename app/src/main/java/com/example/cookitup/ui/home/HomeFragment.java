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
import androidx.core.content.ContextCompat;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private EditText etIngredient;
    private Button btnAddIngredient, btnSearch, btnRefresh;
    private ChipGroup chipGroupIngredients;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private MealAdapter adapter;
    private Switch switchTheme;
    private SharedPreferences sharedPreferences;

    private ArrayList<String> ingredients = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etIngredient = view.findViewById(R.id.et_ingredient);
        btnAddIngredient = view.findViewById(R.id.btn_add_ingredient);
        chipGroupIngredients = view.findViewById(R.id.chip_group_ingredients);
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

        btnAddIngredient.setOnClickListener(v -> {
            String ingredient = etIngredient.getText().toString().trim();
            if (!ingredient.isEmpty()) {
                if (!ingredients.contains(ingredient)) {
                    ingredients.add(ingredient);
                    addChip(ingredient);
                }
                etIngredient.setText("");
            }
        });

        btnSearch.setOnClickListener(v -> {
            if (ingredients.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.hint_ingredient), Toast.LENGTH_SHORT).show();
            } else {
                searchMeals();
            }
        });

        btnRefresh.setOnClickListener(v -> {
            if (!ingredients.isEmpty()) {
                searchMeals();
            }
        });
    }

    private void addChip(String ingredient) {
        Chip chip = new Chip(getContext());
        chip.setText(ingredient);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.light_green);
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_green));
        chip.setOnCloseIconClickListener(v -> {
            chipGroupIngredients.removeView(chip);
            ingredients.remove(ingredient);
        });
        chipGroupIngredients.addView(chip);
    }

    private void searchMeals() {
        progressBar.setVisibility(View.VISIBLE);
        btnRefresh.setVisibility(View.GONE);
        recyclerView.setAdapter(null);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        HashMap<String, Meal> uniqueMeals = new HashMap<>();
        final int[] completedCalls = {0};
        final boolean[] hasError = {false};

        for (String ingredient : ingredients) {
            Call<MealResponse> call = apiService.getMealsByIngredient(ingredient);
            call.enqueue(new Callback<MealResponse>() {
                @Override
                public void onResponse(Call<MealResponse> call, Response<MealResponse> response) {
                    completedCalls[0]++;
                    if (response.isSuccessful() && response.body() != null) {
                        List<Meal> meals = response.body().getMeals();
                        if (meals != null) {
                            for (Meal meal : meals) {
                                uniqueMeals.put(meal.getIdMeal(), meal);
                            }
                        }
                    }
                    checkCompletion(completedCalls[0], hasError[0], uniqueMeals);
                }

                @Override
                public void onFailure(Call<MealResponse> call, Throwable t) {
                    completedCalls[0]++;
                    hasError[0] = true;
                    checkCompletion(completedCalls[0], hasError[0], uniqueMeals);
                }
            });
        }
    }

    private void checkCompletion(int completedCount, boolean hasError, HashMap<String, Meal> uniqueMeals) {
        if (completedCount == ingredients.size()) {
            progressBar.setVisibility(View.GONE);
            if (hasError && uniqueMeals.isEmpty()) {
                btnRefresh.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), getString(R.string.msg_no_internet), Toast.LENGTH_SHORT).show();
            } else if (uniqueMeals.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.msg_no_data), Toast.LENGTH_SHORT).show();
            } else {
                List<Meal> combinedList = new ArrayList<>(uniqueMeals.values());
                adapter = new MealAdapter(new ArrayList<>(combinedList));
                recyclerView.setAdapter(adapter);

                // Wait, MealAdapter might use an interface for clicks or we set it directly if it has setOnItemClickCallback
                try {
                    java.lang.reflect.Method method = adapter.getClass().getMethod("setOnItemClickCallback", MealAdapter.OnItemClickCallback.class);
                    method.invoke(adapter, (MealAdapter.OnItemClickCallback) data -> {
                        Intent intent = new Intent(getActivity(), DetailActivity.class);
                        intent.putExtra("meal_id", data.getIdMeal());
                        intent.putExtra("meal_name", data.getStrMeal());
                        startActivity(intent);
                    });
                } catch (Exception e) {
                    // Fallback or ignore if the method doesn't exist via reflection, but the original code had it directly:
                    adapter.setOnItemClickCallback(data -> {
                        Intent intent = new Intent(getActivity(), DetailActivity.class);
                        intent.putExtra("meal_id", data.getIdMeal());
                        intent.putExtra("meal_name", data.getStrMeal());
                        startActivity(intent);
                    });
                }
            }
        }
    }
}


