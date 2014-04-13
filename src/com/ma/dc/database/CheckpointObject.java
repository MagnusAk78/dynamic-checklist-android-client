package com.ma.dc.database;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

public final class CheckpointObject {
    public final static String[] PROJECTION = DbTableCheckpoint.allColumns();

    private final long id;
    private final String stringId;
    private final String rev;
    private final String name;
    private final String discription;
    private final int timeDays;
    private final int timeHours;
    private final boolean excludeWeekends;
    private final int orderNr;
    private final String errorTag1;
    private final String errorTag2;
    private final String errorTag3;
    private final String errorTag4;
    private final String actionTag1;
    private final String actionTag2;
    private final String actionTag3;
    private final String actionTag4;

    private Long nextMeasurementTime;
    private Long latestMeasurementTimestamp;
    private Integer latestMeasurementValue;

    private final String imageFilename;
    private final Integer imageFilesize;
    private Integer downloadImage;

    public CheckpointObject(Cursor cursor) {
        final ContentValues cv = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, cv);

        id = cv.getAsLong(DbTableCheckpoint.COLUMN_INC_ID).longValue();
        stringId = cv.getAsString(DbTableCheckpoint.COLUMN_STRING_ID);
        name = cv.getAsString(DbTableCheckpoint.COLUMN_NAME);
        orderNr = cv.getAsInteger(DbTableCheckpoint.COLUMN_ORDER_NR).intValue();
        timeDays = cv.getAsInteger(DbTableCheckpoint.COLUMN_TIME_DAYS).intValue();
        timeHours = cv.getAsInteger(DbTableCheckpoint.COLUMN_TIME_HOURS).intValue();
        excludeWeekends = cv.getAsInteger(DbTableCheckpoint.COLUMN_EXCLUDE_WEEKENDS).intValue() > 0;

        rev = cv.getAsString(DbTableCheckpoint.COLUMN_REV);
        discription = cv.getAsString(DbTableCheckpoint.COLUMN_DESCRIPTION);
        errorTag1 = cv.getAsString(DbTableCheckpoint.COLUMN_ERROR_TAG_1);
        errorTag2 = cv.getAsString(DbTableCheckpoint.COLUMN_ERROR_TAG_2);
        errorTag3 = cv.getAsString(DbTableCheckpoint.COLUMN_ERROR_TAG_3);
        errorTag4 = cv.getAsString(DbTableCheckpoint.COLUMN_ERROR_TAG_4);
        actionTag1 = cv.getAsString(DbTableCheckpoint.COLUMN_ACTION_TAG_1);
        actionTag2 = cv.getAsString(DbTableCheckpoint.COLUMN_ACTION_TAG_2);
        actionTag3 = cv.getAsString(DbTableCheckpoint.COLUMN_ACTION_TAG_3);
        actionTag4 = cv.getAsString(DbTableCheckpoint.COLUMN_ACTION_TAG_4);

        // Can be null values
        latestMeasurementTimestamp = cv.getAsLong(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_DATE);
        latestMeasurementValue = cv.getAsInteger(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_VALUE);
        nextMeasurementTime = cv.getAsLong(DbTableCheckpoint.COLUMN_NEXT_MEASUREMENT_TIME);

