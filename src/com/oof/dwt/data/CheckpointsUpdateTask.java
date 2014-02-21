package com.oof.dwt.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.oof.dwt.Common;
import com.oof.dwt.R;
import com.oof.dwt.SettingsFragment;
import com.oof.dwt.contentprovider.DwtContentProvider;
import com.oof.dwt.data.couch.CouchObjCheckpoint;
import com.oof.dwt.data.couch.CouchObjMeasurement;
import com.oof.dwt.data.couch.CouchViewResultKeyArray;
import com.oof.dwt.database.DbCheckpointHelper;
import com.oof.dwt.util.LogHelper;

class CheckpointsUpdateTask extends AsyncTask<Void, Void, Boolean> implements ConnectionUtils.FileDownloadCallback {

    interface CheckpointsUpdateTaskCallback {
        void checkpointsUpdateTaskDone(boolean result);
    }

    private final Gson gson = new Gson();
    private ProgressDialog pd;
    private final Context context;
    private final CheckpointsUpdateTaskCallback checkpointsUpdateTaskCallback;
    
    private ConcurrentHashMap<String, Boolean> fileDownloadCheckMap = new ConcurrentHashMap<String, Boolean>();

    CheckpointsUpdateTask(final Context context, final CheckpointsUpdateTaskCallback checkpointsUpdateTaskCallback) {
        this.context = context;
        this.checkpointsUpdateTaskCallback = checkpointsUpdateTaskCallback;

    }

    @SuppressWarnings("unused")
    private CheckpointsUpdateTask() {
        this.context = null;
        this.checkpointsUpdateTaskCallback = null;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "doInBackground");
        
        final String jsonData = getRawDataFromCheckpointsView();
        if (jsonData == null) {
            return Boolean.FALSE;
        }

