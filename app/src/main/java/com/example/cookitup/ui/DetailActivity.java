package com.example.cookitup.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cookitup.R;
import com.example.cookitup.database.DatabaseContract;
import com.example.cookitup.database.MealHelper;
import com.example.cookitup.model.MealDetail;
import com.example.cookitup.model.MealDetailResponse;
import com.example.cookitup.network.ApiService;
import com.example.cookitup.network.RetrofitClient;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private ImageView ivThumbnail;
    private TextView tvName, tvInstructions, tvToolbarTitle;
    private LinearLayout layoutIngredients;
    private ChipGroup chipGroupTags;
    private Button btnFavorite;
    private MealHelper mealHelper;
    private MealDetail currentMealDetail;
    private boolean isFavorite = false;
    private View skeletonLayout;
    private View contentLayout;

    // Store ingredients JSON for local persistence
    private String ingredientsJson = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        skeletonLayout = findViewById(R.id.skeleton_layout);
        contentLayout = findViewById(R.id.content_layout);
        ivThumbnail = findViewById(R.id.iv_thumbnail);
        tvName = findViewById(R.id.tv_name);
        tvInstructions = findViewById(R.id.tv_instructions);
        layoutIngredients = findViewById(R.id.layout_ingredients);
        btnFavorite = findViewById(R.id.btn_favorite);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        chipGroupTags = findViewById(R.id.chip_group_tags);

        // Set up toolbar navigation without setSupportActionBar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Start shimmer animation on skeleton
        startShimmer();

        mealHelper = MealHelper.getInstance(this);

        String mealId = getIntent().getStringExtra("meal_id");
        String mealName = getIntent().getStringExtra("meal_name");

        // Set toolbar title from intent if available
        if (mealName != null && !mealName.isEmpty()) {
            tvToolbarTitle.setText(mealName);
        }

        if (mealId != null) {
            loadMealDetail(mealId);
        }

        btnFavorite.setOnClickListener(v -> {
            if (currentMealDetail != null) {
                toggleFavorite(currentMealDetail);
            }
        });
    }

    private void loadMealDetail(String mealId) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getMealDetail(mealId).enqueue(new Callback<MealDetailResponse>() {
            @Override
            public void onResponse(Call<MealDetailResponse> call, Response<MealDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getMeals() != null) {
                    currentMealDetail = response.body().getMeals().get(0);
                    displayMealDetail(currentMealDetail);
                    showContent();
                    checkIfFavorite(currentMealDetail.getIdMeal());
                } else {
                    // API returned empty — try local
                    loadFromLocalDatabase(mealId);
                }
            }

            @Override
            public void onFailure(Call<MealDetailResponse> call, Throwable t) {
                // Network failure — try loading from local SQLite
                loadFromLocalDatabase(mealId);
            }
        });
    }

    // Load dari SQLite jika sedang offline
    private void loadFromLocalDatabase(String mealId) {
        // Gunakan ExecutorService agar query SQLite berjalan di background thread (Syarat Lab)
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // Handler untuk kembali ke Main UI Thread (Syarat Lab)
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            mealHelper.open();
            Cursor cursor = mealHelper.queryById(mealId);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(DatabaseContract.MealColumns.MEAL_NAME);
                int thumbIdx = cursor.getColumnIndex(DatabaseContract.MealColumns.THUMBNAIL);
                int instrIdx = cursor.getColumnIndex(DatabaseContract.MealColumns.INSTRUCTIONS);
                int ingIdx = cursor.getColumnIndex(DatabaseContract.MealColumns.INGREDIENTS);

                String name = nameIdx >= 0 ? cursor.getString(nameIdx) : "";
                String thumb = thumbIdx >= 0 ? cursor.getString(thumbIdx) : "";
                String instructions = instrIdx >= 0 ? cursor.getString(instrIdx) : "";
                String ingredientsData = ingIdx >= 0 ? cursor.getString(ingIdx) : null;

                cursor.close();
                mealHelper.close();

                // Build a MealDetail from local data
                MealDetail localDetail = new MealDetail();
                localDetail.setIdMeal(mealId);
                localDetail.setStrMeal(name);
                localDetail.setStrMealThumb(thumb);
                localDetail.setStrInstructions(instructions);

                handler.post(() -> {
                    currentMealDetail = localDetail;
                    tvName.setText(name);
                    tvToolbarTitle.setText(name);
                    tvInstructions.setText(instructions);

                    if (thumb != null && !thumb.isEmpty()) {
                        Picasso.get().load(thumb).into(ivThumbnail);
                    }

                    // Populate ingredients from saved JSON
                    if (ingredientsData != null && !ingredientsData.isEmpty()) {
                        ingredientsJson = ingredientsData;
                        populateIngredientsFromJson(ingredientsData);
                        populateTagsFromJson(ingredientsData);
                    }

                    showContent();
                    isFavorite = true;
                    updateFavoriteButtonUI();

                    Snackbar.make(btnFavorite,
                            getString(R.string.msg_loaded_offline),
                            Snackbar.LENGTH_SHORT).show();
                });
            } else {
                if (cursor != null) cursor.close();
                mealHelper.close();
                handler.post(() -> {
                    showContent();
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.msg_detail_failed),
                            Snackbar.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayMealDetail(MealDetail detail) {
        tvName.setText(detail.getStrMeal());
        tvToolbarTitle.setText(detail.getStrMeal());
        tvInstructions.setText(detail.getStrInstructions());
        Picasso.get().load(detail.getStrMealThumb()).into(ivThumbnail);
        populateIngredients(detail);
        populateTags(detail);

        // Build and cache ingredients JSON for saving
        ingredientsJson = buildIngredientsJson(detail);
    }

    /**
     * Convert MealDetail's 20 ingredient+measure pairs into a JSON string.
     */
    private String buildIngredientsJson(MealDetail d) {
        try {
            JSONArray arr = new JSONArray();
            String[][] pairs = {
                    {d.getStrIngredient1(), d.getStrMeasure1()},
                    {d.getStrIngredient2(), d.getStrMeasure2()},
                    {d.getStrIngredient3(), d.getStrMeasure3()},
                    {d.getStrIngredient4(), d.getStrMeasure4()},
                    {d.getStrIngredient5(), d.getStrMeasure5()},
                    {d.getStrIngredient6(), d.getStrMeasure6()},
                    {d.getStrIngredient7(), d.getStrMeasure7()},
                    {d.getStrIngredient8(), d.getStrMeasure8()},
                    {d.getStrIngredient9(), d.getStrMeasure9()},
                    {d.getStrIngredient10(), d.getStrMeasure10()},
                    {d.getStrIngredient11(), d.getStrMeasure11()},
                    {d.getStrIngredient12(), d.getStrMeasure12()},
                    {d.getStrIngredient13(), d.getStrMeasure13()},
                    {d.getStrIngredient14(), d.getStrMeasure14()},
                    {d.getStrIngredient15(), d.getStrMeasure15()},
                    {d.getStrIngredient16(), d.getStrMeasure16()},
                    {d.getStrIngredient17(), d.getStrMeasure17()},
                    {d.getStrIngredient18(), d.getStrMeasure18()},
                    {d.getStrIngredient19(), d.getStrMeasure19()},
                    {d.getStrIngredient20(), d.getStrMeasure20()}
            };

            for (String[] pair : pairs) {
                String ing = pair[0];
                String meas = pair[1];
                if (ing != null && !ing.trim().isEmpty()) {
                    JSONObject obj = new JSONObject();
                    obj.put("ingredient", ing.trim());
                    obj.put("measure", meas != null ? meas.trim() : "");
                    arr.put(obj);
                }
            }
            return arr.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    /**
     * Parse ingredients JSON and populate the ingredient list UI.
     */
    private void populateIngredientsFromJson(String json) {
        layoutIngredients.removeAllViews();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String ing = obj.getString("ingredient");
                String meas = obj.optString("measure", "");

                TextView tv = new TextView(this);
                String measureText = (!meas.isEmpty()) ? meas + " - " : "";
                tv.setText("• " + measureText + ing);
                tv.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.on_surface));
                tv.setTextSize(16);
                tv.setPadding(0, 0, 0, 16);
                layoutIngredients.addView(tv);
            }
        } catch (Exception e) {
            // Silently fail
        }
    }

    /**
     * Parse ingredients JSON and populate tag chips (first 3).
     */
    private void populateTagsFromJson(String json) {
        chipGroupTags.removeAllViews();
        try {
            JSONArray arr = new JSONArray(json);
            int count = Math.min(arr.length(), 3);
            for (int i = 0; i < count; i++) {
                JSONObject obj = arr.getJSONObject(i);
                String tag = obj.getString("ingredient");
                Chip chip = new Chip(this);
                chip.setText(tag);
                chip.setChipBackgroundColorResource(R.color.secondary_container);
                chip.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.on_secondary_container));
                chip.setChipStrokeWidth(0);
                chip.setClickable(false);
                chipGroupTags.addView(chip);
            }
        } catch (Exception e) {
            // Silently fail
        }
    }

    private void populateTags(MealDetail mealDetail) {
        chipGroupTags.removeAllViews();
        String[] sampleIngredients = {
                mealDetail.getStrIngredient1(),
                mealDetail.getStrIngredient2(),
                mealDetail.getStrIngredient3()
        };
        for (String tag : sampleIngredients) {
            if (tag != null && !tag.trim().isEmpty()) {
                Chip chip = new Chip(this);
                chip.setText(tag.trim());
                chip.setChipBackgroundColorResource(R.color.secondary_container);
                chip.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.on_secondary_container));
                chip.setChipStrokeWidth(0);
                chip.setClickable(false);
                chipGroupTags.addView(chip);
            }
        }
    }

    private void checkIfFavorite(String mealId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            mealHelper.open();
            Cursor cursor = mealHelper.queryById(mealId);
            boolean exists = (cursor != null && cursor.getCount() > 0);
            if (cursor != null) cursor.close();
            mealHelper.close();

            handler.post(() -> {
                isFavorite = exists;
                updateFavoriteButtonUI();
            });
        });
    }

    private void updateFavoriteButtonUI() {
        if (isFavorite) {
            btnFavorite.setText(getString(R.string.btn_unfavorite));
            ((com.google.android.material.button.MaterialButton) btnFavorite)
                    .setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(this, R.color.error)));
        } else {
            btnFavorite.setText(getString(R.string.btn_favorite));
            ((com.google.android.material.button.MaterialButton) btnFavorite)
                    .setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(this, R.color.primary)));
        }
    }

    private void toggleFavorite(MealDetail mealDetail) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            mealHelper.open();
            if (isFavorite) {
                long result = mealHelper.deleteById(mealDetail.getIdMeal());
                mealHelper.close();
                handler.post(() -> {
                    if (result > 0) {
                        isFavorite = false;
                        updateFavoriteButtonUI();
                        // Snackbar with UNDO
                        Snackbar.make(btnFavorite, getString(R.string.msg_removed), Snackbar.LENGTH_LONG)
                                .setAction(getString(R.string.snackbar_undo), v -> {
                                    // Re-insert the meal on UNDO
                                    reInsertFavorite(mealDetail);
                                })
                                .show();
                    }
                });
            } else {
                ContentValues values = new ContentValues();
                values.put(DatabaseContract.MealColumns.MEAL_ID, mealDetail.getIdMeal());
                values.put(DatabaseContract.MealColumns.MEAL_NAME, mealDetail.getStrMeal());
                values.put(DatabaseContract.MealColumns.THUMBNAIL, mealDetail.getStrMealThumb());
                values.put(DatabaseContract.MealColumns.INSTRUCTIONS, mealDetail.getStrInstructions());
                // Save ingredients JSON for offline access
                if (ingredientsJson != null) {
                    values.put(DatabaseContract.MealColumns.INGREDIENTS, ingredientsJson);
                }

                long result = mealHelper.insert(values);
                mealHelper.close();

                handler.post(() -> {
                    if (result > 0) {
                        isFavorite = true;
                        updateFavoriteButtonUI();
                        Snackbar.make(btnFavorite, getString(R.string.msg_saved), Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(btnFavorite, getString(R.string.msg_save_failed), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void populateIngredients(MealDetail mealDetail) {
        layoutIngredients.removeAllViews();
        String[][] ingredients = {
                {mealDetail.getStrIngredient1(), mealDetail.getStrMeasure1()},
                {mealDetail.getStrIngredient2(), mealDetail.getStrMeasure2()},
                {mealDetail.getStrIngredient3(), mealDetail.getStrMeasure3()},
                {mealDetail.getStrIngredient4(), mealDetail.getStrMeasure4()},
                {mealDetail.getStrIngredient5(), mealDetail.getStrMeasure5()},
                {mealDetail.getStrIngredient6(), mealDetail.getStrMeasure6()},
                {mealDetail.getStrIngredient7(), mealDetail.getStrMeasure7()},
                {mealDetail.getStrIngredient8(), mealDetail.getStrMeasure8()},
                {mealDetail.getStrIngredient9(), mealDetail.getStrMeasure9()},
                {mealDetail.getStrIngredient10(), mealDetail.getStrMeasure10()},
                {mealDetail.getStrIngredient11(), mealDetail.getStrMeasure11()},
                {mealDetail.getStrIngredient12(), mealDetail.getStrMeasure12()},
                {mealDetail.getStrIngredient13(), mealDetail.getStrMeasure13()},
                {mealDetail.getStrIngredient14(), mealDetail.getStrMeasure14()},
                {mealDetail.getStrIngredient15(), mealDetail.getStrMeasure15()},
                {mealDetail.getStrIngredient16(), mealDetail.getStrMeasure16()},
                {mealDetail.getStrIngredient17(), mealDetail.getStrMeasure17()},
                {mealDetail.getStrIngredient18(), mealDetail.getStrMeasure18()},
                {mealDetail.getStrIngredient19(), mealDetail.getStrMeasure19()},
                {mealDetail.getStrIngredient20(), mealDetail.getStrMeasure20()}
        };

        for (String[] item : ingredients) {
            String ing = item[0];
            String meas = item[1];
            if (ing != null && !ing.trim().isEmpty()) {
                TextView tv = new TextView(this);
                String measureText = (meas != null && !meas.trim().isEmpty()) ? meas.trim() + " - " : "";
                tv.setText("• " + measureText + ing.trim());
                tv.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.on_surface));
                tv.setTextSize(16);
                tv.setPadding(0, 0, 0, 16);
                layoutIngredients.addView(tv);
            }
        }
    }

    /**
     * Start pulse animation on all skeleton placeholder views.
     */
    private void startShimmer() {
        if (skeletonLayout instanceof android.view.ViewGroup) {
            startAnimationsRecursive((android.view.ViewGroup) skeletonLayout);
        }
    }

    private void startAnimationsRecursive(android.view.ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getBackground() instanceof AnimationDrawable) {
                ((AnimationDrawable) child.getBackground()).start();
            }
            if (child instanceof android.view.ViewGroup) {
                startAnimationsRecursive((android.view.ViewGroup) child);
            }
        }
    }

    /**
     * Crossfade from skeleton placeholder to real content.
     */
    private void showContent() {
        contentLayout.setAlpha(0f);
        contentLayout.setVisibility(View.VISIBLE);
        contentLayout.animate().alpha(1f).setDuration(300).start();
        skeletonLayout.animate().alpha(0f).setDuration(200).withEndAction(() ->
                skeletonLayout.setVisibility(View.GONE)
        ).start();
    }

    /**
     * Re-insert a favorite meal (used by UNDO action in Snackbar).
     */
    private void reInsertFavorite(MealDetail mealDetail) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.MealColumns.MEAL_ID, mealDetail.getIdMeal());
            values.put(DatabaseContract.MealColumns.MEAL_NAME, mealDetail.getStrMeal());
            values.put(DatabaseContract.MealColumns.THUMBNAIL, mealDetail.getStrMealThumb());
            values.put(DatabaseContract.MealColumns.INSTRUCTIONS, mealDetail.getStrInstructions());
            if (ingredientsJson != null) {
                values.put(DatabaseContract.MealColumns.INGREDIENTS, ingredientsJson);
            }

            mealHelper.open();
            long result = mealHelper.insert(values);
            mealHelper.close();

            handler.post(() -> {
                if (result > 0) {
                    isFavorite = true;
                    updateFavoriteButtonUI();
                    Snackbar.make(btnFavorite, getString(R.string.msg_saved), Snackbar.LENGTH_SHORT).show();
                }
            });
        });
    }
}
