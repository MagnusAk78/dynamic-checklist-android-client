package com.ma.dc;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;

import com.ma.dc.database.CheckpointListObject;

class CheckpointListViewObj {
    //Database values
    final CheckpointListObject checkpointListObject;
    
    //Resources object
    final Resources res;
    
    private final int progressInPercent;
    private final CheckStatus status;
    
    CheckpointListViewObj(final Cursor cursor, final long now, final Resources res) {
        this.checkpointListObject = new CheckpointListObject(cursor);
        this.res = res;

        if (getLatestMeasurementValue() == -1) {
            status = CheckStatus.OUT_OF_ORDER;
            progressInPercent = 0;
            return;
        }
        
        if (getNextMeasurementTime() < now) {
            status = CheckStatus.ALARM;
            progressInPercent = 100;
            return;
        }
        
        final long totalTimePeriod = getNextMeasurementTime() - getLatestMeasurementTimestamp();
        final long periodSinceLast = now - getLatestMeasurementTimestamp();
        progressInPercent = (int)(100 * ((double)periodSinceLast / (double)totalTimePeriod));
        if(progressInPercent > 50) {
            status = CheckStatus.TIME_TO_CHECK;
        } else {
            status = CheckStatus.CHECK_OK;
        }
    }
    
    CharSequence getTitleText() {
        return checkpointListObject.getOrderNr() + ": " + checkpointListObject.getName();
    }
    
    CharSequence getUpdateFrequencyText() {
        final int nrOfDays = checkpointListObject.getTimeDays();
        final int nrOfHours = checkpointListObject.getTimeHours();
        
        StringBuilder sb = new StringBuilder();
        sb.append(res.getString(R.string.checkpoint_row_period));
        
        sb.append(": ");
        if(nrOfDays > 0) {
            sb.append(nrOfDays);
            sb.append(" ");
            if(nrOfDays > 1) {
                sb.append(res.getText(R.string.days));
            } else {
                sb.append(res.getText(R.string.day));
            }
            if(nrOfHours > 0) {
                sb.append(", ");
            }
        }
        
        if(nrOfHours > 0) {
            sb.append(nrOfHours);
            sb.append(" ");
            if(nrOfHours > 1) {
                sb.append(res.getText(R.string.hours));
            } else {
                sb.append(res.getText(R.string.hour));
            }
        }
        
        return sb.toString();
    }
    
    @SuppressLint("SimpleDateFormat")
	CharSequence getNextCheckTimeText() {
        long nextMeasurementTime = 0;
        if(checkpointListObject.getNextMeasurementTime() != null) {
            nextMeasurementTime = checkpointListObject.getNextMeasurementTime().longValue();
        }
        
        final Calendar nextMeasurementCalendar = new GregorianCalendar();
        nextMeasurementCalendar.setTimeInMillis(nextMeasurementTime);
        
        StringBuilder sb = new StringBuilder();
        sb.append(res.getString(R.string.checkpoint_row_next_check));
        sb.append(": ");
        
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sb.append(sdf.format(nextMeasurementCalendar.getTime()));
        
        return sb.toString();
    }
    
    

    private long getNextMeasurementTime() {
        final Long nextMeasurementTime = checkpointListObject.getNextMeasurementTime();
        return nextMeasurementTime != null ? nextMeasurementTime.longValue() : 0;
    }

    private long getLatestMeasurementTimestamp() {
        return checkpointListObject.getLatestMeasurementTimestamp() != null ? checkpointListObject.getLatestMeasurementTimestamp().longValue() : 0;
    }

    CheckpointListObject getCheckpointListObject() {
        return checkpointListObject;
    }

    int getProgressInPercent() {
        return progressInPercent;
    }

    int getTitleBgColor() {
        switch (status) {
        case ALARM:
            return Color.TRANSPARENT;
        case TIME_TO_CHECK:
            return Color.TRANSPARENT;
        case OUT_OF_ORDER:
            return Color.GRAY;
        case CHECK_OK:
            return Color.TRANSPARENT;
        }

        // This should never happen
        return Color.BLACK;
    }

    boolean isProgressVisible() {
        return status != CheckStatus.OUT_OF_ORDER;
    }

    private int getLatestMeasurementValue() {
        return checkpointListObject.getLatestMeasurementValue() != null ? checkpointListObject.getLatestMeasurementValue().intValue() : 1;
    }

    public int getCheckStatusValue() {
        return status.getValue();
    }
}
