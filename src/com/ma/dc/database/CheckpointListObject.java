package com.ma.dc.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

public final class CheckpointListObject {
    public final static String[] PROJECTION = { DbTableCheckpoint.COLUMN_INC_ID, DbTableCheckpoint.COLUMN_NAME,
            DbTableCheckpoint.COLUMN_ORDER_NR, DbTableCheckpoint.COLUMN_TIME_DAYS, DbTableCheckpoint.COLUMN_TIME_HOURS,
            DbTableCheckpoint.COLUMN_EXCLUDE_WEEKENDS, DbTableCheckpoint.COLUMN_NEXT_MEASUREMENT_TIME,
            DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_DATE, DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_VALUE };

    private final long id;
    private final String name;
    private final int orderNr;
    private final int timeDays;
    private final int timeHours;
    private final boolean excludeWeekends;
    private final Long nextMeasurementTime;
    private final Long latestMeasurementTimestamp;
    private final Integer latestMeasurementValue;

    public CheckpointListObject(Cursor cursor) {
        final ContentValues cv = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, cv);

        id = cv.getAsLong(DbTableCheckpoint.COLUMN_INC_ID).longValue();
        name = cv.getAsString(DbTableCheckpoint.COLUMN_NAME);
        orderNr = cv.getAsInteger(DbTableCheckpoint.COLUMN_ORDER_NR).intValue();
        timeDays = cv.getAsInteger(DbTableCheckpoint.COLUMN_TIME_DAYS).intValue();
        timeHours = cv.getAsInteger(DbTableCheckpoint.COLUMN_TIME_HOURS).intValue();
        excludeWeekends = cv.getAsInteger(DbTableCheckpoint.COLUMN_EXCLUDE_WEEKENDS).intValue() > 0;

        // Can be null values
        latestMeasurementTimestamp = cv.getAsLong(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_DATE);
        latestMeasurementValue = cv.getAsInteger(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_VALUE);
        nextMeasurementTime = cv.getAsLong(DbTableCheckpoint.COLUMN_NEXT_MEASUREMENT_TIME);
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

    public int getTimeDays() {
        return timeDays;
    }

    public int getTimeHours() {
        return timeHours;
    }

    public boolean getExcludeWeekends() {
        return excludeWeekends;
    }
    
    public Long getNextMeasurementTime() {
        return nextMeasurementTime;
    }

    public Long getLatestMeasurementTimestamp() {
        return latestMeasurementTimestamp;
    }

    public Integer getLatestMeasurementValue() {
        return latestMeasurementValue;
    }

    public static String getColumnNextMeasurementTime() {
        return DbTableCheckpoint.COLUMN_NEXT_MEASUREMENT_TIME;
    }

    public static String getColumnOrderNr() {
        return DbTableCheckpoint.COLUMN_ORDER_NR;
    }
}
