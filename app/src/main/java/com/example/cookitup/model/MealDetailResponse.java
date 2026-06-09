package com.example.cookitup.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MealDetailResponse {
    @SerializedName("meals")
    private List<MealDetail> meals;

    public List<MealDetail> getMeals() {
        return meals;
    }

    public void setMeals(List<MealDetail> meals) {
        this.meals = meals;
    }
}
