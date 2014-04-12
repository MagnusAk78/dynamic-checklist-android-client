package com.ma.dc.data;

import android.content.Context;
import android.widget.Toast;

import com.ma.dc.R;

public final class FullUpdater implements UploadMeasurementsTask.UploadMeasurementsTaskCallback,
        CheckpointsUpdateTask.CheckpointsUpdateTaskCallback,
        CheckpointsImagesDownloadTask.CheckpointsImagesDownloadTaskCallback {

    private final static int STATE_UPLOAD_MEASUREMENTS = 1;
    private final static int STATE_UPDATE = 2;
    private final static int STATE_UPDATE_IMAGES = 3;
    private final static int STATE_DONE = 4;

    private int state = STATE_UPLOAD_MEASUREMENTS;

    final Context myContext;

    public FullUpdater(final Context context) {
        this.myContext = context;
    }

    @Override
    public void checkpointsUpdateTaskDone(boolean allOk) {
        if (allOk == false) {
            Toast.makeText(myContext, R.string.update_checkpoints_not_ok_toast, Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(myContext, "Downloading images", Toast.LENGTH_SHORT).show();
        state = STATE_UPDATE_IMAGES;
        runUpdate();
    }

    public void runUpdate() {
        switch (state) {
        case STATE_UPLOAD_MEASUREMENTS:
            UploadMeasurementsTask uploadTask = new UploadMeasurementsTask(myContext, this);
            uploadTask.execute();
            break;
        case STATE_UPDATE:
            CheckpointsUpdateTask updateTask = new CheckpointsUpdateTask(myContext, this);
            updateTask.execute();
            break;
        case STATE_UPDATE_IMAGES:
            CheckpointsImagesDownloadTask downloadTask = new CheckpointsImagesDownloadTask(myContext, this);
            downloadTask.execute();
            break;
        case STATE_DONE:
            // All done
            Toast.makeText(myContext, R.string.update_info_ok, Toast.LENGTH_SHORT).show();
            break;
        }
    }

    @Override
    public void uploadMeasurementsTaskDone(boolean allOk) {
        if (allOk == false) {
            Toast.makeText(myContext, R.string.upload_data_not_ok_toast, Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(myContext, "Downloading checkpoints", Toast.LENGTH_SHORT).show();
        state = STATE_UPDATE;
        runUpdate();
    }

    @Override
    public void checkpointsImagesDownloadTaskTaskDone(boolean allOk) {
        if (allOk == false) {
            Toast.makeText(myContext, R.string.download_images_not_ok_toast, Toast.LENGTH_LONG).show();
            return;
        }
        state = STATE_DONE;
        runUpdate();
    }
}
