package com.ma.dc;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeCycle {

    private final static int MINUTES_IN_HOUR = 60;
    private final static int HOURS_IN_DAY = 24;
    private final static int DAYS_IN_FULL_WEEK = 7;
    private final static int MINUTE_IN_MILLIS = 60000;

    public final static String HOUR = "hour";
    public final static String DAY = "day";
    public final static String WEEK = "week";
    public final static String MONTH = "month";

    static TimeCycle createCycleFromTime(final int updates, final String timePeriod, final int startTime,
            final int startDay, final long time) {

        final Calendar calendarNow = new GregorianCalendar();
        calendarNow.setTimeInMillis(time);

        final int periodInMinutes = getPeriodInMinutes(updates, timePeriod, calendarNow);

        final Calendar calendarStartTime = createCycleStartTime(updates, timePeriod, startTime, startDay, calendarNow,
                periodInMinutes);

        final Calendar calendarEndTime = createCycleEndTime(updates, timePeriod, startTime, startDay,
                calendarStartTime, calendarNow, periodInMinutes);

        final TimeCycle temporaryTimeCycle = new TimeCycle(calendarStartTime, calendarEndTime, updates, timePeriod,
                startTime, startDay);

        return temporaryTimeCycle;
    }

    private static Calendar createCycleStartTime(final int updates, final String timePeriod, final int startTime,
            final int startDay, final Calendar calendarNow, final int periodInMinutes) {

        final int currentMinute = calendarNow.get(Calendar.MINUTE);
        final int currentHour = calendarNow.get(Calendar.HOUR_OF_DAY);
        final int currentDay = calendarNow.get(Calendar.DAY_OF_YEAR);
        final int currentYear = calendarNow.get(Calendar.YEAR);

        final int daysInCurrentYear = calendarNow.getActualMaximum(Calendar.DAY_OF_YEAR);

        final Calendar calendarStartTime = new GregorianCalendar();
        calendarStartTime.setTime(calendarNow.getTime());
        calendarStartTime.set(Calendar.MINUTE, 0);
        calendarStartTime.set(Calendar.SECOND, 0);
        calendarStartTime.set(Calendar.MILLISECOND, 0);

        if (timePeriod.compareToIgnoreCase(TimeCycle.HOUR) == 0) {
        } else if (timePeriod.compareToIgnoreCase(TimeCycle.DAY) == 0) {
            calendarStartTime.set(Calendar.HOUR_OF_DAY, startTime);

            if (!calendarStartTime.before(calendarNow)) {
                calendarStartTime.add(Calendar.DAY_OF_YEAR, -1);
            }
        } else {
            calendarStartTime.set(Calendar.HOUR_OF_DAY, startTime);
            calendarStartTime.set(Calendar.DAY_OF_WEEK, startDay);

            if (!calendarStartTime.before(calendarNow)) {
                calendarStartTime.add(Calendar.WEEK_OF_YEAR, -1);
            }
        }

        while ((currentYear * daysInCurrentYear * MINUTES_IN_HOUR * HOURS_IN_DAY + currentDay * MINUTES_IN_HOUR
                * HOURS_IN_DAY + currentHour * MINUTES_IN_HOUR + currentMinute) > (calendarStartTime.get(Calendar.YEAR)
                * calendarStartTime.getActualMaximum(Calendar.DAY_OF_YEAR) * MINUTES_IN_HOUR * HOURS_IN_DAY
                + calendarStartTime.get(Calendar.DAY_OF_YEAR) * MINUTES_IN_HOUR * HOURS_IN_DAY
                + calendarStartTime.get(Calendar.HOUR_OF_DAY) * MINUTES_IN_HOUR
                + calendarStartTime.get(Calendar.MINUTE) + periodInMinutes)) {

            calendarStartTime.add(Calendar.MINUTE, periodInMinutes);
        }

        return calendarStartTime;
    }

    private static Calendar createCycleEndTime(final int updates, final String timePeriod, final int startTime,
            final int startDay, final Calendar calendarStartTime, final Calendar calendarNow, final int periodInMinutes) {

        final Calendar calendarEndTime = new GregorianCalendar();
        calendarEndTime.setTime(calendarStartTime.getTime());

        calendarEndTime.add(Calendar.MINUTE, periodInMinutes);

        // START Overflow check

        final int maximumPeriodInMinutes = TimeCycle.getPeriodInMinutes(1, timePeriod, calendarNow);

        final Calendar originalStartTime = new GregorianCalendar();
        originalStartTime.setTime(calendarStartTime.getTime());

        while (((originalStartTime.get(Calendar.HOUR_OF_DAY) != startTime) && (timePeriod.compareTo(TimeCycle.HOUR) != 0))
                || originalStartTime.get(Calendar.MINUTE) != 0) {
            originalStartTime.add(Calendar.MINUTE, -periodInMinutes);
        }

        // Add full period
        originalStartTime.add(Calendar.MINUTE, maximumPeriodInMinutes);

        final int differenceInMinute = (int) ((originalStartTime.getTimeInMillis() - calendarEndTime.getTimeInMillis()) / MINUTE_IN_MILLIS);

        if (differenceInMinute > 0 && differenceInMinute < periodInMinutes) {
            calendarEndTime.add(Calendar.MINUTE, differenceInMinute);
        }

        // END Overflow check

        return calendarEndTime;
    }

    private static int getPeriodInMinutes(final int updates, final String timePeriod, final Calendar calendarNow) {
        final int periodInMinutes;

        if (timePeriod.compareToIgnoreCase(TimeCycle.HOUR) == 0) {
            periodInMinutes = MINUTES_IN_HOUR / updates;
        } else if (timePeriod.compareToIgnoreCase(TimeCycle.DAY) == 0) {
            periodInMinutes = (MINUTES_IN_HOUR * HOURS_IN_DAY) / updates;
        } else if (timePeriod.compareToIgnoreCase(TimeCycle.WEEK) == 0) {
            periodInMinutes = (DAYS_IN_FULL_WEEK * HOURS_IN_DAY * MINUTES_IN_HOUR) / updates;
        } else { // MONTH
            periodInMinutes = (calendarNow.getActualMaximum(Calendar.DAY_OF_MONTH) * HOURS_IN_DAY * MINUTES_IN_HOUR)
                    / updates;
        }

        return periodInMinutes;
    }

    private final Calendar startDate;
    private final Calendar endDate;

    private final int updates;
    private final String timePeriod;
    private final int startDay;
    private final int startTime;

    private TimeCycle(final Calendar startDate, final Calendar endDate, final int updates, final String timePeriod,
            final int startTime, final int startDay) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.updates = updates;
        this.timePeriod = timePeriod;
        this.startTime = startTime;
        this.startDay = startDay;
    }

    long getEndDate() {
        return endDate.getTimeInMillis();
    }

    TimeCycle getNext() {
        final Calendar newCalendarNow = new GregorianCalendar();
        newCalendarNow.setTimeInMillis(endDate.getTimeInMillis() + MINUTE_IN_MILLIS);

        final Calendar newStartDate = new GregorianCalendar();
        newStartDate.setTime(endDate.getTime());

        final int periodInMinutes = TimeCycle.getPeriodInMinutes(updates, timePeriod, newCalendarNow);
        final Calendar newEndDate = TimeCycle.createCycleEndTime(updates, timePeriod, startTime, startDay,
                newStartDate, newCalendarNow, periodInMinutes);

        return new TimeCycle(newStartDate, newEndDate, updates, timePeriod, startTime, startDay);
    }

    TimeCycle getPrevious() {
        final Calendar newCalendarNow = new GregorianCalendar();
        newCalendarNow.setTimeInMillis(startDate.getTimeInMillis() - MINUTE_IN_MILLIS);

        final Calendar newEndDate = new GregorianCalendar();
        newEndDate.setTime(startDate.getTime());

        final int periodInMinutes = TimeCycle.getPeriodInMinutes(updates, timePeriod, newCalendarNow);
        final Calendar newStartDate = TimeCycle.createCycleStartTime(updates, timePeriod, startTime, startDay,
                newCalendarNow, periodInMinutes);

        return new TimeCycle(newStartDate, newEndDate, updates, timePeriod, startTime, startDay);
    }

    int getProgressTowardsEndTimeInPercent(final long time) {
        if (isBefore(time)) {
            return 0;
        }
        if (isAfter(time)) {
            return 100;
        }
        final double transformedTime = time - startDate.getTimeInMillis();
        final double transformedEndTime = endDate.getTimeInMillis() - startDate.getTimeInMillis();

        return Double.valueOf(transformedTime * 100 / transformedEndTime).intValue();
    }

    long getStartDate() {
        return startDate.getTimeInMillis();
    }

    boolean isAfter(final long time) {
        return time > endDate.getTimeInMillis();
    }

    boolean isBefore(final long time) {
        return time < startDate.getTimeInMillis();
    }

    boolean isWithin(final long time) {
        if (isBefore(time)) {
            return false;
        }
        if (isAfter(time)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TimeCycle(");
        sb.append("Start date: ");
        sb.append(startDate.getTime());
        sb.append(", End date: ");
        sb.append(endDate.getTime());
        sb.append(")");
        return sb.toString();
    }

    TimeCycle transferToTime(final long anotherTime) {
        return TimeCycle.createCycleFromTime(updates, timePeriod, startTime, startDay, anotherTime);
    }
}