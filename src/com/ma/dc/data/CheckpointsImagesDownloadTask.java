package com.ma.dc.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import com.androidquery.AQuery;
import com.ma.dc.Common;
import com.ma.dc.SettingsFragment;
import com.ma.dc.contentprovider.DcContentProvider;
import com.ma.dc.database.DbCheckpointHelper;
import com.ma.dc.util.LogHelper;
import com.ma.dc.R;

class CheckpointsImagesDownloadTask extends AsyncTask<Void, Void, Boolean> implements ConnectionUtils.FileDownloadCallback {

    interface CheckpointsImagesDownloadTaskCallback {
        void checkpointsImagesDownloadTaskTaskDone(boolean result);
    }

    private ProgressDialog pd;
    private final Context context;
    private final CheckpointsImagesDownloadTaskCallback checkpointsImagesDownloadTaskCallback;
    private AQuery aq;
    
    private ConcurrentHashMap<String, Boolean> fileDownloadCheckMap = new ConcurrentHashMap<String, Boolean>();

    CheckpointsImagesDownloadTask(final Context context, final CheckpointsImagesDownloadTaskCallback checkpointsImagesDownloadTaskCallback) {
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
        
        final List<ContentValues> checkpointDatabaseList = getAllCheckpointsInDatabase();
        
        //Download images
        for(ContentValues checkpoint: checkpointDatabaseList) {
        	LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "doInBackground", "Id: " + DbCheckpointHelper.getId(checkpoint));
        	LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "doInBackground", "download image: " + DbCheckpointHelper.getDownloadImage(checkpoint));
        	LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "doInBackground", "image size: " + DbCheckpointHelper.getImageSize(checkpoint));
        	
        	if(DbCheckpointHelper.getDownloadImage(checkpoint)) {
        		
        		String imageFilename = DbCheckpointHelper.getImageFilename(checkpoint);
        		LogHelper.logDebug(this, Common.LOG_TAG_NETWORK, "doInBackground", DbCheckpointHelper.getName(checkpoint) + ", download image:" + imageFilename);
                    
        		final String cloudantName = SettingsFragment.getCloudantName(context);
        		final String databaseName = SettingsFragment.getDatabaseName(context);
                    
        		final String imgUrl = Common.HTTP_STRING + cloudantName + Common.CLOUDANT_END + "/" + databaseName + "/" + DbCheckpointHelper.getId(checkpoint) + "/" + imageFilename;
                    
        		final File target = new File(context.getFilesDir(), DbCheckpointHelper.getId(checkpoint) + "/" + imageFilename);
                    
        		fileDownloadCheckMap.put(imgUrl, Boolean.FALSE);
                    
        		ConnectionUtils.getFileFromServer(aq, imgUrl, target, this);
        		
        		DbCheckpointHelper.setImageDownloaded(checkpoint);
        		
        		DbCheckpointHelper.syncWithDatabase(context.getContentResolver(), checkpoint);
        	}
        }
            
        //Wait for every file
        while(fileDownloadCheckMap.containsValue(Boolean.FALSE)) {
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

    private List<ContentValues> getAllCheckpointsInDatabase() {
        final Cursor checkpointsCursor = context.getContentResolver().query(DcContentProvider.CHECKPOINTS_URI, null,
                null, null, null);
        final ArrayList<ContentValues> checkpointsFromDatabase = new ArrayList<ContentValues>(
                checkpointsCursor.getCount());
        while (checkpointsCursor.moveToNext()) {
            checkpointsFromDatabase.add(DbCheckpointHelper.createCheckpointCvFromDatabase(checkpointsCursor));
        }
        checkpointsCursor.close();
        return checkpointsFromDatabase;
    }
}