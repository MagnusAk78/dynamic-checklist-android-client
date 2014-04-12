package com.ma.dc;

import java.util.Comparator;

import android.content.res.Resources;
import android.graphics.Color;

import com.ma.dc.database.CheckpointListObject;
import com.ma.dc.util.LogHelper;

class CheckpointListViewObj {
    private final CheckpointListObject checkpointListObject;

    // Labels
    private final CharSequence titleText;
    private final int orderNr;
    private final CharSequence updateFreqText;

    // Calculation objects
    private TimeCycle currentTimeCycle;
    private TimeCycle nextTimeCycle;
    private TimeCycle previousTimeCycle;
    private long lastMeasurementTimeCycleNextEndTimeMillis = 0;

    private long lastMeasurementTime;
    private long lastMeasurementValue = 1;

    private int progressInPercent;

    // Compare values for sorting
    private long timeToNextRequriedCheck = 0;

    private CheckStatus status;
    private CharSequence timeToNextCheckText;

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

        if (seconds <= secondsInMinute) {
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
        } else if (seconds < (secondsInMinute * 2)) {
            sb.append("1:");
            sb.append(seconds - secondsInMinute);
            sb.append(" ");
            sb.append(res.getString(R.string.minutes));
        } else if (seconds < (secondsInHour * 2)) {
            long minutes = (seconds / secondsInMinute) + 1;
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
        sb.append(" ");
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

    CheckpointListViewObj(final CheckpointListObject checkpointListObject, final long now, final Resources res) {
        this.checkpointListObject = checkpointListObject;
        titleText = checkpointListObject.getOrderNr() + ": " + checkpointListObject.getName();
        orderNr = checkpointListObject.getOrderNr();

        updateFreqText = getUpdateFreqString(res, checkpointListObject.getUpdates(),
                checkpointListObject.getTimePeriod());

        currentTimeCycle = TimeCycle.createCycleFromTime(checkpointListObject.getUpdates(),
                checkpointListObject.getTimePeriod(), checkpointListObject.getStartTime(),
                checkpointListObject.getStartDay(), now);
        nextTimeCycle = currentTimeCycle.getNext();
        previousTimeCycle = currentTimeCycle.getPrevious();

        updateStatusValues();
        updateTimeValues(now);
        updateTextValues(res);
    }

    CheckpointListObject getCheckpointListObject() {
        return checkpointListObject;
    }

    int getProgressInPercent() {
        return progressInPercent;
    }

    CharSequence getTimeToNextCheckText() {
        return timeToNextCheckText;
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

    boolean isProgressVisible() {
        return progressVisible;
    }

    long updateValues(final long now, final Resources res) {
        updateTimeCycle(now);
        updateStatusValues();
        if (lastMeasurementValue != -1) {
            updateTimeValues(now);
            updateTextValues(res);
        }

        return timeToNextRequriedCheck;
    }

    private void updateTimeCycle(final long now) {
        if (!currentTimeCycle.isWithin(now)) {
            previousTimeCycle = currentTimeCycle;
            currentTimeCycle = currentTimeCycle.getNext();
            nextTimeCycle = currentTimeCycle.getNext();

            LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "UpdateTask", "updateTimeCycle: " + currentTimeCycle);
        }
    }

    private void updateStatusValues() {
        if (checkpointListObject.getLatestMeasurementTimestamp() != null
                && checkpointListObject.getLatestMeasurementValue() != null
                && lastMeasurementTime != checkpointListObject.getLatestMeasurementTimestamp().longValue()) {
            lastMeasurementTime = checkpointListObject.getLatestMeasurementTimestamp().longValue();
            lastMeasurementValue = checkpointListObject.getLatestMeasurementValue().intValue();

            final TimeCycle afterLastMeasurementTimeCycle = currentTimeCycle.transferToTime(lastMeasurementTime)
                    .getNext();

            lastMeasurementTimeCycleNextEndTimeMillis = afterLastMeasurementTimeCycle.getEndDate();
        }

        if (lastMeasurementValue == -1) {
            status = CheckStatus.OUT_OF_ORDER;
            progressVisible = false;
        } else {
            progressVisible = true;
        }
    }

    private void updateTimeValues(final long now) {
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "updateTimeValues");

        if (previousTimeCycle.isBefore(lastMeasurementTime)) {
            // Measurement was done before previous cycle

            if (lastMeasurementTimeCycleNextEndTimeMillis != 0) {
                timeToNextRequriedCheck = lastMeasurementTimeCycleNextEndTimeMillis - now;
            } else {
                timeToNextRequriedCheck = previousTimeCycle.getEndDate() - now;
            }

            status = CheckStatus.ALARM;
            progressInPercent = 100;

            return;
        }

        if (currentTimeCycle.isWithin(lastMeasurementTime)) {
            // All is well, the progress bar should be empty
            status = CheckStatus.CHECK_OK;
            progressInPercent = 0;

            timeToNextRequriedCheck = nextTimeCycle.getEndDate() - now;
        } else {
            LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "updateTimeValues", "default");

            status = CheckStatus.TIME_TO_CHECK;
            progressInPercent = currentTimeCycle.getProgressTowardsEndTimeInPercent(now);

            timeToNextRequriedCheck = currentTimeCycle.getEndDate() - now;
        }
    }

    private void updateTextValues(final Resources res) {
        timeToNextCheckText = getTimeToNextCheckString(res, timeToNextRequriedCheck);
    }

    public int getCheckStatusValue() {
        return status.getValue();
    }
}
