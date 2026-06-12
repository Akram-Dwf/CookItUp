package com.example.cookitup.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.cookitup.database.DatabaseContract.MealColumns;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "dbcookitup";
    private static final int DATABASE_VERSION = 2;

    private static final String SQL_CREATE_TABLE_MEAL = String.format("CREATE TABLE %s"
                    + " (%s INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " %s TEXT NOT NULL," +
                    " %s TEXT NOT NULL," +
                    " %s TEXT NOT NULL," +
                    " %s TEXT," +
                    " %s TEXT)",
            DatabaseContract.TABLE_NAME,
            MealColumns._ID,
            MealColumns.MEAL_ID,
            MealColumns.MEAL_NAME,
            MealColumns.THUMBNAIL,
            MealColumns.INSTRUCTIONS,
            MealColumns.INGREDIENTS
    );

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_MEAL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add ingredients column for existing users
            db.execSQL("ALTER TABLE " + DatabaseContract.TABLE_NAME +
                    " ADD COLUMN " + MealColumns.INGREDIENTS + " TEXT");
        }
    }
}
