package com.ma.dc.data;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.androidquery.AQuery;
import com.ma.dc.Common;
import com.ma.dc.SettingsFragment;
import com.ma.dc.database.CheckpointObject;
import com.ma.dc.database.DbCheckpointHelper;
import com.ma.dc.util.LogHelper;
import com.ma.dc.R;

class CheckpointsImagesDownloadTask extends AsyncTask<Void, Void, Boolean> implements
        ConnectionUtils.FileDownloadCallback {

    interface CheckpointsImagesDownloadTaskCallback {
        void checkpointsImagesDownloadTaskTaskDone(boolean result);
    }

    private ProgressDialog pd;
    private final Context context;
    private final CheckpointsImagesDownloadTaskCallback checkpointsImagesDownloadTaskCallback;
    private AQuery aq;

    private ConcurrentHashMap<String, Boolean> fileDownloadCheckMap = new ConcurrentHashMap<String, Boolean>();
    private ConcurrentHashMap<String, CheckpointObject> fileToCheckpoint = new ConcurrentHashMap<String, CheckpointObject>();

    CheckpointsImagesDownloadTask(final Context context,
            final CheckpointsImagesDownloadTaskCallback checkpointsImagesDownloadTaskCallback) {
        this.context = context;
        this.checkpointsImagesDownloadTaskCallback = checkpointsImagesDownloadTaskCallback;
        aq = new AQuery(context);

    }

    @SuppressWarnings("unused")
    private CheckpointsImagesDownloadTask() {
        this.context = null;
        this.checkpointsImagesDownloadTaskCallback = null;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "doInBackground");

        final List<CheckpointObject> checkpointDatabaseList = DbCheckpointHelper.getAllCheckpointsInDatabase(context
                .getContentResolver());

        // Download images
        for (CheckpointObject checkpoint : checkpointDatabaseList) {
            LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "doInBackground", "Id: " + checkpoint.getId());
            LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "doInBackground",
                    "download image: " + checkpoint.getDownloadImage());
            LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "doInBackground",
                    "image size: " + checkpoint.getImageFilesize());

            if (checkpoint.getDownloadImage()) {
                final String cloudantName = SettingsFragment.getCloudantName(context);
                final String databaseName = SettingsFragment.getDatabaseName(context);

                final String imgUrl = Common.HTTP_STRING + cloudantName + Common.CLOUDANT_END + "/" + databaseName
                        + "/" + checkpoint.getFullImageFilePath();

                final File target = new File(context.getFilesDir(), checkpoint.getFullImageFilePath());

                fileDownloadCheckMap.put(imgUrl, Boolean.FALSE);
                fileToCheckpoint.put(imgUrl, checkpoint);

                ConnectionUtils.getFileFromServer(aq, imgUrl, target, this);

                checkpoint.updateDownloadImage(context.getContentResolver(), false);
            }
        }

        // Wait for every file
        while (fileDownloadCheckMap.containsValue(Boolean.FALSE)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public void downloadDone(String url, boolean statusOk, String failureMessage) {
        fileDownloadCheckMap.put(url, Boolean.TRUE);
        if (!statusOk) {
            // Reset value to re-download file
            fileToCheckpoint.get(url).updateDownloadImage(context.getContentResolver(), true);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (pd != null) {
            pd.dismiss();
        }

        checkpointsImagesDownloadTaskCallback.checkpointsImagesDownloadTaskTaskDone(result);
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
}