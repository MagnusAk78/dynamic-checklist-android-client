package com.ma.dc.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import com.google.gson.Gson;
import com.ma.dc.Common;
import com.ma.dc.SettingsFragment;
import com.ma.dc.contentprovider.DcContentProvider;
import com.ma.dc.data.couch.CouchObjMeasurement;
import com.ma.dc.database.DbCheckpointHelper;
import com.ma.dc.database.DbTableCheckpoint;
import com.ma.dc.util.LogHelper;
import com.ma.dc.R;

final class UploadMeasurementsTask extends AsyncTask<Void, Void, Boolean> {

    interface UploadMeasurementsTaskCallback {
        void uploadMeasurementsTaskDone(boolean result);
    }

    private final Gson gson = new Gson();
    private ProgressDialog pd;
    private final Context context;
    private final UploadMeasurementsTaskCallback callbackObject;

    UploadMeasurementsTask(final Context context, final UploadMeasurementsTaskCallback callbackObject) {
        this.context = context;
        this.callbackObject = callbackObject;
    }

    @SuppressWarnings("unused")
    private UploadMeasurementsTask() {
        this.context = null;
        callbackObject = null;
    }

    @Override
    protected Boolean doInBackground(Void... states) {

        final List<ContentValues> checkpointsWithNewMeasurementValues = getAllCheckpointsWithNewMeasurementValuesFromDatabase();

        boolean returnValue = Boolean.TRUE;
        if (checkpointsWithNewMeasurementValues.size() > 0) {
            returnValue = sendNewMeasurementsToServer(checkpointsWithNewMeasurementValues);
        }

        return returnValue;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (pd != null) {
            pd.dismiss();
        }

        callbackObject.uploadMeasurementsTaskDone(result.booleanValue());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        pd = new ProgressDialog(context);
        pd.setTitle(R.string.progress_title);
        pd.setMessage(context.getResources().getString(R.string.progress_info_upload_measurements));
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.show();
    }

    private List<ContentValues> getAllCheckpointsWithNewMeasurementValuesFromDatabase() {
        final String selection = DbTableCheckpoint.COLUMN_LATEST_MEASUREMENT_SYNCED + " = ?";
        final String[] selectionArgs = new String[] { "0" };

        final Cursor unsyncedCheckpointsCursor = context.getContentResolver().query(DcContentProvider.CHECKPOINTS_URI,
                null, selection, selectionArgs, null);

        final List<ContentValues> checkpointsWithNewMeasurementValues = new ArrayList<ContentValues>(
                unsyncedCheckpointsCursor.getCount());
        while (unsyncedCheckpointsCursor.moveToNext()) {
            checkpointsWithNewMeasurementValues.add(DbCheckpointHelper.createCheckpointCvFromDatabase(unsyncedCheckpointsCursor));
        }

        unsyncedCheckpointsCursor.close();
        return checkpointsWithNewMeasurementValues;
    }

    private Boolean sendMeasurementToServer(final CouchObjMeasurement measurement) {
        final String jsonValue = gson.toJson(measurement);

        LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "sendMeasurement", "jsonValue: " + jsonValue);
        
		final String cloudantName = SettingsFragment.getCloudantName(context);
		final String databaseName = SettingsFragment.getDatabaseName(context);
		
		final String baseUrl = Common.HTTP_STRING + cloudantName + Common.CLOUDANT_END + "/" + databaseName;

        try {
            final int result = ConnectionUtils.postJsonDataToUrl(baseUrl, 10000, jsonValue);
            LogHelper.logDebug(UploadMeasurementsTask.class.getName(), "sendMeasurement",
                    "result,: " + Integer.toString(result));
            if (result == 201) {
                return Boolean.TRUE;
            }
        } catch (MalformedURLException e) {
            LogHelper.logWarning(this, Common.LOG_TAG_NETWORK, "sendMeasurementToServer", e);
        } catch (IOException e) {
            LogHelper.logWarning(this, Common.LOG_TAG_NETWORK, "sendMeasurementToServer", e);
        }

        return Boolean.FALSE;
    }

    private Boolean sendNewMeasurementsToServer(final List<ContentValues> checkpointsWithNewMeasurementValues) {
        boolean allSent = Boolean.TRUE;
        for (ContentValues checkpointCv : checkpointsWithNewMeasurementValues) {
            if (sendMeasurementToServer(DbCheckpointHelper.createCouchObjMeasurement(checkpointCv))) {
                DbCheckpointHelper.setLatestMeasuredSynced(Boolean.TRUE, checkpointCv);
                DbCheckpointHelper.syncWithDatabase(context.getContentResolver(), checkpointCv);
            } else {
                allSent = Boolean.FALSE;
            }
        }
        return allSent;
    }
}
