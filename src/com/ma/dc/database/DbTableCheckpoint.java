package com.ma.dc.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public final class DbTableCheckpoint {

    private final static String CREATE_TABLE = "create table ";

    // ------------------------------------------------------
    // CheckpointTable
    static final String TABLE_NAME = "checkpoints";

    static final String COLUMN_INC_ID = "_id";
    static final String COLUMN_STRING_ID = "checkpoint_string_id";
    static final String COLUMN_REV = "checkpoint_rev";
    static final String COLUMN_NAME = "checkpoint_name";
    static final String COLUMN_ORDER_NR = "checkpoint_order_nr";
    static final String COLUMN_NEXT_MEASUREMENT_TIME = "checkpoint_next_measurement_time";
    static final String COLUMN_DESCRIPTION = "checkpoint_description";
    static final String COLUMN_TIME_DAYS = "checkpoint_time_days";
    static final String COLUMN_TIME_HOURS = "checkpoint_time_hours";
    static final String COLUMN_EXCLUDE_WEEKENDS = "checkpoint_exclude_weekends";
    static final String COLUMN_ERROR_TAG_1 = "checkpoint_error_tag_1";
    static final String COLUMN_ERROR_TAG_2 = "checkpoint_error_tag_2";
    static final String COLUMN_ERROR_TAG_3 = "checkpoint_error_tag_3";
    static final String COLUMN_ERROR_TAG_4 = "checkpoint_error_tag_4";
    static final String COLUMN_ACTION_TAG_1 = "checkpoint_action_tag_1";
    static final String COLUMN_ACTION_TAG_2 = "checkpoint_action_tag_2";
    static final String COLUMN_ACTION_TAG_3 = "checkpoint_action_tag_3";
    static final String COLUMN_ACTION_TAG_4 = "checkpoint_action_tag_4";

    static final String COLUMN_LATEST_MEASUREMENT_DATE = "checkpoint_latest_measurement_date";
    static final String COLUMN_LATEST_MEASUREMENT_VALUE = "checkpoint_latest_measurement_value";

    static final String COLUMN_IMAGE_FILENAME = "checkpoint_image_filename";
    static final String COLUMN_IMAGE_SIZE = "checkpoint_image_size";
    static final String COLUMN_DOWNLOAD_IMAGE = "checkpoint_download_image";

    private final static String CHECKPOINT_TABLE_CREATE = CREATE_TABLE + TABLE_NAME + " (" + COLUMN_INC_ID
            + " integer primary key autoincrement, " + COLUMN_STRING_ID + " text not null unique, " + COLUMN_REV
            + " text not null, " + COLUMN_NAME + " text not null, " + COLUMN_DESCRIPTION + " text not null, "
            + COLUMN_TIME_DAYS + " integer not null, " + COLUMN_TIME_HOURS + " STRING, " + COLUMN_EXCLUDE_WEEKENDS
            + " integer not null, " + COLUMN_ORDER_NR + " integer not null, " + COLUMN_NEXT_MEASUREMENT_TIME +
            " integer, "  
            + COLUMN_ERROR_TAG_1 + " text not null, " 
            + COLUMN_ERROR_TAG_2 + " text not null, " + COLUMN_ERROR_TAG_3
            + " text not null, " + COLUMN_ERROR_TAG_4 + " text not null, " + COLUMN_ACTION_TAG_1 + " text not null, "
            + COLUMN_ACTION_TAG_2 + " text not null, " + COLUMN_ACTION_TAG_3 + " text not null, " + COLUMN_ACTION_TAG_4
            + " text not null, " + COLUMN_LATEST_MEASUREMENT_DATE + " long, " + COLUMN_LATEST_MEASUREMENT_VALUE
            + " integer, " + COLUMN_IMAGE_FILENAME + " text, " + COLUMN_IMAGE_SIZE + " integer, "
            + COLUMN_DOWNLOAD_IMAGE + " integer);";

    static String[] allColumns() {
        final String[] available = { COLUMN_INC_ID, COLUMN_STRING_ID, COLUMN_REV, COLUMN_NAME, COLUMN_DESCRIPTION,
                COLUMN_TIME_DAYS, COLUMN_TIME_HOURS, COLUMN_EXCLUDE_WEEKENDS, COLUMN_ORDER_NR, COLUMN_NEXT_MEASUREMENT_TIME,
                COLUMN_ERROR_TAG_1, COLUMN_ERROR_TAG_2, COLUMN_ERROR_TAG_3, COLUMN_ERROR_TAG_4, COLUMN_ACTION_TAG_1,
                COLUMN_ACTION_TAG_2, COLUMN_ACTION_TAG_3, COLUMN_ACTION_TAG_4, COLUMN_LATEST_MEASUREMENT_DATE,
                COLUMN_LATEST_MEASUREMENT_VALUE, COLUMN_IMAGE_FILENAME, COLUMN_IMAGE_SIZE, COLUMN_DOWNLOAD_IMAGE };
        return available;
    }

    static void onCreate(final SQLiteDatabase database) {
        database.execSQL(CHECKPOINT_TABLE_CREATE);
    }

    static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(DbTableCheckpoint.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

    // This class cannot be instantiated
    private DbTableCheckpoint() {
    }
}
