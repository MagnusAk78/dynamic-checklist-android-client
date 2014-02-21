package com.ma.dc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ma.dc.contentprovider.DcContentProvider;
import com.ma.dc.data.InsertNewMeasurementTask;
import com.ma.dc.database.DbCheckpointHelper;
import com.ma.dc.util.LogHelper;
import com.ma.dc.R;

/**
 * A fragment representing a single Checkpoint detail screen. This fragment is
 * either contained in a {@link CheckpointListActivity} in two-pane mode (on
 * tablets) or a {@link CheckpointDetailActivity} on handsets.
 */
public class CheckpointDetailFragment extends Fragment implements OnClickListener,
        InsertNewMeasurementTask.InsertMeasurementCallback {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_ITEM_CHECK_STATUS = "item_check_status";

    private ContentValues checkpointCv;

    private ImageButton btnOk;
    private ImageButton btnOkAfterAction;
    private ImageButton btnNotOk;
    
    private CheckStatus checkOkEnabled = CheckStatus.TIME_TO_CHECK;
    
    private TagChooser actionTagChooser;
    private TagChooser errorTagChooser;

    public interface Callbacks {
        public void onCheckpointDetailDone(CheckpointDetailFragment detailFragment);
    }

    /**
     * The fragment's current callback object, which is notified when the detail
     * is done.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onCheckpointDetailDone(CheckpointDetailFragment detailFragment) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CheckpointDetailFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onCreate");

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.

            final String checkpointId = getArguments().getString(ARG_ITEM_ID);
            checkOkEnabled = CheckStatus.getFromValue(getArguments().getInt(ARG_ITEM_CHECK_STATUS));
            
            LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onCreate", "checkpointId: " + checkpointId);
            
            final Uri uriCheckpoint = Uri.parse(DcContentProvider.CHECKPOINTS_URI + "/" + checkpointId);
            Cursor data = this.getActivity().getContentResolver().query(uriCheckpoint, null, null, null, null);
            if(data.moveToFirst()) {
                LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onCreate", "data.moveToFirst()");
                checkpointCv = DbCheckpointHelper.createCheckpointCvFromDatabase(data);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onDestroy");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_checkpoint_detail, container, false);

        if (checkpointCv != null) {
            ((TextView) rootView.findViewById(R.id.checkpoint_detail_name)).setText(DbCheckpointHelper.getName(checkpointCv));
            ((TextView) rootView.findViewById(R.id.checkpoint_detail_description)).setText(DbCheckpointHelper.getDescription(checkpointCv));
            
            final ImageView imageView = (ImageView) rootView.findViewById(R.id.checkpoint_detail_image_view);
            
            final Bitmap image = DbCheckpointHelper.getImage(this.getActivity(), checkpointCv);
            
            final float proposedWidth = 800;
            final float scale = proposedWidth / (float)image.getWidth();
            
            LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onCreateView", "scale:" + scale);
            
            final int newWidth = (int)(image.getWidth() * scale);
            final int newHeight = (int)(image.getHeight() * scale);
            
            LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onCreateView", "newWidth: " + newWidth);
            LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onCreateView", "newHeight:" + newHeight);
            
            final Bitmap scaledImage = Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
            final BitmapDrawable bd = new BitmapDrawable(getResources(), scaledImage);

            imageView.setImageDrawable(bd);
        }

        btnOk = (ImageButton) rootView.findViewById(R.id.checkpoint_detail_ok_btn);
        btnOkAfterAction = (ImageButton) rootView.findViewById(R.id.checkpoint_detail_ok_after_action_btn);
        btnNotOk = (ImageButton) rootView.findViewById(R.id.checkpoint_detail_not_ok_btn);        

        btnOk.setOnClickListener(this);
        btnOkAfterAction.setOnClickListener(this);
        btnNotOk.setOnClickListener(this);
        
        if(checkOkEnabled == CheckStatus.OUT_OF_ORDER) {            
            btnNotOk.setEnabled(false);
        }
        
        if(checkOkEnabled == CheckStatus.CHECK_OK) {            
            btnOk.setEnabled(false);
        }

        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (view == btnOk) {
            LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onClick", "btnOk clicked");
            InsertNewMeasurementTask insertNewMeasurementTask = new InsertNewMeasurementTask(this, this.getActivity(),
                    checkpointCv, 1, null);
            insertNewMeasurementTask.execute();
        } else if (view == btnOkAfterAction) {
            LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onClick", "btnOkAfterAction clicked");
            alertDialogOkAfterAction();
        } else if (view == btnNotOk) {
            LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onClick", "btnNotOk clicked");
            alertDialogNotOk();
        }
    }
    
    private void alertDialogOkAfterAction() {
        AlertDialog.Builder okAfterActionDialogBuilder = new AlertDialog.Builder(getActivity());
        
        okAfterActionDialogBuilder.setTitle(R.string.dialog_ok_after_action_title);
        
        String[] actionTagList = new String[5];
        actionTagList[0] = DbCheckpointHelper.getActionTag1(checkpointCv);
        actionTagList[1] = DbCheckpointHelper.getActionTag2(checkpointCv);
        actionTagList[2] = DbCheckpointHelper.getActionTag3(checkpointCv);
        actionTagList[3] = DbCheckpointHelper.getActionTag4(checkpointCv);
        actionTagList[4] = getResources().getString(R.string.multi_option_other);
        
        actionTagChooser = new TagChooser(actionTagList, -1);
        
        okAfterActionDialogBuilder.setSingleChoiceItems(actionTagList, -1, actionTagChooser);               
        
        // Setting OK Button
        okAfterActionDialogBuilder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                InsertNewMeasurementTask insertNewMeasurementTask = new InsertNewMeasurementTask(CheckpointDetailFragment.this, 
                        CheckpointDetailFragment.this.getActivity(), checkpointCv, 0, actionTagChooser.getChosenTag());
                insertNewMeasurementTask.execute();
            }
        });
        
        okAfterActionDialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //This does nothing.
            }
        });

        // Showing Alert Message
        okAfterActionDialogBuilder.create().show();
    }

    private void alertDialogNotOk() {
        AlertDialog.Builder notOkDialogBuilder = new AlertDialog.Builder(getActivity());
        
        notOkDialogBuilder.setTitle(R.string.dialog_not_ok_title);
       
        String[] errorTagList = new String[5];
        errorTagList[0] = DbCheckpointHelper.getErrorTag1(checkpointCv);
        errorTagList[1] = DbCheckpointHelper.getErrorTag2(checkpointCv);
        errorTagList[2] = DbCheckpointHelper.getErrorTag3(checkpointCv);
        errorTagList[3] = DbCheckpointHelper.getErrorTag4(checkpointCv);
        errorTagList[4] = getResources().getString(R.string.multi_option_other);
        
        errorTagChooser = new TagChooser(errorTagList, -1);
        
        notOkDialogBuilder.setSingleChoiceItems(errorTagList, -1, errorTagChooser);

        // Setting OK Button
        notOkDialogBuilder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                InsertNewMeasurementTask insertNewMeasurementTask = new InsertNewMeasurementTask(CheckpointDetailFragment.this, 
                        CheckpointDetailFragment.this.getActivity(), checkpointCv, -1, CheckpointDetailFragment.this.errorTagChooser.getChosenTag());
                insertNewMeasurementTask.execute();
            }
        });
        
        notOkDialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //This does nothing.
            }
        });

        // Showing Alert Message
        notOkDialogBuilder.create().show();
    }

    @Override
    public void insertMeasurementDone() {
        mCallbacks.onCheckpointDetailDone(this);
    }
}
