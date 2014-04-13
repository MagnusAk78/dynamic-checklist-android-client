package com.ma.dc.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.ma.dc.Common;
import com.ma.dc.data.couch.CouchObjCheckpoint;
import com.ma.dc.util.LogHelper;

public final class DbCheckpointHelper {

    public static int storeCheckpointFromNetwork(final ContentResolver contentReslover,
            final CouchObjCheckpoint checkpointCouchObject) {
        LogHelper.logDebug(DbCheckpointHelper.class, Common.LOG_TAG_DB, "createCheckpointCvFromNetwork");
        
        //First find if it's already in the database
        CheckpointObject oldCheckpoint = null;
        String selection = DbTableCheckpoint.COLUMN_STRING_ID + "=?";
        String[] selectionArgs = {checkpointCouchObject.getId()};
        final Cursor cursor = contentReslover.query(DcContentProvider.CHECKPOINTS_URI, CheckpointObject.PROJECTION, selection, 
                selectionArgs, null);
        if(cursor.moveToFirst()) {
            oldCheckpoint = new CheckpointObject(cursor);
        }
        
        cursor.close();

        if (oldCheckpoint != null) {
            LogHelper.logDebug(DbCheckpointHelper.class, Common.LOG_TAG_DB, "createCheckpointCvFromNetwork",
                    "oldCheckpoint != null");

            // Same revision mean that nothing has changed.
            if (oldCheckpoint.getRev().compareTo(checkpointCouchObject.getRev()) == 0) {
                // This has not changed
                return 0;
            }
        }

        final ContentValues cv = new ContentValues();

        cv.put(DbTableCheckpoint.COLUMN_STRING_ID, checkpointCouchObject.getId());
        cv.put(DbTableCheckpoint.COLUMN_REV, checkpointCouchObject.getRev());
        cv.put(DbTableCheckpoint.COLUMN_NAME, checkpointCouchObject.getName());
        cv.put(DbTableCheckpoint.COLUMN_DESCRIPTION, checkpointCouchObject.getDescription());
        cv.put(DbTableCheckpoint.COLUMN_TIME_DAYS, checkpointCouchObject.getTimeDays());
        cv.put(DbTableCheckpoint.COLUMN_TIME_HOURS, checkpointCouchObject.getTimeHours());
        cv.put(DbTableCheckpoint.COLUMN_EXCLUDE_WEEKENDS, checkpointCouchObject.getExcludeWeekends());
        cv.put(DbTableCheckpoint.COLUMN_ORDER_NR, checkpointCouchObject.getOrderNr());

        cv.put(DbTableCheckpoint.COLUMN_ERROR_TAG_1, checkpointCouchObject.getErrorTag1());
        cv.put(DbTableCheckpoint.COLUMN_ERROR_TAG_2, checkpointCouchObject.getErrorTag2());
        cv.put(DbTableCheckpoint.COLUMN_ERROR_TAG_3, checkpointCouchObject.getErrorTag3());
        cv.put(DbTableCheckpoint.COLUMN_ERROR_TAG_4, checkpointCouchObject.getErrorTag4());

        cv.put(DbTableCheckpoint.COLUMN_ACTION_TAG_1, checkpointCouchObject.getActionTag1());
        cv.put(DbTableCheckpoint.COLUMN_ACTION_TAG_2, checkpointCouchObject.getActionTag2());
        cv.put(DbTableCheckpoint.COLUMN_ACTION_TAG_3, checkpointCouchObject.getActionTag3());
        cv.put(DbTableCheckpoint.COLUMN_ACTION_TAG_4, checkpointCouchObject.getActionTag4());

        if (checkpointCouchObject.getNrOfAttachments() > 0) {
            LogHelper.logDebug(DbCheckpointHelper.class, Common.LOG_TAG_DB, "createCheckpointCvFromNetwork",
                    "checkpointCouchObject.getNrOfAttachments() > 0");

            String filename = checkpointCouchObject.getAttachmentFilenames().get(0);
            cv.put(DbTableCheckpoint.COLUMN_IMAGE_FILENAME, filename);
            int filenameLength = checkpointCouchObject.getAttachmentInfo(filename).getLength();
            cv.put(DbTableCheckpoint.COLUMN_IMAGE_SIZE, filenameLength);

            if (oldCheckpoint != null) {
                LogHelper.logDebug(DbCheckpointHelper.class, Common.LOG_TAG_DB, "createCheckpointCvFromNetwork",
                        "oldCheckpoint != null");

                final String oldFileName = oldCheckpoint.getImageFileName();
                final Integer oldFileSize = oldCheckpoint.getImageFilesize();

                if (oldFileName != null && oldFileName.compareTo(filename) == 0 && oldFileSize == filenameLength) {
                    LogHelper.logDebug(DbCheckpointHelper.class, Common.LOG_TAG_DB, "createCheckpointCvFromNetwork",
                            "cv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, Boolean.FALSE)");
                    cv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, Integer.valueOf(0));
                } else {
                    LogHelper.logDebug(DbCheckpointHelper.class, Common.LOG_TAG_DB, "createCheckpointCvFromNetwork",
                            "cv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, Boolean.TRUE)");
                    cv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, Integer.valueOf(1));
                }
            } else {
                LogHelper.logDebug(DbCheckpointHelper.class, Common.LOG_TAG_DB, "createCheckpointCvFromNetwork",
                        "cv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, Boolean.TRUE)");
                cv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, Integer.valueOf(1));
            }
        } else {
            cv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, Integer.valueOf(0));
        }

        if (oldCheckpoint != null) {
            return contentReslover.update(Uri.parse(DcContentProvider.CHECKPOINTS_URI + "/" + oldCheckpoint.getId()),
                    cv, null, null);
        } else {
            contentReslover.insert(DcContentProvider.CHECKPOINTS_URI, cv);
            return 1;
        }
    }
    
    public static int removeAllCheckpointsExceptIdList(final Context context, 
            final List<String> checkpointIdList) {
        Cursor cursorToCheckpintsToDelete;
        if(checkpointIdList == null || checkpointIdList.size() == 0) {
            cursorToCheckpintsToDelete = context.getContentResolver().query(DcContentProvider.CHECKPOINTS_URI, CheckpointObject.PROJECTION, null, null, null);
        }
        
        StringBuilder selectionBuilder = new StringBuilder();
        for(int i=0; i<checkpointIdList.size(); i++) {
            selectionBuilder.append(DbTableCheckpoint.COLUMN_STRING_ID);
            selectionBuilder.append("!='");
            selectionBuilder.append(checkpointIdList.get(i));
            selectionBuilder.append("'");
            if((i+1) < checkpointIdList.size()) {
                selectionBuilder.append(" and ");
            }
        }
        
        int nr = 0;
        cursorToCheckpintsToDelete = context.getContentResolver().query(DcContentProvider.CHECKPOINTS_URI, CheckpointObject.PROJECTION, selectionBuilder.toString(), null, null);
        
        while(cursorToCheckpintsToDelete.moveToNext()) {
            new CheckpointObject(cursorToCheckpintsToDelete).deleteFromDatabase(context);
            nr++;
        }
        cursorToCheckpintsToDelete.close();
        return nr;
    }

    public static List<CheckpointObject> getAllCheckpointsInDatabase(final ContentResolver contentResolver) {
        final Cursor checkpointsCursor = contentResolver.query(DcContentProvider.CHECKPOINTS_URI,
                CheckpointObject.PROJECTION, null, null, null);
        final ArrayList<CheckpointObject> checkpointsFromDatabase = new ArrayList<CheckpointObject>(
                checkpointsCursor.getCount());
        while (checkpointsCursor.moveToNext()) {
            checkpointsFromDatabase.add(new CheckpointObject(checkpointsCursor));
        }
        checkpointsCursor.close();
        return checkpointsFromDatabase;
    }
    
    public static void deleteImage(final Context context, final long id) {
        CheckpointObject checkpointObject = null;
        final Cursor cursor = context.getContentResolver().query(Uri.parse(DcContentProvider.CHECKPOINTS_URI + "/" + id), 
                CheckpointObject.PROJECTION, null, null, null);
        if(cursor.moveToFirst()) {
            checkpointObject = new CheckpointObject(cursor);
        }
        cursor.close();
        if(checkpointObject != null) {
            checkpointObject.deleteImage(context);
        }
    }
    
    public static CheckpointObject findCheckpointByStringId(final ContentResolver contentResolver, final String stringId) {
        CheckpointObject checkpointObject = null;
        final String selection = DbTableCheckpoint.COLUMN_STRING_ID + "=?";
        final String[] selectionArgs = { stringId };
        final Cursor cursor = contentResolver.query(DcContentProvider.CHECKPOINTS_URI, CheckpointObject.PROJECTION, selection, 
                selectionArgs, null);
        if(cursor.moveToFirst()) {
            checkpointObject = new CheckpointObject(cursor);
        }
        
        cursor.close();
        
        return checkpointObject;
    }
    
}
