package com.ma.dc.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DwtDatabaseHelper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "dwt.db";
    private final static int DATABASE_VERSION = 1;

    public DwtDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        DbTableCheckpoint.onCreate(db);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        DbTableCheckpoint.onUpgrade(db, oldVersion, newVersion);
    }
}