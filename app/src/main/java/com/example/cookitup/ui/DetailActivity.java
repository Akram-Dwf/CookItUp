package com.example.cookitup.ui;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cookitup.R;
import com.example.cookitup.database.DatabaseContract;
import com.example.cookitup.database.MealHelper;
import com.example.cookitup.model.MealDetail;
import com.example.cookitup.model.MealDetailResponse;
import com.example.cookitup.network.ApiService;
import com.example.cookitup.network.RetrofitClient;
import com.squareup.picasso.Picasso;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private ImageView ivThumbnail;
    private TextView tvName, tvInstructions;
    private Button btnFavorite;
    private MealHelper mealHelper;
    private MealDetail currentMealDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ivThumbnail = findViewById(R.id.iv_thumbnail);
        tvName = findViewById(R.id.tv_name);
        tvInstructions = findViewById(R.id.tv_instructions);
        btnFavorite = findViewById(R.id.btn_favorite);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        mealHelper = MealHelper.getInstance(this);

        String mealId = getIntent().getStringExtra("meal_id");
        if (mealId != null) {
            loadMealDetail(mealId);
        }

        btnFavorite.setOnClickListener(v -> {
            if (currentMealDetail != null) {
                saveToFavorite(currentMealDetail);
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
                    tvInstructions.setText(currentMealDetail.getStrInstructions());
                    Picasso.get().load(currentMealDetail.getStrMealThumb()).into(ivThumbnail);
                }
            }

            @Override
            public void onFailure(Call<MealDetailResponse> call, Throwable t) {
                Toast.makeText(DetailActivity.this, "Gagal mengambil data detail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToFavorite(MealDetail mealDetail) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            mealHelper.open();
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.MealColumns.MEAL_ID, mealDetail.getIdMeal());
            values.put(DatabaseContract.MealColumns.MEAL_NAME, mealDetail.getStrMeal());
            values.put(DatabaseContract.MealColumns.THUMBNAIL, mealDetail.getStrMealThumb());
            values.put(DatabaseContract.MealColumns.INSTRUCTIONS, mealDetail.getStrInstructions());

            long result = mealHelper.insert(values);
            mealHelper.close();

            handler.post(() -> {
                if (result > 0) {
                    Toast.makeText(DetailActivity.this, "Resep disimpan!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DetailActivity.this, "Gagal menyimpan resep", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
