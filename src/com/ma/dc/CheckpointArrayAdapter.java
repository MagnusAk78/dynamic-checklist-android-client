package com.ma.dc;

import com.ma.customviews.BoxedProgressBar;
import com.ma.dc.R;
import com.ma.dc.util.LogHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CheckpointArrayAdapter extends ArrayAdapter<CheckpointListViewObj> {

    static class CheckpointViewHolder {
        TextView titleTexViewt;
        TextView timeToNextTextView;
        TextView updateFreqTextView;
        BoxedProgressBar progressBar;
    }

    private final LayoutInflater mInflater;
    private long proposedUpdateInterval;

    public CheckpointArrayAdapter(Context context, int resource) {
        super(context, resource);

        mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.setNotifyOnChange(false);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {        
        final CheckpointViewHolder checkpointViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.checkpoint_row_layout, null);

            checkpointViewHolder = new CheckpointViewHolder();

            checkpointViewHolder.titleTexViewt = (TextView) convertView.findViewById(R.id.checkpoint_row_title);
            checkpointViewHolder.timeToNextTextView = (TextView) convertView
                    .findViewById(R.id.checkpoint_row_time_to_next);
            checkpointViewHolder.updateFreqTextView = (TextView) convertView
                    .findViewById(R.id.checkpoint_row_update_freq);
            checkpointViewHolder.progressBar = (BoxedProgressBar) convertView.
                    findViewById(R.id.checkpoint_row_boxed_progress_bar);

            convertView.setTag(checkpointViewHolder);
        } else {
            checkpointViewHolder = (CheckpointViewHolder) convertView.getTag();
        }

        CheckpointListViewObj checkpointObj = this.getItem(position);

        checkpointViewHolder.titleTexViewt.setText(checkpointObj.getTitleText());
        checkpointViewHolder.titleTexViewt.setBackgroundColor(checkpointObj.getTitleBgColor());
        checkpointViewHolder.timeToNextTextView.setText(checkpointObj.getTimeToNextCheckText());
        checkpointViewHolder.updateFreqTextView.setText(checkpointObj.getUpdateFreqText());

        if (checkpointObj.isProgressVisible()) {
            checkpointViewHolder.progressBar.setVisibility(View.VISIBLE);
            checkpointViewHolder.progressBar.setProgressInPercent(checkpointObj.getProgressInPercent());
        } else {
            checkpointViewHolder.progressBar.setVisibility(View.INVISIBLE);
        }
        
        return convertView;
    }

    void updateAllValues() {
    	
    	LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "updateAllValues");
    	
        final long now = System.currentTimeMillis();
        long lowestTimeToCheck = Long.MAX_VALUE;
        for (int i = 0; i < this.getCount(); i++) {
            this.getItem(i).updateValues(now, getContext().getResources());
            lowestTimeToCheck = Math.min(lowestTimeToCheck, Math.abs(this.getItem(i).getTimeToNextRequriedCheck()));
        }

        if (lowestTimeToCheck < 120000) {
            proposedUpdateInterval = 850;
        } else if (lowestTimeToCheck < 3600000) {
            proposedUpdateInterval = 5000;
        } else {
            proposedUpdateInterval = 10000;
        }
    }

    long getProposedUpdateInterval() {
        return proposedUpdateInterval;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }
}
