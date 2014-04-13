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

    /**
     * @return New Id or -1
     */
    public long storeToDatabase(final ContentResolver contentResolver) {        
        //Add this to the checkpoint as the latest measurement
        final CheckpointObject checkpointObject = DbCheckpointHelper.
                findCheckpointByStringId(contentResolver, checkpointStringId);
        if(checkpointObject == null) {
            //Something is wrong
            return -1;
        }
        
        checkpointObject.updateLatestMeasurement(contentResolver, timestamp, value);
        
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
