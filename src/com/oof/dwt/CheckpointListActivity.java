package com.oof.dwt;

import com.oof.dwt.database.DbCheckpointHelper;
import com.oof.dwt.util.LogHelper;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * An activity representing a list of Checkpoints. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link CheckpointDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link CheckpointListFragment} and the item details (if present) is a
 * {@link CheckpointDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link CheckpointListFragment.Callbacks} interface to listen for item
 * selections.
 */
public class CheckpointListActivity extends FragmentActivity implements CheckpointListFragment.Callbacks,
        CheckpointDetailFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onCreate");

        setContentView(R.layout.activity_checkpoint_list);

        if (findViewById(R.id.checkpoint_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((CheckpointListFragment) getSupportFragmentManager().findFragmentById(R.id.checkpoint_list))
                    .setActivateOnItemClick(true);
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        Common.listActivity = this;
    }
    
    

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onConfigurationChanged");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onDestroy");
    }



    /**
     * Callback method from {@link CheckpointListFragment.Callbacks} indicating
     * that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(CheckpointListViewObj checkpointObj) {
        String checkpointId = DbCheckpointHelper.getId(checkpointObj.getCheckpointContentValues());
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onItemSelected", "id: " + checkpointId);

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            final Bundle arguments = new Bundle();
            arguments.putString(CheckpointDetailFragment.ARG_ITEM_ID, checkpointId);
            arguments.putInt(CheckpointDetailFragment.ARG_ITEM_CHECK_STATUS, checkpointObj.getCheckStatusValue());
            CheckpointDetailFragment fragment = new CheckpointDetailFragment();
            fragment.setArguments(arguments);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.checkpoint_detail_container, fragment);
            // if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
            // //transaction.addToBackStack(null);
            // }
            transaction.commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, CheckpointDetailActivity.class);
            detailIntent.putExtra(CheckpointDetailFragment.ARG_ITEM_ID, checkpointId);
            detailIntent.putExtra(CheckpointDetailFragment.ARG_ITEM_CHECK_STATUS, checkpointObj.getCheckStatusValue());
            startActivity(detailIntent);
        }
    }

    @Override
    public void onCheckpointDetailDone(CheckpointDetailFragment detailFragment) {
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onCheckpointDetailDone");
        
        final Fragment activeDetailFragment = getSupportFragmentManager().findFragmentById(R.id.checkpoint_detail_container);
        
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.detach(activeDetailFragment);
        fragmentTransaction.commit();
        
        clearListViewChoices();
    }

    @Override
    public void onBackPressed() {
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onBackPressed");

        if (!mTwoPane) {
            // This is just showing the list view, just pass the back press, it
            // will close the application

            super.onBackPressed();
            return;
        }

        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onBackPressed", "mTwoPane");

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment activeDetailFragment = fragmentManager.findFragmentById(R.id.checkpoint_detail_container);

        if (activeDetailFragment == null || !activeDetailFragment.isVisible()) {
            // No checkpoint details are visible, just pass the back press, it
            // will close the application

            super.onBackPressed();
            return;
        }

        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onBackPressed", "activeDetailFragment != null");

        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.detach(activeDetailFragment);
        fragmentTransaction.commit();

        clearListViewChoices();
    }

    private void clearListViewChoices() {
        final CheckpointListFragment checkpointListFragment = (CheckpointListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.checkpoint_list);
        checkpointListFragment.getListView().clearChoices();
        checkpointListFragment.getListView().requestLayout();
    }

}
