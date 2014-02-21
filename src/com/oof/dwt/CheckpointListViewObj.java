package com.oof.dwt;

import java.util.Comparator;
import java.util.Date;

import com.oof.dwt.database.DbCheckpointHelper;
import android.content.ContentValues;
import android.content.res.Resources;
import android.graphics.Color;

class CheckpointListViewObj {

    private final ContentValues checkpointCv;

    // Labels
    private final CharSequence titleText;
    private final int orderNr;
    private final CharSequence updateFreqText;

    // Calculation objects
    private TimeCycle currentTimeCycle;

    // Compare values for sorting
    private long timeToNextRequriedCheck = 0;

    private boolean enabled;

    private boolean lastValueOk = true;

    private CheckStatus status;
    private CharSequence timeToNextCheckText;

    private int progressInPercent;
    private boolean progressVisible = true;

    private final static long millisInSecond = 1000;
    private final static long secondsInMinute = 60;
    private final static long secondsInHour = secondsInMinute * 60;
    private final static long secondsInDay = secondsInHour * 24;

    static Comparator<CheckpointListViewObj> getComparator() {
        return new Comparator<CheckpointListViewObj>() {

            @Override
            public int compare(CheckpointListViewObj lhs, CheckpointListViewObj rhs) {
                if (SettingsFragment.getCheckpointSortOrder() == SettingsFragment.CHECKPOINT_SORT_ORDER_TYPE.DYMANIC) {
                    if (lhs.status == rhs.status) {
                        return (int) (lhs.timeToNextRequriedCheck - rhs.timeToNextRequriedCheck);
                    } else {
                        return (int) (rhs.status.getValue() - lhs.status.getValue());
                    }
                } else {
                    return (int) (lhs.orderNr - rhs.orderNr);
                }
            }
        };
    }

    private static void appendMillisAsUnitsToStringBuffer(final StringBuffer sb, final long millis,
            final Resources res, final boolean singularValue) {
        final long seconds = millis / millisInSecond;

        if (seconds < secondsInMinute) {
            if (seconds > 1) {
                sb.append(seconds);
                sb.append(" ");
                sb.append(res.getString(R.string.seconds));
            } else {
                if (singularValue) {
                    sb.append(seconds);
                    sb.append(" ");
                }
                sb.append(res.getString(R.string.second));
            }
        } else if (seconds < (secondsInHour * 2)) {
            long minutes = seconds / secondsInMinute;
            if (minutes > 1) {
                sb.append(minutes);
                sb.append(" ");
                sb.append(res.getString(R.string.minutes));
            } else {
                if (singularValue) {
                    sb.append(minutes);
                    sb.append(" ");
                }
                sb.append(res.getString(R.string.minute));
            }
        } else if (seconds < (secondsInDay * 2)) {
            long hours = seconds / secondsInHour;
            if (hours > 1) {
                sb.append(hours);
                sb.append(" ");
                sb.append(res.getString(R.string.hours));
            } else {
                if (singularValue) {
                    sb.append(hours);
                    sb.append(" ");
                }
                sb.append(res.getString(R.string.hour));
            }
        } else {
            long days = seconds / secondsInDay;
            if (days > 1) {
                sb.append(days);
                sb.append(" ");
                sb.append(res.getString(R.string.days));
            } else {
                if (singularValue) {
                    sb.append(days);
                    sb.append(" ");
                }
                sb.append(res.getString(R.string.day));
            }
        }
    }

    private static CharSequence getTimeToNextCheckString(final Resources res, final long timeToCheck) {
        StringBuffer sb = new StringBuffer();
        sb.append(res.getString(R.string.checkpoint_row_time_to_next_check_text));
        sb.append(": ");
        if (timeToCheck >= 0) {
            sb.append(res.getString(R.string.in_time));
            sb.append(" ");
        }

        appendMillisAsUnitsToStringBuffer(sb, Math.abs(timeToCheck), res, true);

        if (timeToCheck < 0) {
            sb.append(" ");
            sb.append(res.getString(R.string.time_ago));
        }

        return sb.toString();
    }

    private static CharSequence getUpdateFreqString(final Resources res, final int updates, final String timePeriod) {
        StringBuffer sb = new StringBuffer();
        String updateEvery = res.getString(R.string.checkpoint_row_update_frequency_text);
        
        sb.append(updates);
        sb.append(" ");
        sb.append(updateEvery);
        sb.append(" ");
        sb.append(timePeriod);

        return sb.toString();
    }

