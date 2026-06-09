package com.example.cookitup.network;

import com.example.cookitup.model.MealDetailResponse;
import com.example.cookitup.model.MealResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("filter.php")
    Call<MealResponse> getMealsByIngredient(@Query("i") String ingredient);

    @GET("lookup.php")
    Call<MealDetailResponse> getMealDetail(@Query("i") String mealId);

    @GET("random.php")
    Call<MealDetailResponse> getRandomMeal();
}
