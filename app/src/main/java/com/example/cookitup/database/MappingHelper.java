package com.example.cookitup.database;

import android.database.Cursor;

import com.example.cookitup.model.Meal;

import java.util.ArrayList;

public class MappingHelper {

    public static ArrayList<Meal> mapCursorToArrayList(Cursor notesCursor) {
        ArrayList<Meal> mealsList = new ArrayList<>();

        while (notesCursor.moveToNext()) {
            String idMeal = notesCursor.getString(notesCursor.getColumnIndexOrThrow(DatabaseContract.MealColumns.MEAL_ID));
            String strMeal = notesCursor.getString(notesCursor.getColumnIndexOrThrow(DatabaseContract.MealColumns.MEAL_NAME));
            String strMealThumb = notesCursor.getString(notesCursor.getColumnIndexOrThrow(DatabaseContract.MealColumns.THUMBNAIL));
            
            Meal meal = new Meal();
            meal.setIdMeal(idMeal);
            meal.setStrMeal(strMeal);
            meal.setStrMealThumb(strMealThumb);
            mealsList.add(meal);
        }

        return mealsList;
    }
}
