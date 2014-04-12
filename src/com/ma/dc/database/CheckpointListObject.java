package com.ma.dc.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

public final class CheckpointListObject {
    public final static String[] PROJECTION = { DbTableCheckpoint.COLUMN_INC_ID, DbTableCheckpoint.COLUMN_NAME,
            DbTableCheckpoint.COLUMN_ORDER_NR, DbTableCheckpoint.COLUMN_START_TIME, DbTableCheckpoint.COLUMN_START_DAY,
            DbTableCheckpoint.COLUMN_TIME_PERIOD, DbTableCheckpoint.COLUMN_UPDATES,
            DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_DATE, DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_VALUE };

    private final long id;
    private final String name;
    private final int orderNr;
    private final int startTime;
    private final int startDay;
    private final String timePeriod;
    private final int updates;
    private final Long latestMeasurementTimestamp;
    private final Integer latestMeasurementValue;

    public CheckpointListObject(Cursor cursor) {
        final ContentValues cv = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, cv);

        id = cv.getAsLong(DbTableCheckpoint.COLUMN_INC_ID).longValue();
        name = cv.getAsString(DbTableCheckpoint.COLUMN_NAME);
        orderNr = cv.getAsInteger(DbTableCheckpoint.COLUMN_ORDER_NR).intValue();
        startTime = cv.getAsInteger(DbTableCheckpoint.COLUMN_START_TIME).intValue();
        startDay = cv.getAsInteger(DbTableCheckpoint.COLUMN_START_DAY).intValue();
        timePeriod = cv.getAsString(DbTableCheckpoint.COLUMN_TIME_PERIOD);
        updates = cv.getAsInteger(DbTableCheckpoint.COLUMN_UPDATES).intValue();

        // Can be null values
        latestMeasurementTimestamp = cv.getAsLong(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_DATE);
        latestMeasurementValue = cv.getAsInteger(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_VALUE);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getOrderNr() {
        return orderNr;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getStartDay() {
        return startDay;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public int getUpdates() {
        return updates;
    }

    public Long getLatestMeasurementTimestamp() {
        return latestMeasurementTimestamp;
    }

    public Integer getLatestMeasurementValue() {
        return latestMeasurementValue;
    }
}
