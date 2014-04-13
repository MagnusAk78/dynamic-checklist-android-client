package com.ma.dc.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

class DbTableMeasurement {
    private final static String CREATE_TABLE = "create table ";

    // ------------------------------------------------------
    // CheckpointTable
    static final String TABLE_NAME = "measurements";

    static final String COLUMN_ID = "_id";
    static final String COLUMN_DATE = "measurement_date";
    static final String COLUMN_CHECKPOINT = "measurement_checkpoint_id";
    static final String COLUMN_VALUE = "measurement_value";
    static final String COLUMN_TAG = "measurement_tag";

    private final static String MEASUREMENT_TABLE_CREATE = CREATE_TABLE + TABLE_NAME + " (" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_DATE + " long not null, " + COLUMN_CHECKPOINT
            + " text not null, " + COLUMN_VALUE + " integer not null, " + COLUMN_TAG + " text);";

    static String[] allColumns() {
        final String[] available = { COLUMN_CHECKPOINT, COLUMN_DATE, COLUMN_VALUE, COLUMN_TAG };
        return available;
    }

    static void onCreate(final SQLiteDatabase database) {
        database.execSQL(MEASUREMENT_TABLE_CREATE);
    }

    static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(DbTableMeasurement.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

    // This class cannot be instantiated
    private DbTableMeasurement() {
    }
}
