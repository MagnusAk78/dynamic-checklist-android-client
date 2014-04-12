package com.ma.dc.data;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;

import com.ma.dc.database.NewMeasurementObject;
import com.ma.dc.util.LogHelper;
import com.ma.dc.Common;
import com.ma.dc.R;

public class InsertNewMeasurementTask extends AsyncTask<Void, Void, Boolean> {

    public interface InsertMeasurementCallback {
        void insertMeasurementDone();
    }

    private ProgressDialog pd;

    private final Context mApplicationContext;
    private final InsertMeasurementCallback mCallback;
    private final NewMeasurementObject newMeasurementObject;

    public InsertNewMeasurementTask(final InsertMeasurementCallback callback, final Context context,
            final NewMeasurementObject newMeasurementObject) {
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "InsertNewMeasurementTask");

        this.mCallback = callback;
        this.mApplicationContext = context.getApplicationContext();
        this.newMeasurementObject = newMeasurementObject;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            newMeasurementObject.storeToDatabase(mApplicationContext.getContentResolver());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (pd != null) {
            pd.dismiss();
        }
        Toast toast = null;
        if (result.booleanValue()) {
            switch (newMeasurementObject.getValue()) {
            case 1:
                toast = Toast.makeText(mApplicationContext, R.string.check_done_check_ok, Toast.LENGTH_LONG);
                break;
            case 0:
                toast = Toast.makeText(mApplicationContext, R.string.check_done_check_ok_after_action,
                        Toast.LENGTH_LONG);
                break;
            case -1:
                toast = Toast.makeText(mApplicationContext, R.string.check_done_check_not_ok, Toast.LENGTH_LONG);
                break;
            }
        } else {
            toast = Toast.makeText(mApplicationContext, R.string.check_done_problem, Toast.LENGTH_LONG);
        }

        if (toast != null) {
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }

        mCallback.insertMeasurementDone();
    }
}