    CheckpointListViewObj(final ContentValues checkpointCv, final Date now, final Resources res) {
        this.checkpointCv = checkpointCv;
        titleText = DbCheckpointHelper.getOrderNr(checkpointCv) + ": " + DbCheckpointHelper.getName(checkpointCv);
        orderNr = DbCheckpointHelper.getOrderNr(checkpointCv);
        
        int updates = DbCheckpointHelper.getUpdates(checkpointCv).intValue();
        int startTime = DbCheckpointHelper.getStartTime(checkpointCv).intValue();
        
        String timePeriod = DbCheckpointHelper.getTimePeriod(checkpointCv);
        
        int startDay = DbCheckpointHelper.getStartDay(checkpointCv).intValue();
        boolean includeWeekends = DbCheckpointHelper.getIncludeWeekends(checkpointCv).booleanValue();
        
        
        updateFreqText = getUpdateFreqString(res, updates, timePeriod);

        
        //LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "CheckpointListViewObj","startDate: " + startDate);
        //LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "CheckpointListViewObj","updateFrequency: " + updateFrequency);
        
        currentTimeCycle = TimeCycle.getCurrentCycle(updates, timePeriod, startTime, startDay, includeWeekends, now);
        updateValues(now, res);
    }

    ContentValues getCheckpointContentValues() {
        return checkpointCv;
    }

    int getProgressInPercent() {
        return progressInPercent;
    }

    CharSequence getTimeToNextCheckText() {
        return timeToNextCheckText;
    }

    long getTimeToNextRequriedCheck() {
        return timeToNextRequriedCheck;
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

    CharSequence getTitleText() {
        return titleText;
    }

    CharSequence getUpdateFreqText() {
        return updateFreqText;
    }

    boolean isEnabled() {
        return enabled;
    }

    boolean isProgressVisible() {
        return progressVisible;
    }

    void updateValues(final Date now, final Resources res) {
        Long latestMeasurementDateLong = DbCheckpointHelper.getLatestMeasurementDate(checkpointCv);       
        if (latestMeasurementDateLong == null) {
        	latestMeasurementDateLong = now.getTime();
        }
        Date latestMeasurementDate = new Date(latestMeasurementDateLong.longValue());
        
        enabled = Boolean.TRUE;

        if (!currentTimeCycle.isWithin(now)) {
            currentTimeCycle = currentTimeCycle.getNext();
        }

        Integer latestMeasurementValue = DbCheckpointHelper.getLatestMeasuredValue(checkpointCv);
        if (latestMeasurementValue != null && latestMeasurementValue.intValue() == -1) {
            lastValueOk = false;
            status = CheckStatus.OUT_OF_ORDER;
            progressVisible = false;
        } else {
            lastValueOk = true;
            status = CheckStatus.TIME_TO_CHECK;
            progressVisible = true;
        }

        if (currentTimeCycle.isWithin(latestMeasurementDate)) {
            // All is well, the progress bar should be empty

            timeToNextRequriedCheck = currentTimeCycle.getNext().getEndDate().getTime() - latestMeasurementDate.getTime();
            timeToNextCheckText = getTimeToNextCheckString(res, timeToNextRequriedCheck);

            if (lastValueOk) {
                status = CheckStatus.CHECK_OK;
                enabled = Boolean.FALSE;
            }
            progressInPercent = 0;
            return;
        }

        if (currentTimeCycle.getPrevious().isBefore(latestMeasurementDate)) {
            // The horror, measurement was not done in previous cycle

            final TimeCycle measuredTimeCycle = TimeCycle.getCurrentCycle(
                    DbCheckpointHelper.getUpdates(checkpointCv), DbCheckpointHelper.getTimePeriod(checkpointCv), DbCheckpointHelper.getStartTime(checkpointCv), 
                    DbCheckpointHelper.getStartDay(checkpointCv), DbCheckpointHelper.getIncludeWeekends(checkpointCv), now);

            timeToNextRequriedCheck = measuredTimeCycle.getNext().getEndDate().getTime() - now.getTime();
            timeToNextCheckText = getTimeToNextCheckString(res, timeToNextRequriedCheck);

            if (lastValueOk) {
                status = CheckStatus.ALARM;
            }
            progressInPercent = 100;
            return;
        }

        timeToNextRequriedCheck = currentTimeCycle.getEndDate().getTime() - now.getTime();
        timeToNextCheckText = getTimeToNextCheckString(res, timeToNextRequriedCheck);

        if (lastValueOk) {
            progressInPercent = currentTimeCycle.getProgressBetween(now);
        }
    }

    public int getCheckStatusValue() {
        return status.getValue();
    }
}
