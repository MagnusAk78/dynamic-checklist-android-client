package com.ma.dc.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

class DcDatabaseHelper extends SQLiteOpenHelper {

    final static String DATABASE_NAME = "dc.db";
    final static int DATABASE_VERSION = 1;

    DcDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        DbTableCheckpoint.onCreate(db);
        DbTableMeasurement.onCreate(db);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        DbTableCheckpoint.onUpgrade(db, oldVersion, newVersion);
        DbTableMeasurement.onUpgrade(db, oldVersion, newVersion);
    }
}
