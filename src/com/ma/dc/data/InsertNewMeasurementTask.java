package com.ma.dc.data;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;

import com.ma.dc.data.couch.CouchObjMeasurement;
import com.ma.dc.database.DbCheckpointHelper;
import com.ma.dc.R;

public class InsertNewMeasurementTask extends AsyncTask<Void, Void, Boolean> {

    public interface InsertMeasurementCallback {
        void insertMeasurementDone();
    }

    private ProgressDialog pd;

    private final Context mApplicationContext;
    private final ContentValues checkpointCv;
    private final int value;
    private final String tag;

    private final InsertMeasurementCallback mCallback;

    public InsertNewMeasurementTask(final InsertMeasurementCallback callback, final Context context,
            final ContentValues checkpointCv, final int value, final String tag) {
        this.mCallback = callback;
        this.mApplicationContext = context.getApplicationContext();
        this.checkpointCv = checkpointCv;
        this.value = value;
        this.tag = tag;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        final CouchObjMeasurement measurement = new CouchObjMeasurement(DbCheckpointHelper.getId(checkpointCv), value, tag);
        DbCheckpointHelper.addMeasurement(checkpointCv, measurement);
        DbCheckpointHelper.setLatestMeasuredSynced(Boolean.FALSE, checkpointCv);
        return DbCheckpointHelper.syncWithDatabase(mApplicationContext.getContentResolver(), checkpointCv);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (pd != null) {
            pd.dismiss();
        }
        Toast toast = null;
        if (result.booleanValue()) {
            switch(value) {
            case 1:
                toast = Toast.makeText(mApplicationContext, R.string.check_done_check_ok, Toast.LENGTH_LONG);
                break;
            case 0:
                toast = Toast.makeText(mApplicationContext, R.string.check_done_check_ok_after_action, Toast.LENGTH_LONG);
                break;
            case -1:
                toast = Toast.makeText(mApplicationContext, R.string.check_done_check_not_ok, Toast.LENGTH_LONG);
                break;
            }
        } else {
            toast = Toast.makeText(mApplicationContext, R.string.check_done_problem, Toast.LENGTH_LONG);
        }
        
        if(toast != null) {
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }

        mCallback.insertMeasurementDone();
    }
}
