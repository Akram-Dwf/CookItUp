package com.example.cookitup.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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

    private static final int MAX_RESULTS = 30;


    private EditText etIngredient;
    private android.widget.ImageButton btnAddIngredient;
    private Button btnSearch, btnRefresh;
    private ChipGroup chipGroupIngredients;
    private LinearLayout skeletonContainer;
    private LinearLayout emptyHomeLayout;
    private RecyclerView recyclerView;
    private MealAdapter adapter;
    private ImageView btnThemeToggle;
    private SharedPreferences sharedPreferences;
    
    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etIngredient = view.findViewById(R.id.et_ingredient);
        btnAddIngredient = view.findViewById(R.id.btn_add_chip);
        chipGroupIngredients = view.findViewById(R.id.chip_group_ingredients);
        btnSearch = view.findViewById(R.id.btn_search);
        btnRefresh = view.findViewById(R.id.btn_refresh);
        skeletonContainer = view.findViewById(R.id.skeleton_container);
        emptyHomeLayout = view.findViewById(R.id.layout_empty_home);
        recyclerView = view.findViewById(R.id.recycler_view);

        // Optimasi performa RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setHasFixedSize(false);

        // Buat adapter 1x dan gunakan ulang agar switch mode mulus
        adapter = new MealAdapter(new ArrayList<>());
        adapter.setOnItemClickCallback(data -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("meal_id", data.getIdMeal());
                intent.putExtra("meal_name", data.getStrMeal());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        sharedPreferences = requireActivity().getSharedPreferences("cookitup_prefs", Context.MODE_PRIVATE);
        
        // Pakai scope activity agar ViewModel bertahan saat ganti fragment/tema
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        btnThemeToggle = view.findViewById(R.id.btn_theme_toggle);
        btnThemeToggle.setOnClickListener(v -> {
            // Simpan posisi scroll sebelum ganti tema
            if (recyclerView.getLayoutManager() != null) {
                viewModel.recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
            }

            boolean currentMode = sharedPreferences.getBoolean("dark_mode", false);
            boolean newMode = !currentMode;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", newMode);
            editor.apply();

            if (newMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Kembalikan daftar bahan dari ViewModel
        for (String ing : viewModel.ingredients) {
            addChip(ing);
        }

        btnAddIngredient.setOnClickListener(v -> {
            String ingredient = etIngredient.getText().toString().trim().toLowerCase();
            if (!ingredient.isEmpty()) {
                if (!viewModel.ingredients.contains(ingredient)) {
                    viewModel.ingredients.add(ingredient);
                    addChip(ingredient);
                }
                etIngredient.setText("");
            }
        });

        btnSearch.setOnClickListener(v -> {
            if (viewModel.ingredients.isEmpty()) {
                viewModel.searchResults.setValue(new ArrayList<>());
                Toast.makeText(getContext(), getString(R.string.hint_ingredient), Toast.LENGTH_SHORT).show();
            } else {
                searchMeals();
            }
        });

        btnRefresh.setOnClickListener(v -> {
            if (!viewModel.ingredients.isEmpty()) {
                searchMeals();
            }
        });

        // Pantau data ViewModel
        viewModel.isSearching.observe(getViewLifecycleOwner(), isSearching -> {
            if (isSearching) {
                showSkeleton();
                btnRefresh.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                emptyHomeLayout.setVisibility(View.GONE);
            } else {
                hideSkeleton();
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.searchResults.observe(getViewLifecycleOwner(), meals -> {
            if (meals != null) {
                // Tampilkan/sembunyikan pesan kosong
                if (meals.isEmpty() && viewModel.ingredients.isEmpty()) {
                    emptyHomeLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyHomeLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                // Gunakan ulang adapter, ganti datanya saja
                adapter.setData(new ArrayList<>(meals));

                // Kembalikan posisi scroll setelah ganti tema
                if (viewModel.recyclerViewState != null && recyclerView.getLayoutManager() != null) {
                    recyclerView.getLayoutManager().onRestoreInstanceState(viewModel.recyclerViewState);
                    viewModel.recyclerViewState = null;
                }
            }
        });
    }

    /**
     * Tampilkan skeleton loading
     */
    private void showSkeleton() {
        skeletonContainer.setVisibility(View.VISIBLE);
        startAnimationsRecursive(skeletonContainer);
    }

    /**
     * Sembunyikan skeleton
     */
    private void hideSkeleton() {
        skeletonContainer.setVisibility(View.GONE);
    }

    private void startAnimationsRecursive(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getBackground() instanceof AnimationDrawable) {
                ((AnimationDrawable) child.getBackground()).start();
            }
            if (child instanceof ViewGroup) {
                startAnimationsRecursive((ViewGroup) child);
            }
        }
    }

    private void addChip(String ingredient) {
        Chip chip = new Chip(getContext());
        chip.setText(ingredient);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.secondary_container);
        chip.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.on_secondary_container));
        chip.setOnCloseIconClickListener(v -> {
            chipGroupIngredients.removeView(chip);
            viewModel.ingredients.remove(ingredient);
            if (viewModel.ingredients.isEmpty()) {
                viewModel.searchResults.setValue(new ArrayList<>());
            }
        });
        chipGroupIngredients.addView(chip);
    }

    private void searchMeals() {
        viewModel.isSearching.setValue(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        HashMap<String, Meal> uniqueMeals = new HashMap<>();
        HashMap<String, Integer> mealOccurrences = new HashMap<>();
        final int[] completedCalls = {0};
        final boolean[] hasError = {false};

        for (String ingredient : viewModel.ingredients) {
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
                                mealOccurrences.put(meal.getIdMeal(), mealOccurrences.getOrDefault(meal.getIdMeal(), 0) + 1);
                            }
                        }
                    }
                    checkCompletion(completedCalls[0], hasError[0], uniqueMeals, mealOccurrences);
                }

                @Override
                public void onFailure(Call<MealResponse> call, Throwable t) {
                    completedCalls[0]++;
                    hasError[0] = true;
                    checkCompletion(completedCalls[0], hasError[0], uniqueMeals, mealOccurrences);
                }
            });
        }
    }

    private void checkCompletion(int completedCount, boolean hasError, HashMap<String, Meal> uniqueMeals, HashMap<String, Integer> mealOccurrences) {
        if (completedCount == viewModel.ingredients.size()) {
            viewModel.isSearching.setValue(false);
            
            List<Meal> intersectionList = new ArrayList<>();
            for (String id : uniqueMeals.keySet()) {
                if (mealOccurrences.get(id) == viewModel.ingredients.size()) {
                    intersectionList.add(uniqueMeals.get(id));
                }
            }

            // Batasi hasil maksimal untuk performa
            if (intersectionList.size() > MAX_RESULTS) {
                intersectionList = intersectionList.subList(0, MAX_RESULTS);
            }

            if (hasError && intersectionList.isEmpty()) {
                btnRefresh.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), getString(R.string.msg_no_internet), Toast.LENGTH_SHORT).show();
            } else if (intersectionList.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.msg_no_data), Toast.LENGTH_SHORT).show();
            }
            
            viewModel.searchResults.setValue(intersectionList);
        }
    }
}
