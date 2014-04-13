package com.ma.dc;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.ma.dc.data.FullUpdater;
import com.ma.dc.database.CheckpointListObject;
import com.ma.dc.database.DcContentProvider;
import com.ma.dc.util.LogHelper;
import com.ma.dc.R;

/**
 * A list fragment representing a list of Checkpoints. This fragment also
 * supports tablet devices by allowing list items to be given an 'activated'
 * state upon selection. This helps indicate which item is currently being
 * viewed in a {@link CheckpointDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class CheckpointListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(CheckpointListViewObj checkpointListViewObj);

        /**
         * Callback for long clicks
         */
        public void onItemLongClick(CheckpointListViewObj checkpointListViewObj);
    }

    private UpdateTask mUpdateTask = new UpdateTask();

    private class UpdateTask extends Thread implements Runnable {

        public void run() {
            LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "UpdateTask.run");
            //myAdapter.updateAllValues();
            myAdapter.notifyDataSetChanged();
            long proposedUpdateInterval = myAdapter.getProposedUpdateInterval();

            mHandler.postDelayed(this, proposedUpdateInterval);
        }
    }

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(CheckpointListViewObj checkpointListViewObj) {
        }

        public void onItemLongClick(CheckpointListViewObj checkpointListViewObj) {
        }
    };

    private CheckpointCursorAdapter myAdapter;

    private Handler mHandler = new Handler();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CheckpointListFragment() {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onCreate");

        this.setRetainInstance(true);

        getLoaderManager().initLoader(0, null, this);
        myAdapter = new CheckpointCursorAdapter(this.getActivity(), null, 0);
        setListAdapter(myAdapter);

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);

        this.getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                mCallbacks.onItemLongClick((CheckpointListViewObj)myAdapter.getItem(pos));
                return true;
            }
        });

        this.getListView().setLongClickable(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onDestroy");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String sortOrder;
        if(SettingsFragment.getCheckpointSortOrder() == SettingsFragment.CHECKPOINT_SORT_ORDER_TYPE.DYMANIC) {
            sortOrder = CheckpointListObject.getColumnNextMeasurementTime();
        } else {
            sortOrder = CheckpointListObject.getColumnOrderNr();
        }

        return new CursorLoader(this.getActivity(), DcContentProvider.CHECKPOINTS_URI, CheckpointListObject.PROJECTION,
                null, null, sortOrder);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.checklists_actions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onListItemClick");
        super.onListItemClick(listView, view, position, id);

        mCallbacks.onItemSelected((CheckpointListViewObj)myAdapter.getItem(position));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onLoadFinished");

        myAdapter.swapCursor(newCursor);
        mHandler.postDelayed(mUpdateTask, 100);
    }
    
    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        myAdapter.swapCursor(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_refresh:
            LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onOptionsItemSelected", "action_refresh pressed");

            FullUpdater updater = new FullUpdater(getActivity());
            updater.runUpdate();
            return true;
        case R.id.action_settings:
            LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onOptionsItemSelected", "action_settings pressed");

            final Intent intent = new Intent(this.getActivity(), SettingsActivity.class);
            this.startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onStop");

        super.onStop();

        mHandler.removeCallbacks(mUpdateTask);

        getLoaderManager().getLoader(0).stopLoading();
    }

    @Override
    public void onStart() {
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onStart");

        super.onStart();
        
        final String cloudantName = SettingsFragment.getCloudantName(this.getActivity());
        final String databaseName = SettingsFragment.getDatabaseName(this.getActivity());
        this.getActivity().setTitle(cloudantName + "/" + databaseName);

        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
