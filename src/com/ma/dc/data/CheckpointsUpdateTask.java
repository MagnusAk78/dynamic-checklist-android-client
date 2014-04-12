package com.ma.dc.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.ma.dc.Common;
import com.ma.dc.SettingsFragment;
import com.ma.dc.data.couch.CouchObjCheckpoint;
import com.ma.dc.data.couch.CouchObjMeasurement;
import com.ma.dc.data.couch.CouchViewResultKeyArray;
import com.ma.dc.database.CheckpointObject;
import com.ma.dc.database.DbCheckpointHelper;
import com.ma.dc.util.LogHelper;
import com.ma.dc.R;

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

        final List<CheckpointObject> checkpointDatabaseList = DbCheckpointHelper.getAllCheckpointsInDatabase(context
                .getContentResolver());

        if (checkpointList.size() > 0) {
            if (checkpointDatabaseList.size() > 0) {
                removeAllCheckpointsNoLongerOnServer(checkpointList, checkpointDatabaseList);
            }
            final int updated = saveTheNewDataToDatabase(checkpointList, checkpointDatabaseList);
            LogHelper
                    .logDebug(UploadMeasurementsTask.class.getName(), "doInBackground", "database changed: " + updated);
        }

        try {
            getLatestMeasurementForEachCheckpoint(DbCheckpointHelper.getAllCheckpointsInDatabase(context
                    .getContentResolver()));
        } catch (Exception e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }

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

    private CouchObjMeasurement getLatestMeasurement(final String checkpointId) throws MalformedURLException,
            IOException, JsonSyntaxException {

        final String cloudantName = SettingsFragment.getCloudantName(context);
        final String databaseName = SettingsFragment.getDatabaseName(context);

        final String baseUrl = Common.HTTP_STRING + cloudantName + Common.CLOUDANT_END + "/" + databaseName;

        final String fullUrl = baseUrl + Common.MEASUREMENTS_VIEW_END + "?startkey=[\"" + checkpointId
                + "\",{}]&endkey=[\"" + checkpointId + "\"]&descending=true&limit=1";

        final String jsonData = ConnectionUtils.getJsonDataFromUrl(fullUrl, 4000);

        final CouchViewResultKeyArray mv;

        mv = gson.fromJson(jsonData, CouchViewResultKeyArray.class);
        if (mv.getNumberOfRows() != 1) {
            return null;
        }

        List<JsonObject> jsonList = new ArrayList<JsonObject>(1);
        mv.populateListFromRows(jsonList);

        if (jsonList.size() != 1) {
            return null;
        }

        return gson.fromJson(jsonList.get(0), CouchObjMeasurement.class);
    }

    private void getLatestMeasurementForEachCheckpoint(final List<CheckpointObject> checkpointList)
            throws JsonSyntaxException, MalformedURLException, IOException {
        for (CheckpointObject checkpoint : checkpointList) {
            final CouchObjMeasurement measurementObj = getLatestMeasurement(checkpoint.getStringId());
            if (measurementObj != null) {
                checkpoint.updateLatestMeasurement(context.getContentResolver(), measurementObj.getDate(),
                        measurementObj.getValue());
            }
        }
    }

    private String getRawDataFromCheckpointsView() {
        LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "getRawDataFromCheckpointsView");
        try {
            final String cloudantName = SettingsFragment.getCloudantName(context);
            final String databaseName = SettingsFragment.getDatabaseName(context);

            LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "getRawDataFromCheckpointsView", "cloudantName: "
                    + cloudantName);
            LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "getRawDataFromCheckpointsView", "cloudantName: "
                    + databaseName);

            final String baseUrl = Common.HTTP_STRING + cloudantName + Common.CLOUDANT_END + "/" + databaseName;

            LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "getRawDataFromCheckpointsView", "baseUrl: " + baseUrl);

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
                LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "parseCheckpointsJsonToObjects", checkpoint.getName()
                        + " added!");
            } catch (JsonSyntaxException e) {
                LogHelper.logWarning(this, Common.LOG_TAG_NETWORK, "parseCheckpointsJsonToObjects", e);
                return null;
            }
        }

        return checkpointList;
    }

    private int removeAllCheckpointsNoLongerOnServer(final List<CouchObjCheckpoint> checkpointList,
            final List<CheckpointObject> checkpointDatabaseList) {
        int nrRemoved = 0;
        for (CheckpointObject checkpointObject : checkpointDatabaseList) {
            if (!couchObjCheckpointListContainsId(checkpointList, checkpointObject.getStringId())) {
                nrRemoved += checkpointObject.deleteFromDatabase(context);
            }
        }

        return nrRemoved;
    }

    private boolean couchObjCheckpointListContainsId(final List<CouchObjCheckpoint> checkpointList,
            final String stringId) {
        for (CouchObjCheckpoint checkpointObj : checkpointList) {
            if (checkpointObj.getId().compareTo(stringId) == 0) {
                return true;
            }
        }
        return false;
    }

    private CheckpointObject findCheckpointObjectWithStringId(final List<CheckpointObject> checkpointList,
            final String stringId) {
        for (CheckpointObject checkpointObject : checkpointList) {
            if (checkpointObject.getStringId().compareTo(stringId) == 0) {
                return checkpointObject;
            }
        }
        return null;
    }

    private int saveTheNewDataToDatabase(final List<CouchObjCheckpoint> checkpointList,
            List<CheckpointObject> checkpointDatabaseList) {
        int updated = 0;
        for (CouchObjCheckpoint checkpoint : checkpointList) {
            final CheckpointObject oldVersion = findCheckpointObjectWithStringId(checkpointDatabaseList,
                    checkpoint.getId());
            updated += DbCheckpointHelper.storeCheckpointFromNetwork(context.getContentResolver(), checkpoint,
                    oldVersion);
        }
        return updated;
    }
}