        imageFilename = cv.getAsString(DbTableCheckpoint.COLUMN_IMAGE_FILENAME);
        imageFilesize = cv.getAsInteger(DbTableCheckpoint.COLUMN_IMAGE_SIZE);
        downloadImage = cv.getAsInteger(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE);
    }

    public long getId() {
        return id;
    }

    public String getStringId() {
        return stringId;
    }

    public String getRev() {
        return rev;
    }

    public String getName() {
        return name;
    }

    public String getDiscription() {
        return discription;
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

    public int getOrderNr() {
        return orderNr;
    }

    public String getErrorTag1() {
        return errorTag1;
    }

    public String getErrorTag2() {
        return errorTag2;
    }

    public String getErrorTag3() {
        return errorTag3;
    }

    public String getErrorTag4() {
        return errorTag4;
    }

    public String getActionTag1() {
        return actionTag1;
    }

    public String getActionTag2() {
        return actionTag2;
    }

    public String getActionTag3() {
        return actionTag3;
    }

    public String getActionTag4() {
        return actionTag4;
    }

    public Long getLatestMeasurementTimestamp() {
        return latestMeasurementTimestamp;
    }

    public Integer getLatestMeasurementValue() {
        return latestMeasurementValue;
    }
    
    public Long getNextMeasurementTime() {
        return nextMeasurementTime;
    }
    
    String getImageFileName() {
        return imageFilename;
    }

    public String getFullImageFilePath() {
        return stringId + "/" + imageFilename;
    }

    public Integer getImageFilesize() {
        return imageFilesize;
    }

    public boolean getDownloadImage() {
        if (downloadImage == null) {
            return true;
        }
        return downloadImage.intValue() > 0;
    }

    public void updateLatestMeasurement(final ContentResolver contentResolver, final long latestMeasurementTimestamp,
            final int latestMeasurementValue) {
        
        long nextMeasurementTime = latestMeasurementTimestamp + (this.timeDays * 24 + this.timeHours) * 3600000;
        
        if(this.excludeWeekends) {
            final Calendar nextMeasurementCalendar = new GregorianCalendar();
            
            int dayOfWeek;
            do {
                nextMeasurementCalendar.setTimeInMillis(nextMeasurementTime);
            
                dayOfWeek = nextMeasurementCalendar.get(Calendar.DAY_OF_WEEK);
                if(dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    if(timeDays > 0) {
                        nextMeasurementTime += 24 * 3600000;
                    } else {
                        nextMeasurementTime += timeHours * 3600000;
                    }
                }
            }while(dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
        }

        final ContentValues cv = new ContentValues();

        cv.put(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_DATE, latestMeasurementTimestamp);
        cv.put(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_VALUE, latestMeasurementValue);
        cv.put(DbTableCheckpoint.COLUMN_NEXT_MEASUREMENT_TIME, nextMeasurementTime);

        int changedRows = contentResolver.update(Uri.parse(DcContentProvider.CHECKPOINTS_URI + "/" + id), cv, null,
                null);
        if (changedRows == 1) {
            this.latestMeasurementTimestamp = latestMeasurementTimestamp;
            this.latestMeasurementValue = latestMeasurementValue;
        }
    }

    public void updateDownloadImage(ContentResolver contentResolver, boolean downloadImage) {
        final ContentValues cv = new ContentValues();

        cv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, downloadImage);

        int changedRows = contentResolver.update(Uri.parse(DcContentProvider.CHECKPOINTS_URI + "/" + id), cv, null,
                null);
        if (changedRows == 1) {
            this.downloadImage = downloadImage ? Integer.valueOf(1) : Integer.valueOf(0);
        }
    }

    public int deleteFromDatabase(final Context context) {
        // Remove all measurements
        final String selection = DbTableMeasurement.COLUMN_CHECKPOINT + "=?";
        final String[] selectionArgs = { stringId };
        final Cursor cursor = context.getContentResolver().query(DcContentProvider.MEASUREMENTS_URI,
                MeasurementObject.PROJECTION, selection, selectionArgs, null);
        while (cursor.moveToNext()) {
            final MeasurementObject measurement = new MeasurementObject(cursor);
            measurement.deleteFromDatabase(context.getContentResolver());
        }
        
        cursor.close();

        deleteImageFile(context);

        // Delete checkpoint
        return context.getContentResolver().delete(Uri.parse(DcContentProvider.CHECKPOINTS_URI + "/" + id), null, null);
    }
    
    void deleteImage(final Context context) {
        deleteImageFile(context);
        updateDownloadImage(context.getContentResolver(), true);
    }
    
    private void deleteImageFile(final Context context) {
        // Remove local image file
        final File fileToDelete = new File(context.getFilesDir(), getFullImageFilePath());
        fileToDelete.delete();
    }
}
