package com.oof.dwt.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public final class DbTableCheckpoint {
    
    private final static String CREATE_TABLE = "create table ";

    // ------------------------------------------------------
    // CheckpointTable
    public static final String TABLE_NAME = "checkpoints";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_REV = "rev";
    public static final String COLUMN_NAME = "checkpoint_key_name";
    public static final String COLUMN_DESCRIPTION = "checkpoint_key_description";
    public static final String COLUMN_ACTIVE = "checkpoint_key_active";
    public static final String COLUMN_UPDATES = "checkpoint_key_updates";
    public static final String COLUMN_TIME_PERIOD = "checkpoint_key_time_period";
    public static final String COLUMN_START_TIME = "checkpoint_key_start_time";
    public static final String COLUMN_START_DAY = "checkpoint_key_start_day";
    public static final String COLUMN_INCLUDE_WEEKENDS = "checkpoint_key_include_weekends";
    public static final String COLUMN_ORDER_NR = "checkpoint_key_order_nr";
    public static final String COLUMN_ERROR_TAG_1 = "checkpoint_key_error_tag_1";
    public static final String COLUMN_ERROR_TAG_2 = "checkpoint_key_error_tag_2";
    public static final String COLUMN_ERROR_TAG_3 = "checkpoint_key_error_tag_3";
    public static final String COLUMN_ERROR_TAG_4 = "checkpoint_key_error_tag_4";
    public static final String COLUMN_ACTION_TAG_1 = "checkpoint_key_action_tag_1";
    public static final String COLUMN_ACTION_TAG_2 = "checkpoint_key_action_tag_2";
    public static final String COLUMN_ACTION_TAG_3 = "checkpoint_key_action_tag_3";
    public static final String COLUMN_ACTION_TAG_4 = "checkpoint_key_action_tag_4";
    
    public static final String COLUMN_IMAGE_FILENAME = "checkpoint_key_image_filename";
    public static final String COLUMN_IMAGE_SIZE = "checkpoint_key_image_size";
    public static final String COLUMN_DOWNLOAD_IMAGE = "checkpoint_key_download_image";

    public static final String COLUMN_LATEST_MEASUREMENT_DATE = "checkpoint_key_latest_measurement_date";
    public static final String COLUMN_LATEST_MEASUREMENT_VALUE = "checkpoint_key_latest_measurement_value";
    public static final String COLUMN_LATEST_MEASUREMENT_TAG = "checkpoint_key_latest_measurement_tag";
    public static final String COLUMN_LATEST_MEASUREMENT_SYNCED = "checkpoint_key_latest_measurement_synced";
    
    private final static String CHECKPOINT_TABLE_CREATE = CREATE_TABLE + TABLE_NAME + " (" 
    		+ COLUMN_NAME + " TEXT, " + COLUMN_ID + " TEXT, " + COLUMN_REV + " TEXT, " 
    		+ COLUMN_DESCRIPTION + " TEXT, " + COLUMN_ACTIVE + " INTEGER, " 
    		+ COLUMN_UPDATES + " INTEGER, " + COLUMN_TIME_PERIOD + " STRING, " 
    		+ COLUMN_START_TIME + " INTEGER, " + COLUMN_START_DAY + " INTEGER, " 
    		+ COLUMN_INCLUDE_WEEKENDS + " INTEGER, " + COLUMN_ORDER_NR + " INTEGER, " 
    		+ COLUMN_ERROR_TAG_1 + " TEXT, " + COLUMN_ERROR_TAG_2 + " TEXT, " 
    		+ COLUMN_ERROR_TAG_3 + " TEXT, " + COLUMN_ERROR_TAG_4 + " TEXT, " 
    		+ COLUMN_ACTION_TAG_1 + " TEXT, " + COLUMN_ACTION_TAG_2 + " TEXT, " 
    		+ COLUMN_ACTION_TAG_3 + " TEXT, " + COLUMN_ACTION_TAG_4 + " TEXT, " 
    		+ COLUMN_IMAGE_FILENAME + " TEXT, " + COLUMN_IMAGE_SIZE + " INTEGER, " 
    		+ COLUMN_DOWNLOAD_IMAGE + " INTEGER, " + COLUMN_LATEST_MEASUREMENT_DATE + " LONG, " 
    		+ COLUMN_LATEST_MEASUREMENT_VALUE + " INTEGER, " + COLUMN_LATEST_MEASUREMENT_TAG + " TEXT, " 
    		+ COLUMN_LATEST_MEASUREMENT_SYNCED + " INTEGER);";

    public static String[] allColumns() {
        final String[] available = { COLUMN_ID, COLUMN_REV, COLUMN_NAME, COLUMN_DESCRIPTION, COLUMN_ACTIVE,
        		COLUMN_UPDATES, COLUMN_TIME_PERIOD, COLUMN_START_TIME, COLUMN_START_DAY, 
        		COLUMN_INCLUDE_WEEKENDS, COLUMN_ORDER_NR,COLUMN_ERROR_TAG_1, COLUMN_ERROR_TAG_2,
        		COLUMN_ERROR_TAG_3, COLUMN_ERROR_TAG_4, COLUMN_ACTION_TAG_1, COLUMN_ACTION_TAG_2,
        		COLUMN_ACTION_TAG_3, COLUMN_ACTION_TAG_4, COLUMN_IMAGE_FILENAME, COLUMN_IMAGE_SIZE, 
        		COLUMN_DOWNLOAD_IMAGE, COLUMN_LATEST_MEASUREMENT_DATE, COLUMN_LATEST_MEASUREMENT_VALUE, 
        		COLUMN_LATEST_MEASUREMENT_TAG, COLUMN_LATEST_MEASUREMENT_SYNCED};
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
