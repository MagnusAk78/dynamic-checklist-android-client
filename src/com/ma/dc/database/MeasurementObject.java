package com.ma.dc.database;

import com.ma.dc.data.couch.CouchObjMeasurement;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

public final class MeasurementObject {
    public final static String[] PROJECTION = { DbTableMeasurement.COLUMN_ID, DbTableMeasurement.COLUMN_CHECKPOINT,
            DbTableMeasurement.COLUMN_DATE, DbTableMeasurement.COLUMN_TAG, DbTableMeasurement.COLUMN_VALUE };

    private final long id;
    private final String checkpointStringId;
    private final long timestamp;
    private final String tag;
    private final int value;

    public MeasurementObject(Cursor cursor) {
        final ContentValues cv = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, cv);

        id = cv.getAsLong(DbTableMeasurement.COLUMN_ID).longValue();
        checkpointStringId = cv.getAsString(DbTableMeasurement.COLUMN_CHECKPOINT);
        timestamp = cv.getAsLong(DbTableMeasurement.COLUMN_DATE).longValue();
        tag = cv.getAsString(DbTableMeasurement.COLUMN_TAG);
        value = cv.getAsInteger(DbTableMeasurement.COLUMN_DATE).intValue();
    }

    public long getId() {
        return id;
    }

    public String getCheckpointStringId() {
        return checkpointStringId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTag() {
        return tag;
    }

    public int getValue() {
        return value;
    }

    public CouchObjMeasurement createCouchObjMeasurement() {
        return new CouchObjMeasurement(checkpointStringId, value, tag, timestamp);
    }

    public int deleteFromDatabase(final ContentResolver contentResolver) {
        return contentResolver.delete(Uri.parse(DcContentProvider.MEASUREMENTS_URI + "/" + id), null, null);
    }
}
