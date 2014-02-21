package com.oof.dwt.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.oof.dwt.Common;
import com.oof.dwt.database.DbTableCheckpoint;
import com.oof.dwt.database.DwtDatabaseHelper;
import com.oof.dwt.util.LogHelper;

public class DwtContentProvider extends ContentProvider {

    // database
    private DwtDatabaseHelper database;

    // Used for the UriMacher
    private static final int CHECKPOINTS = 10;
    private static final int CHECKPOINT_ID = 20;

    private static final String AUTHORITY = "com.oof.dwt.contentprovider";

    private static final String CHECKPOINTS_PATH = "checkpoints";

    // ----------------------------------------------------------------------------------------------
    // Public URI's for this ContentProvider

    public static final Uri CHECKPOINTS_URI = Uri.parse("content://" + AUTHORITY + "/" + CHECKPOINTS_PATH);

    public static final String CHECKPOINT_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/checkpoints";
    public static final String CHECKPOINT_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/checkpoint";

    // ----------------------------------------------------------------------------------------------

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, CHECKPOINTS_PATH, CHECKPOINTS);
        sURIMatcher.addURI(AUTHORITY, CHECKPOINTS_PATH + "/*", CHECKPOINT_ID);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        
        LogHelper.logDebug(this, Common.LOG_TAG_CONTENT_PROVIDER, "delete", "uri");

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
        case CHECKPOINTS:
            rowsDeleted = sqlDB.delete(DbTableCheckpoint.TABLE_NAME, selection, selectionArgs);
            break;
        case CHECKPOINT_ID:
            rowsDeleted = deleteFromId(uri, selection, selectionArgs, sqlDB, DbTableCheckpoint.TABLE_NAME);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public String getType(Uri url) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id;

        String path;
        switch (uriType) {
        case CHECKPOINTS:
            id = sqlDB.insert(DbTableCheckpoint.TABLE_NAME, null, values);
            path = CHECKPOINTS_PATH;
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(path + "/" + id);
    }

    @Override
    public boolean onCreate() {
        database = new DwtDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        int uriType = sURIMatcher.match(uri);

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (uriType) {
        case CHECKPOINTS:
            // Check if the caller has requested a column which does not exists
            checkCheckpointColumns(projection);
            // Set the table
            queryBuilder.setTables(DbTableCheckpoint.TABLE_NAME);
            break;
        case CHECKPOINT_ID:
            buildQueryFromId(uri, projection, queryBuilder, DbTableCheckpoint.TABLE_NAME);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
        case CHECKPOINTS:
            rowsUpdated = sqlDB.update(DbTableCheckpoint.TABLE_NAME, values, selection, selectionArgs);
            break;
        case CHECKPOINT_ID:
            rowsUpdated = updateWithId(uri, values, selection, selectionArgs, sqlDB, DbTableCheckpoint.TABLE_NAME);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void buildQueryFromId(Uri uri, String[] projection, SQLiteQueryBuilder queryBuilder, String table) {
        // Check if the caller has requested a column which does not exists
        checkCheckpointColumns(projection);
        // Set the table
        queryBuilder.setTables(table);
        // Adding the ID to the original query
        queryBuilder.appendWhere(DbTableCheckpoint.COLUMN_ID + "='" + uri.getLastPathSegment() + "'");
    }

    private void checkCheckpointColumns(String[] projection) {
        checkColumns(DbTableCheckpoint.allColumns(), projection);
    }

    private void checkColumns(String[] available, String[] projection) {
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

    private int deleteFromId(Uri uri, String selection, String[] selectionArgs, SQLiteDatabase sqlDB, String table) {
        int rowsDeleted;
        String id = uri.getLastPathSegment();
        if (TextUtils.isEmpty(selection)) {
            rowsDeleted = sqlDB.delete(table, DbTableCheckpoint.COLUMN_ID + "='" + id + "'", null);
        } else {
            rowsDeleted = sqlDB.delete(table, DbTableCheckpoint.COLUMN_ID + "='" + id + "' and " + selection, selectionArgs);
        }
        return rowsDeleted;
    }

    private int updateWithId(Uri uri, ContentValues values, String selection, String[] selectionArgs,
            SQLiteDatabase sqlDB, String table) {
        int rowsUpdated;
        String checkpointId = uri.getLastPathSegment();
        if (TextUtils.isEmpty(selection)) {
            rowsUpdated = sqlDB.update(table, values, DbTableCheckpoint.COLUMN_ID + "=?", new String[] { checkpointId });
        } else {
            rowsUpdated = sqlDB.update(table, values, DbTableCheckpoint.COLUMN_ID + "='" + checkpointId + "' and "
                    + selection, selectionArgs);
        }
        return rowsUpdated;
    }
}
