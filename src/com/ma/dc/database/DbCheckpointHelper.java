package com.ma.dc.database;

import java.io.File;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.ma.dc.Common;
import com.ma.dc.contentprovider.DcContentProvider;
import com.ma.dc.data.couch.CouchObjCheckpoint;
import com.ma.dc.data.couch.CouchObjMeasurement;
import com.ma.dc.util.LogHelper;

public final class DbCheckpointHelper {
    
    public static ContentValues createCheckpointCvFromNetwork(final CouchObjCheckpoint checkpointCouchObject, final ContentValues oldCheckpoint) {
        
    	LogHelper.logDebug(DbCheckpointHelper.class, Common.LOG_TAG_DB, "createCheckpointCvFromNetwork");
    	
        if(oldCheckpoint != null) {
        	LogHelper.logDebug(DbCheckpointHelper.class, Common.LOG_TAG_DB, "createCheckpointCvFromNetwork", "oldCheckpoint != null");
        	
        	if(DbCheckpointHelper.getRev(oldCheckpoint).compareTo(checkpointCouchObject.getRev()) == 0) {
        		return oldCheckpoint;
        	}
        }
        
        final ContentValues cv = new ContentValues();

        cv.put(DbTableCheckpoint.COLUMN_ID, checkpointCouchObject.getId());
        cv.put(DbTableCheckpoint.COLUMN_REV, checkpointCouchObject.getRev());
        cv.put(DbTableCheckpoint.COLUMN_NAME, checkpointCouchObject.getName());
        cv.put(DbTableCheckpoint.COLUMN_DESCRIPTION, checkpointCouchObject.getDescription());
        cv.put(DbTableCheckpoint.COLUMN_ACTIVE, checkpointCouchObject.getActive());
        cv.put(DbTableCheckpoint.COLUMN_UPDATES, checkpointCouchObject.getUpdates());
        cv.put(DbTableCheckpoint.COLUMN_TIME_PERIOD, checkpointCouchObject.getTimePeriod());
        cv.put(DbTableCheckpoint.COLUMN_START_TIME, checkpointCouchObject.getStartTime());
        cv.put(DbTableCheckpoint.COLUMN_START_DAY, checkpointCouchObject.getStartDay());
        cv.put(DbTableCheckpoint.COLUMN_ORDER_NR, checkpointCouchObject.getOrderNr());
        
        cv.put(DbTableCheckpoint.COLUMN_ERROR_TAG_1, checkpointCouchObject.getErrorTag1());
        cv.put(DbTableCheckpoint.COLUMN_ERROR_TAG_2, checkpointCouchObject.getErrorTag2());
        cv.put(DbTableCheckpoint.COLUMN_ERROR_TAG_3, checkpointCouchObject.getErrorTag3());
        cv.put(DbTableCheckpoint.COLUMN_ERROR_TAG_4, checkpointCouchObject.getErrorTag4());
        
        cv.put(DbTableCheckpoint.COLUMN_ACTION_TAG_1, checkpointCouchObject.getActionTag1());
        cv.put(DbTableCheckpoint.COLUMN_ACTION_TAG_2, checkpointCouchObject.getActionTag2());
        cv.put(DbTableCheckpoint.COLUMN_ACTION_TAG_3, checkpointCouchObject.getActionTag3());
        cv.put(DbTableCheckpoint.COLUMN_ACTION_TAG_4, checkpointCouchObject.getActionTag4());
        
        cv.put(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_SYNCED, Boolean.TRUE);
       
        if(checkpointCouchObject.getNrOfAttachments() > 0) {
        	LogHelper.logDebug(DbCheckpointHelper.class, Common.LOG_TAG_DB, "createCheckpointCvFromNetwork", "checkpointCouchObject.getNrOfAttachments() > 0");
        	
            String filename = checkpointCouchObject.getAttachmentFilenames().get(0);
            cv.put(DbTableCheckpoint.COLUMN_IMAGE_FILENAME, filename);
            int filenameLength = checkpointCouchObject.getAttachmentInfo(filename).getLength();
            cv.put(DbTableCheckpoint.COLUMN_IMAGE_SIZE, filenameLength);
            
            if(oldCheckpoint != null) {
            	LogHelper.logDebug(DbCheckpointHelper.class, Common.LOG_TAG_DB, "createCheckpointCvFromNetwork", "oldCheckpoint != null");
            	
            	if(DbCheckpointHelper.getImageFilename(oldCheckpoint).compareTo(filename) == 0 &&
            	   DbCheckpointHelper.getImageSize(oldCheckpoint) == filenameLength) {
            		LogHelper.logDebug(DbCheckpointHelper.class, Common.LOG_TAG_DB, "createCheckpointCvFromNetwork", "cv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, Boolean.FALSE)");
            		cv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, Integer.valueOf(0));		
            	} else {
            		LogHelper.logDebug(DbCheckpointHelper.class, Common.LOG_TAG_DB, "createCheckpointCvFromNetwork", "cv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, Boolean.TRUE)");
            		cv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, Integer.valueOf(1));		
            	}
            } else {
            	LogHelper.logDebug(DbCheckpointHelper.class, Common.LOG_TAG_DB, "createCheckpointCvFromNetwork", "cv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, Boolean.TRUE)");
            	cv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, Integer.valueOf(1));
            }
        } else {
        	cv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, Integer.valueOf(0));
        }
        
        return cv;
    }
    
    public static ContentValues createCheckpointCvFromDatabase(final Cursor cursor) {
        final ContentValues cv = new ContentValues();
        
        DatabaseUtils.cursorRowToContentValues(cursor, cv);
        
        return cv;
    }

    public static void addMeasurement(final ContentValues checkpointCv, final CouchObjMeasurement measurementCouchObject) {
        if (checkpointCv != null && measurementCouchObject != null) {
            checkpointCv.put(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_DATE, measurementCouchObject.getDate());
            checkpointCv.put(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_VALUE, measurementCouchObject.getValue());
            checkpointCv.put(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_TAG, measurementCouchObject.getTag());
        }
    }

    public static String getDescription(final ContentValues checkpointCv) {
        return checkpointCv.getAsString(DbTableCheckpoint.COLUMN_DESCRIPTION);
    }

    public static String getId(final ContentValues checkpointCv) {
        return checkpointCv.getAsString(DbTableCheckpoint.COLUMN_ID);
    }

    public static Boolean getLatestMeasuredSynced(final ContentValues checkpointCv) {
        return checkpointCv.getAsBoolean(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_SYNCED);
    }

    public static Integer getLatestMeasuredValue(final ContentValues checkpointCv) {
        return checkpointCv.getAsInteger(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_VALUE);
    }
    
    public static String getLatestMeasurementTag(final ContentValues checkpointCv) {
    	return checkpointCv.getAsString(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_TAG);
	}
    
    public static Long getLatestMeasurementDate(final ContentValues checkpointCv) {
        return checkpointCv.getAsLong(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_DATE);
    }

    public static String getRev(final ContentValues checkpointCv) {
        return checkpointCv.getAsString(DbTableCheckpoint.COLUMN_REV);
    }
    
    public static String getName(final ContentValues checkpointCv) {
        return checkpointCv.getAsString(DbTableCheckpoint.COLUMN_NAME);
    }

    public static String getTimePeriod(final ContentValues checkpointCv) {
        return checkpointCv.getAsString(DbTableCheckpoint.COLUMN_TIME_PERIOD);
    }

    public static Integer getUpdates(final ContentValues checkpointCv) {
        return checkpointCv.getAsInteger(DbTableCheckpoint.COLUMN_UPDATES);
    }
    
    public static Integer getStartTime(final ContentValues checkpointCv) {
        return checkpointCv.getAsInteger(DbTableCheckpoint.COLUMN_START_TIME);
    }
    
    public static Integer getStartDay(final ContentValues checkpointCv) {
        return checkpointCv.getAsInteger(DbTableCheckpoint.COLUMN_START_DAY);
    }

    public static Integer getOrderNr(final ContentValues checkpointCv) {
        return checkpointCv.getAsInteger(DbTableCheckpoint.COLUMN_ORDER_NR);
    }
    
    public static String getErrorTag1(final ContentValues checkpointCv) {
        return checkpointCv.getAsString(DbTableCheckpoint.COLUMN_ERROR_TAG_1);
    }
    
    public static String getErrorTag2(final ContentValues checkpointCv) {
        return checkpointCv.getAsString(DbTableCheckpoint.COLUMN_ERROR_TAG_2);
    }
    
    public static String getErrorTag3(final ContentValues checkpointCv) {
        return checkpointCv.getAsString(DbTableCheckpoint.COLUMN_ERROR_TAG_3);
    }
    
    public static String getErrorTag4(final ContentValues checkpointCv) {
        return checkpointCv.getAsString(DbTableCheckpoint.COLUMN_ERROR_TAG_4);
    }
    
    public static String getActionTag1(final ContentValues checkpointCv) {
        return checkpointCv.getAsString(DbTableCheckpoint.COLUMN_ACTION_TAG_1);
    }
    
    public static String getActionTag2(final ContentValues checkpointCv) {
        return checkpointCv.getAsString(DbTableCheckpoint.COLUMN_ACTION_TAG_2);
    }
    
    public static String getActionTag3(final ContentValues checkpointCv) {
        return checkpointCv.getAsString(DbTableCheckpoint.COLUMN_ACTION_TAG_3);
    }
    
    public static String getActionTag4(final ContentValues checkpointCv) {
        return checkpointCv.getAsString(DbTableCheckpoint.COLUMN_ACTION_TAG_4);
    }
    
    public static String getImageFilename(final ContentValues checkpointCv) {
        return checkpointCv.getAsString(DbTableCheckpoint.COLUMN_IMAGE_FILENAME);
    }

    public static Integer getImageSize(final ContentValues checkpointCv) {
        return checkpointCv.getAsInteger(DbTableCheckpoint.COLUMN_IMAGE_SIZE);
    }
    
    public static Boolean getDownloadImage(final ContentValues checkpointCv) {
        return checkpointCv.getAsInteger(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE) != 0;
    }

    public static Bitmap getImage(final Context context, final ContentValues checkpointCv) {
        final File target = new File(context.getFilesDir(), getLocalImageFilename(checkpointCv));
        return BitmapFactory.decodeFile(target.getAbsolutePath());
    }
    
    private static boolean deleteImage(final Context context, final ContentValues checkpointCv) {
        final File fileToDelete = new File(context.getFilesDir(), getLocalImageFilename(checkpointCv));
        return fileToDelete.delete();
    }
    
    private static String getLocalImageFilename(final ContentValues checkpointCv) {
    	final String imageFilename = getImageFilename(checkpointCv);
        final String id = getId(checkpointCv);
        return id + "/" + imageFilename;
    }

    public static void removeFromDb(final Context context, final ContentResolver contentResolver, final ContentValues checkpointCv) {
    	deleteImage(context, checkpointCv);
        contentResolver.delete(getUrlOne(getId(checkpointCv)), null, null);
    }

    public static void setLatestMeasuredSynced(final Boolean synced, final ContentValues checkpointCv) {
        checkpointCv.put(DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_SYNCED, synced);
    }
    
    public static void setImageDownloaded(final ContentValues checkpointCv) {
        checkpointCv.put(DbTableCheckpoint.COLUMN_DOWNLOAD_IMAGE, Integer.valueOf(0));
    }

    /**
     * @return true if the database was changed
     */
    public static boolean syncWithDatabase(final ContentResolver contentResolver, final ContentValues checkpointCv) {

        LogHelper.logDebug(DbCheckpointHelper.class.getName(), Common.LOG_TAG_DB, "syncWithDatabase");

        Cursor dbCursor = contentResolver.query(getUrlOne(getId(checkpointCv)), null, null, null, null);
        int nrOfRows = dbCursor.getCount();
        dbCursor.close();
        if (nrOfRows == 0) {
            LogHelper.logDebug(DbCheckpointHelper.class.getName(), Common.LOG_TAG_DB, "syncWithDatabase", "insert");
            contentResolver.insert(DcContentProvider.CHECKPOINTS_URI, checkpointCv);
            return true;
        } else {
            LogHelper.logDebug(DbCheckpointHelper.class.getName(), Common.LOG_TAG_DB, "syncWithDatabase", "update");
            int result = contentResolver.update(getUrlOne(getId(checkpointCv)), checkpointCv, null, null);

            return (result > 0);
        }
    }
    
    public static Uri getUrlOne(final String id) {
        return Uri.parse(DcContentProvider.CHECKPOINTS_URI + "/" + id);
    }

    public static CouchObjMeasurement createCouchObjMeasurement(final ContentValues checkpointCv) {
    	
    	final int value = DbCheckpointHelper.getLatestMeasuredValue(checkpointCv);
    	final String tag = DbCheckpointHelper.getLatestMeasurementTag(checkpointCv);
    	final long timestamp = DbCheckpointHelper.getLatestMeasurementDate(checkpointCv).longValue();
        return new CouchObjMeasurement(DbCheckpointHelper.getId(checkpointCv), value, tag, timestamp);
    }
}
