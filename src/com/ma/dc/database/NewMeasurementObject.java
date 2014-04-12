package com.ma.dc.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

public final class NewMeasurementObject {

    private final String checkpointStringId;
    private final long timestamp;
    private final String tag;
    private final int value;

    public NewMeasurementObject(final String checkpointStringId, final long timestamp, final String tag, final int value) {

        this.checkpointStringId = checkpointStringId;
        this.timestamp = timestamp;
        this.tag = tag;
        this.value = value;
    }

    // Returns new id
    public long storeToDatabase(ContentResolver contentResolver) {
        final ContentValues cv = new ContentValues();

        cv.put(DbTableMeasurement.COLUMN_CHECKPOINT, checkpointStringId);
        cv.put(DbTableMeasurement.COLUMN_DATE, timestamp);
        cv.put(DbTableMeasurement.COLUMN_TAG, tag);
        cv.put(DbTableMeasurement.COLUMN_VALUE, value);

        final Uri insertedUri = contentResolver.insert(DcContentProvider.MEASUREMENTS_URI, cv);

        return Long.parseLong(insertedUri.getPathSegments().get(1));

    }

    public int getValue() {
        return value;
    }
}