        // Parse Json values
        final List<CouchObjCheckpoint> checkpointList = parseCheckpointsJsonToObjects(jsonData);
        if (checkpointList == null) {
            LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "doInBackground", "checkpointList == null");
            return Boolean.FALSE;
        }
        
        Map<CouchObjCheckpoint, CouchObjMeasurement> checkpointMeasurementMap = null;
        if (checkpointList.size() > 0) {
            checkpointMeasurementMap = new HashMap<CouchObjCheckpoint, CouchObjMeasurement>(checkpointList.size());
            getLatestMeasurementForEachCheckpoint(checkpointList, checkpointMeasurementMap);
        }

        final List<ContentValues> checkpointDatabaseList = getAllCheckpointsInDatabase();

        if (checkpointList.size() > 0 && checkpointDatabaseList.size() > 0) {
            removeAllCheckpointsNoLongerOnServer(checkpointList, checkpointDatabaseList);
        }

        boolean changed = false;
        if (checkpointList.size() > 0) {
            changed = saveTheNewDataToDatabase(checkpointList, checkpointMeasurementMap);
        }

        LogHelper.logDebug(UploadMeasurementsTask.class.getName(), "doInBackground", "database changed: " + changed);

        return Boolean.TRUE;
    }
    
    @Override
    public void downloadDone(String url, boolean statusOk, String failureMessage) {
        fileDownloadCheckMap.put(url, Boolean.TRUE);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (pd != null) {
            pd.dismiss();
        }

        checkpointsUpdateTaskCallback.checkpointsUpdateTaskDone(result.booleanValue());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        pd = new ProgressDialog(context);
        pd.setTitle(R.string.progress_title);
        pd.setMessage(context.getResources().getString(R.string.progress_info_updating));
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.show();
    }

    private List<ContentValues> getAllCheckpointsInDatabase() {
        final Cursor checkpointsCursor = context.getContentResolver().query(DwtContentProvider.CHECKPOINTS_URI, null,
                null, null, null);
        final ArrayList<ContentValues> checkpointsFromDatabase = new ArrayList<ContentValues>(
                checkpointsCursor.getCount());
        while (checkpointsCursor.moveToNext()) {
            checkpointsFromDatabase.add(DbCheckpointHelper.createCheckpointCvFromDatabase(checkpointsCursor));
        }
        checkpointsCursor.close();
        return checkpointsFromDatabase;
    }

    private CouchObjMeasurement getLatestMeasurement(String checkpointId) {

		final String cloudantName = SettingsFragment.getCloudantName(context);
		final String databaseName = SettingsFragment.getDatabaseName(context);
		
		final String baseUrl = Common.HTTP_STRING + cloudantName + Common.CLOUDANT_END + "/" + databaseName;
        
        String fullUrl = baseUrl + Common.MEASUREMENTS_VIEW_END + "?startkey=[\"" + checkpointId + "\",{}]&endkey=[\""
                + checkpointId + "\"]&descending=true&limit=1";

        String jsonData = null;
        try {
            jsonData = ConnectionUtils.getJsonDataFromUrl(fullUrl, 10000);
        } catch (Exception e) {
            LogHelper.logWarning(this, Common.LOG_TAG_NETWORK, "getLatestMeasurement", e);
        }
        if (jsonData == null) {
            return null;
        }

        CouchViewResultKeyArray mv;

        mv = gson.fromJson(jsonData, CouchViewResultKeyArray.class);
        if (mv.getNumberOfRows() == 0) {
            return null;
        }
        List<JsonObject> jsonList = new ArrayList<JsonObject>();
        mv.populateListFromRows(jsonList);

        for (JsonObject json : jsonList) {
            CouchObjMeasurement latestMeasurement = gson.fromJson(json, CouchObjMeasurement.class);

            if (latestMeasurement != null) {
                return latestMeasurement;
            }
        }
        return null;
    }

    private void getLatestMeasurementForEachCheckpoint(final List<CouchObjCheckpoint> checkpointList,
            Map<CouchObjCheckpoint, CouchObjMeasurement> checkpointMeasurementMap) {
        for (CouchObjCheckpoint checkpoint : checkpointList) {
            final CouchObjMeasurement measurement = getLatestMeasurement(checkpoint.getId());
            if (measurement != null) {
                checkpointMeasurementMap.put(checkpoint, measurement);
            }
        }
    }

    private String getRawDataFromCheckpointsView() {
        LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "getRawDataFromCheckpointsView");
        try {
    		final String cloudantName = SettingsFragment.getCloudantName(context);
    		final String databaseName = SettingsFragment.getDatabaseName(context);
    		
    		final String baseUrl = Common.HTTP_STRING + cloudantName + Common.CLOUDANT_END + "/" + databaseName;
            
            return ConnectionUtils.getJsonDataFromUrl(baseUrl + Common.CHECKPOINTS_VIEW_END, 10000);
        } catch (MalformedURLException e) {
            LogHelper.logWarning(this, Common.LOG_TAG_NETWORK, "getRawDataFromCheckpointsView", e);
            return null;
        } catch (IOException e) {
            LogHelper.logWarning(this, Common.LOG_TAG_NETWORK, "getRawDataFromCheckpointsView", e);
            return null;
        } catch (Exception e) {
            LogHelper.logWarning(this, Common.LOG_TAG_NETWORK, "getRawDataFromCheckpointsView", e);
            return null;
        }
    }

    private List<CouchObjCheckpoint> parseCheckpointsJsonToObjects(final String jsonData) {
        LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "parseCheckpointsJsonToObjects");
        
        CouchViewResultKeyArray checkpointsView = null;
        try {
            checkpointsView = gson.fromJson(jsonData, CouchViewResultKeyArray.class);
            if (checkpointsView.getNumberOfRows() == 0) {
                // This is ok, create an empty list
                return new ArrayList<CouchObjCheckpoint>(0);
            }
        } catch (JsonSyntaxException e) {
            LogHelper.logWarning(this, Common.LOG_TAG_NETWORK, "parseCheckpointsJsonToObjects", e);
            return null;
        }

        final List<JsonObject> jsonList = new ArrayList<JsonObject>(checkpointsView.getNumberOfRows());
        checkpointsView.populateListFromRows(jsonList);

        final List<CouchObjCheckpoint> checkpointList = new ArrayList<CouchObjCheckpoint>(
                checkpointsView.getNumberOfRows());

        for (JsonObject json : jsonList) {
            try {
                final CouchObjCheckpoint checkpoint = gson.fromJson(json, CouchObjCheckpoint.class);
                checkpointList.add(checkpoint);
                LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "parseCheckpointsJsonToObjects", checkpoint.getName() + " added!");
            } catch (JsonSyntaxException e) {
                LogHelper.logWarning(this, Common.LOG_TAG_NETWORK, "parseCheckpointsJsonToObjects", e);
                return null;
            }
        }

        return checkpointList;
    }

    private void removeAllCheckpointsNoLongerOnServer(final List<CouchObjCheckpoint> checkpointList,
            final List<ContentValues> checkpointDatabaseList) {
        for (ContentValues checkpointCv : checkpointDatabaseList) {
            if(!couchObjCheckpointListContainsId(checkpointList, DbCheckpointHelper.getId(checkpointCv))) {
                DbCheckpointHelper.removeFromDb(context, context.getContentResolver(), checkpointCv);
            }
        }
    }
    
    private boolean couchObjCheckpointListContainsId(final List<CouchObjCheckpoint> checkpointList, final String id) {
        for (CouchObjCheckpoint checkpointObj : checkpointList) {
            if(checkpointObj.getId().compareTo(id) == 0) {
                return true;
            }
        }
        return false;
    }

    private boolean saveTheNewDataToDatabase(final List<CouchObjCheckpoint> checkpointList,
            final Map<CouchObjCheckpoint, CouchObjMeasurement> checkpointMeasurementMap) {
    	
        boolean changed = false;
        for (CouchObjCheckpoint checkpoint : checkpointList) {
        	//Compare revision to database, only create a new if different
        	
            final Cursor checkpointsCursor = context.getContentResolver().query(DbCheckpointHelper.getUrlOne(checkpoint.getId()), null,
                    null, null, null);
            ContentValues cvCheckpointDatabase = null;
            if (checkpointsCursor.moveToFirst()) {
            	cvCheckpointDatabase = DbCheckpointHelper.createCheckpointCvFromDatabase(checkpointsCursor);
            }
            checkpointsCursor.close();
            
            LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "saveTheNewDataToDatabase", checkpoint.toString());
        	
            final ContentValues checkpointCv = DbCheckpointHelper.createCheckpointCvFromNetwork(checkpoint, cvCheckpointDatabase);
            DbCheckpointHelper.addMeasurement(checkpointCv, checkpointMeasurementMap.get(checkpoint));
            DbCheckpointHelper.setLatestMeasuredSynced(Boolean.TRUE, checkpointCv);
            changed = DbCheckpointHelper.syncWithDatabase(context.getContentResolver(), checkpointCv) || changed;
        }
        return changed;
    }
}