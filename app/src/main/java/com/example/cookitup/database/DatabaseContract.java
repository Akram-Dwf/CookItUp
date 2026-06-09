package com.example.cookitup.database;

import android.provider.BaseColumns;

public class DatabaseContract {
    public static final String TABLE_NAME = "favorite_meal";

    public static final class MealColumns implements BaseColumns {
        public static final String MEAL_ID = "meal_id";
        public static final String MEAL_NAME = "meal_name";
        public static final String THUMBNAIL = "thumbnail";
        public static final String INSTRUCTIONS = "instructions";
    }
}
