package com.example.icnew;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "myapp.db";
    private static final int DATABASE_VERSION = 1;

    // Constructor
    public MyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables here
        db.execSQL("CREATE TABLE IF NOT EXISTS my_table ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "image_path TEXT,"
                + "date TEXT,"
                + "analysis_result TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Upgrade logic here
        db.execSQL("DROP TABLE IF EXISTS analysis");
        onCreate(db);
    }
}
