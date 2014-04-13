package com.ma.dc;

import com.ma.customviews.BoxedProgressBar;
import com.ma.dc.R;
import com.ma.dc.util.LogHelper;

import android.content.Context;
import android.database.Cursor;
import android.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CheckpointCursorAdapter extends CursorAdapter {

    static class CheckpointViewHolder {
        TextView titleTexViewt;
        TextView timeToNextTextView;
        TextView updateFreqTextView;
        BoxedProgressBar progressBar;
    }

    private final LayoutInflater mInflater;

    public CheckpointCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }
    
    Context mContext;

    long getProposedUpdateInterval() {
        return 1000;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "bindView");
        
        CheckpointViewHolder checkpointViewHolder = (CheckpointViewHolder) view.getTag();

        if (checkpointViewHolder == null) {
            checkpointViewHolder = new CheckpointViewHolder();

            checkpointViewHolder.titleTexViewt = (TextView) view.findViewById(R.id.checkpoint_row_title);
            checkpointViewHolder.timeToNextTextView = (TextView) view
                    .findViewById(R.id.checkpoint_row_time_to_next);
            checkpointViewHolder.updateFreqTextView = (TextView) view
                    .findViewById(R.id.checkpoint_row_update_freq);
            checkpointViewHolder.progressBar = (BoxedProgressBar) view
                    .findViewById(R.id.checkpoint_row_boxed_progress_bar);

            view.setTag(checkpointViewHolder);
        }
        
        CheckpointListViewObj checkpointListViewObj = 
                new CheckpointListViewObj(cursor, System.currentTimeMillis(), context.getResources());

        checkpointViewHolder.titleTexViewt.setText(checkpointListViewObj.getTitleText());
        checkpointViewHolder.titleTexViewt.setBackgroundColor(checkpointListViewObj.getTitleBgColor());
        
        checkpointViewHolder.timeToNextTextView.setText(checkpointListViewObj.getNextCheckTimeText());
        checkpointViewHolder.updateFreqTextView.setText(checkpointListViewObj.getUpdateFrequencyText());

        if (checkpointListViewObj.isProgressVisible()) {
            checkpointViewHolder.progressBar.setVisibility(View.VISIBLE);
            checkpointViewHolder.progressBar.setProgressInPercent(checkpointListViewObj.getProgressInPercent());
        } else {
            checkpointViewHolder.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return mInflater.inflate(R.layout.checkpoint_row_layout, null);
    }

    @Override
    public Object getItem(int position) {
        final Cursor cursor = getCursor();
        if(cursor.moveToPosition(position)) {
            return new CheckpointListViewObj(cursor, System.currentTimeMillis(), mContext.getResources());
        } else {
            return null;
        }
    }
}
