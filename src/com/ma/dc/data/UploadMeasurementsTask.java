package com.ma.dc.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import com.google.gson.Gson;
import com.ma.dc.Common;
import com.ma.dc.SettingsFragment;
import com.ma.dc.data.couch.CouchObjMeasurement;
import com.ma.dc.database.DcContentProvider;
import com.ma.dc.database.MeasurementObject;
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

        final List<MeasurementObject> allMeasurementNotSentToServer = getAllMeasurementNotSentToServer();

        boolean returnValue = Boolean.TRUE;
        if (allMeasurementNotSentToServer.size() > 0) {
            returnValue = sendNewMeasurementsToServer(allMeasurementNotSentToServer);
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

    private List<MeasurementObject> getAllMeasurementNotSentToServer() {
        LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "getAllMeasurementNotSentToServer");

        final Cursor unsyncedMeasurementsCursor = context.getContentResolver().query(
                DcContentProvider.MEASUREMENTS_URI, MeasurementObject.PROJECTION, null, null, null);

        final List<MeasurementObject> measurementsNotSentToServer = new ArrayList<MeasurementObject>(
                unsyncedMeasurementsCursor.getCount());
        while (unsyncedMeasurementsCursor.moveToNext()) {
            measurementsNotSentToServer.add(new MeasurementObject(unsyncedMeasurementsCursor));
        }

        unsyncedMeasurementsCursor.close();
        return measurementsNotSentToServer;
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

    private Boolean sendNewMeasurementsToServer(final List<MeasurementObject> measurementsNotSentToServer) {
        boolean allSent = Boolean.TRUE;
        for (MeasurementObject measurementObject : measurementsNotSentToServer) {
            if (sendMeasurementToServer(measurementObject.createCouchObjMeasurement())) {
                measurementObject.deleteFromDatabase(context.getContentResolver());
            } else {
                allSent = Boolean.FALSE;
            }
        }
        return allSent;
    }
}
