package com.ma.dc.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.ma.dc.Common;
import com.ma.dc.util.LogHelper;

public class DcContentProvider extends ContentProvider {

    // database
    private DcDatabaseHelper database;

    // Used for the UriMacher
    private static final int CHECKPOINTS_ALL = 1;
    private static final int CHECKPOINT_ID = 2;
    private static final int MEASUREMENTS_ALL = 3;
    private static final int MEASUREMENT_ID = 4;

    private static final String AUTHORITY = "com.ma.dc.database";

    private static final String CHECKPOINTS_PATH = "checkpoints";
    private static final String MEASUREMENTS_PATH = "measurements";

    // ----------------------------------------------------------------------------------------------
    // Public URI's for this ContentProvider

    public static final Uri CHECKPOINTS_URI = Uri.parse("content://" + AUTHORITY + "/" + CHECKPOINTS_PATH);
    public static final Uri MEASUREMENTS_URI = Uri.parse("content://" + AUTHORITY + "/" + MEASUREMENTS_PATH);

    public static final String CHECKPOINT_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/checkpoints";
    public static final String CHECKPOINT_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/checkpoint";

    public static final String MEASUREMENT_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/measurements";
    public static final String MEASUREMENT_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/measurements";

    // ----------------------------------------------------------------------------------------------

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, CHECKPOINTS_PATH, CHECKPOINTS_ALL);
        sURIMatcher.addURI(AUTHORITY, CHECKPOINTS_PATH + "/#", CHECKPOINT_ID);
        sURIMatcher.addURI(AUTHORITY, MEASUREMENTS_PATH, MEASUREMENTS_ALL);
        sURIMatcher.addURI(AUTHORITY, MEASUREMENTS_PATH + "/#", MEASUREMENT_ID);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        LogHelper.logDebug(this, Common.LOG_TAG_DB, "delete", "uri: " + uri);

        final SQLiteDatabase db = database.getWritableDatabase();

        final String tableName;

        switch (sURIMatcher.match(uri)) {
        case CHECKPOINTS_ALL:
            tableName = DbTableCheckpoint.TABLE_NAME;
            break;
        case CHECKPOINT_ID:
            tableName = DbTableCheckpoint.TABLE_NAME;

            final String checkpointId = uri.getPathSegments().get(1);
            selection = DbTableCheckpoint.COLUMN_INC_ID + "='" + checkpointId + "'"
                    + (!TextUtils.isEmpty(selection) ? " and (" + selection + ')' : "");
            break;
        case MEASUREMENTS_ALL:
            tableName = DbTableMeasurement.TABLE_NAME;
            break;
        case MEASUREMENT_ID:
            tableName = DbTableMeasurement.TABLE_NAME;

            final String measurementId = uri.getPathSegments().get(1);
            selection = DbTableMeasurement.COLUMN_ID + "=" + measurementId
                    + (!TextUtils.isEmpty(selection) ? " and (" + selection + ')' : "");
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // To return the number of deleted items you must specify a where
        // clause.
        if (selection == null) {
            selection = "1";
        }

        // Perform delete.
        final int deleteCount = db.delete(tableName, selection, selectionArgs);

        // Notify observers
        getContext().getContentResolver().notifyChange(uri, null);

        return deleteCount;
    }

    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
        case CHECKPOINTS_ALL:
            return CHECKPOINT_CONTENT_TYPE;
        case CHECKPOINT_ID:
            return CHECKPOINT_CONTENT_ITEM_TYPE;
        case MEASUREMENTS_ALL:
            return MEASUREMENT_CONTENT_TYPE;
        case MEASUREMENT_ID:
            return MEASUREMENT_CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unsupported Iri:" + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        LogHelper.logDebug(this, Common.LOG_TAG_DB, "insert", "uri: " + uri);

        final SQLiteDatabase db = database.getWritableDatabase();

        // NullColumnHack: specify what column that can be set to null if
        // inserting empty values.
        String nullColumnHack = null;

        final String tableName;
        final Uri contentUri;

        switch (sURIMatcher.match(uri)) {
        case CHECKPOINTS_ALL:
            tableName = DbTableCheckpoint.TABLE_NAME;
            contentUri = CHECKPOINTS_URI;
            break;
        case MEASUREMENTS_ALL:
            tableName = DbTableMeasurement.TABLE_NAME;
            contentUri = MEASUREMENTS_URI;
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        final long incId = db.insert(tableName, nullColumnHack, values);

        if (incId > -1) {
            // Construct and return URI of newly inserted row.
            Uri insertedId = ContentUris.withAppendedId(contentUri, incId);

            // Notify observers
            getContext().getContentResolver().notifyChange(insertedId, null);

            return insertedId;
        } else {
            return null;
        }
    }

    @Override
    public boolean onCreate() {
        LogHelper.logDebug(this, Common.LOG_TAG_DB, "onCreate");
        database = new DcDatabaseHelper(getContext(), DcDatabaseHelper.DATABASE_NAME, null,
                DcDatabaseHelper.DATABASE_VERSION);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        LogHelper.logDebug(this, Common.LOG_TAG_DB, "query", "uri: " + uri);

        // Open the database
        SQLiteDatabase db;
        try {
            db = database.getWritableDatabase();
        } catch (SQLiteException e) {
            db = database.getReadableDatabase();
        }

        // Replace with valid statements if needed.
        String groupBy = "";
        String having = "";

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (sURIMatcher.match(uri)) {
        case CHECKPOINTS_ALL:
            queryBuilder.setTables(DbTableCheckpoint.TABLE_NAME);
            break;
        case CHECKPOINT_ID:
            final String checkpointId = uri.getPathSegments().get(1);
            queryBuilder.appendWhere(DbTableCheckpoint.COLUMN_INC_ID + "='" + checkpointId + "'");
            queryBuilder.setTables(DbTableCheckpoint.TABLE_NAME);
            break;
        case MEASUREMENTS_ALL:
            queryBuilder.setTables(DbTableMeasurement.TABLE_NAME);
            break;
        case MEASUREMENT_ID:
            queryBuilder.setTables(DbTableMeasurement.TABLE_NAME);
            final String measurementId = uri.getPathSegments().get(1);
            queryBuilder.appendWhere(DbTableMeasurement.COLUMN_ID + " = " + measurementId);
            break;
        default:
            break;
        }

        final Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);

        // Set notification so that loaders will be updated on update.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        LogHelper.logDebug(this, Common.LOG_TAG_DB, "update", "uri: " + uri);

        final SQLiteDatabase db = database.getWritableDatabase();

        final String tableName;

        switch (sURIMatcher.match(uri)) {
        case CHECKPOINTS_ALL:
            tableName = DbTableCheckpoint.TABLE_NAME;
            break;
        case CHECKPOINT_ID:
            tableName = DbTableCheckpoint.TABLE_NAME;

            final String checkpointId = uri.getPathSegments().get(1);
            selection = DbTableCheckpoint.COLUMN_INC_ID + "='" + checkpointId + "'"
                    + (!TextUtils.isEmpty(selection) ? " and (" + selection + ')' : "");
            break;
        case MEASUREMENTS_ALL:
            tableName = DbTableMeasurement.TABLE_NAME;
            break;
        case MEASUREMENT_ID:
            tableName = DbTableMeasurement.TABLE_NAME;

            final String measurementId = uri.getPathSegments().get(1);
            selection = DbTableCheckpoint.COLUMN_INC_ID + "=" + measurementId
                    + (!TextUtils.isEmpty(selection) ? " and (" + selection + ')' : "");
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // Perform update.
        final int changeCount = db.update(tableName, values, selection, selectionArgs);

        // Notify observers
        getContext().getContentResolver().notifyChange(uri, null);

        return changeCount;
    }
}
