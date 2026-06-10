package com.example.cookitup.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

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
                    tvName.setText(currentMealDetail.getStrMeal());
                    tvToolbarTitle.setText(currentMealDetail.getStrMeal());
                    tvInstructions.setText(currentMealDetail.getStrInstructions());
                    Picasso.get().load(currentMealDetail.getStrMealThumb()).into(ivThumbnail);
                    populateIngredients(currentMealDetail);
                    populateTags(currentMealDetail);
                    checkIfFavorite(currentMealDetail.getIdMeal());
                }
            }

            @Override
            public void onFailure(Call<MealDetailResponse> call, Throwable t) {
                Toast.makeText(DetailActivity.this, getString(R.string.msg_detail_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateTags(MealDetail mealDetail) {
        chipGroupTags.removeAllViews();
        // Use first 3 non-null ingredients as tag labels
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
                        Toast.makeText(DetailActivity.this, getString(R.string.msg_removed), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                ContentValues values = new ContentValues();
                values.put(DatabaseContract.MealColumns.MEAL_ID, mealDetail.getIdMeal());
                values.put(DatabaseContract.MealColumns.MEAL_NAME, mealDetail.getStrMeal());
                values.put(DatabaseContract.MealColumns.THUMBNAIL, mealDetail.getStrMealThumb());
                values.put(DatabaseContract.MealColumns.INSTRUCTIONS, mealDetail.getStrInstructions());

                long result = mealHelper.insert(values);
                mealHelper.close();

                handler.post(() -> {
                    if (result > 0) {
                        isFavorite = true;
                        updateFavoriteButtonUI();
                        Toast.makeText(DetailActivity.this, getString(R.string.msg_saved), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DetailActivity.this, getString(R.string.msg_save_failed), Toast.LENGTH_SHORT).show();
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
}
