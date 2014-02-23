package com.ma.dc;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.ma.dc.util.LogHelper;


class TimeCycle {
    private final Calendar startDate;
    private final Calendar endDate;
    
    private final int updates;
	private final int startTime;
	private final String timePeriod;
	private final int startDay;
	private final boolean includeWeekends;
    
    private final static int MINUTES_IN_HOUR = 60;
    private final static int HOURS_IN_DAY = 24;
    private final static int DAYS_IN_FULL_WEEK = 7;
    private final static int DAYS_IN_MONTH = 30;
    
    private static boolean inWeekend(final Calendar date) {
    	final int dayInWeek = date.get(Calendar.DAY_OF_WEEK);
    	if(dayInWeek == Calendar.SATURDAY || dayInWeek == Calendar.SUNDAY) {
    		return true;
    	}
    	return false;
    }
    
    private static TimeCycle createCycleFromTime(final int updates, final String timePeriod, final int startTime, 
    		final int startDay, final boolean includeWeekends, final Date time) {

    	Calendar calendarNow = new GregorianCalendar();
    	calendarNow.setTime(time);
    	
    	LogHelper.logDebug(TimeCycle.class, Common.LOG_TAG_MAIN, "createCycleFromTime", "time: " + calendarNow.getTimeInMillis());
    	
    	int currentMinute = calendarNow.get(Calendar.MINUTE);
    	int currentHour = calendarNow.get(Calendar.HOUR_OF_DAY);
    	int currentDay = calendarNow.get(Calendar.DAY_OF_YEAR);
    			
    	Calendar calendarStartTime = new GregorianCalendar();
    	calendarStartTime.setTime(calendarNow.getTime());
    	calendarStartTime.set(Calendar.MINUTE, 0);
    	calendarStartTime.set(Calendar.SECOND, 0);
    	calendarStartTime.set(Calendar.MILLISECOND, 0);
    	
    	Calendar calendarEndTime = new GregorianCalendar();
    	calendarEndTime.setTime(calendarNow.getTime());
    	calendarEndTime.set(Calendar.MINUTE, 0);
    	calendarEndTime.set(Calendar.SECOND, 0);
    	calendarEndTime.set(Calendar.MILLISECOND, 0);
    	
    	if(timePeriod.compareToIgnoreCase("HOUR") == 0) {
    		LogHelper.logDebug(TimeCycle.class, Common.LOG_TAG_MAIN, "createCycleFromTime", "timePeriod: " + timePeriod);
    		
    		int periodInMinutes = MINUTES_IN_HOUR / updates;
    		
    		LogHelper.logDebug(TimeCycle.class, Common.LOG_TAG_MAIN, "createCycleFromTime", "periodInMinutes: " + periodInMinutes);
    		
    		while(currentMinute > (calendarStartTime.get(Calendar.MINUTE) + periodInMinutes)) {
    			calendarStartTime.add(Calendar.MINUTE, periodInMinutes);
    			calendarEndTime.add(Calendar.MINUTE, periodInMinutes);
    		}
    		calendarEndTime.add(Calendar.MINUTE, periodInMinutes);
    		
    		LogHelper.logDebug(TimeCycle.class, Common.LOG_TAG_MAIN, "createCycleFromTime", "calendarStartTime: " + calendarStartTime.getTimeInMillis());
    		LogHelper.logDebug(TimeCycle.class, Common.LOG_TAG_MAIN, "createCycleFromTime", "calendarEndTime: " + calendarEndTime.getTimeInMillis());
    		
    	} else if(timePeriod.compareToIgnoreCase("DAY") == 0) {    	
    		calendarStartTime.set(Calendar.HOUR_OF_DAY, startTime);
    		calendarEndTime.set(Calendar.HOUR_OF_DAY, startTime);
    		
    		int periodInMinutes = (MINUTES_IN_HOUR * HOURS_IN_DAY) / updates;
    		
    		while((currentHour * MINUTES_IN_HOUR + currentMinute) > (calendarStartTime.get(Calendar.HOUR_OF_DAY) * MINUTES_IN_HOUR + calendarStartTime.get(Calendar.MINUTE) + periodInMinutes)) {
    			calendarStartTime.add(Calendar.MINUTE, periodInMinutes);
    			calendarEndTime.add(Calendar.MINUTE, periodInMinutes);
    		}
    		calendarEndTime.add(Calendar.MINUTE, periodInMinutes);
    		
    	} else if(timePeriod.compareToIgnoreCase("WEEK") == 0) {
    		calendarStartTime.set(Calendar.HOUR_OF_DAY, startTime);
    		calendarEndTime.set(Calendar.HOUR_OF_DAY, startTime);
    		
    		calendarStartTime.set(Calendar.DAY_OF_WEEK, startDay);
    		if(!calendarStartTime.before(calendarNow)) {
    			calendarStartTime.add(Calendar.WEEK_OF_YEAR, -1);
    		}
    		calendarEndTime.set(Calendar.WEEK_OF_YEAR, calendarStartTime.get(Calendar.WEEK_OF_YEAR));
    		
    		int periodInHours = (DAYS_IN_FULL_WEEK * HOURS_IN_DAY) / updates;
    		
    		while(currentHour > (calendarStartTime.get(Calendar.HOUR) + periodInHours)) {
    			calendarStartTime.add(Calendar.HOUR, periodInHours);
    			calendarEndTime.add(Calendar.HOUR, periodInHours);
    		}
    		calendarEndTime.add(Calendar.HOUR, periodInHours);
    	} else { //MONTH
    		calendarStartTime.set(Calendar.HOUR_OF_DAY, startTime);
    		calendarEndTime.set(Calendar.HOUR_OF_DAY, startTime);
    		
    		calendarStartTime.set(Calendar.DAY_OF_WEEK, startDay);
    		if(!calendarStartTime.before(calendarNow)) {
    			calendarStartTime.add(Calendar.WEEK_OF_YEAR, -1);
    		}
    		calendarEndTime.set(Calendar.WEEK_OF_YEAR, calendarStartTime.get(Calendar.WEEK_OF_YEAR));
    		
    		int periodInDays = DAYS_IN_MONTH / updates;
    		
    		while(currentDay > (calendarStartTime.get(Calendar.DAY_OF_YEAR) + periodInDays)) {
    			calendarStartTime.add(Calendar.DAY_OF_YEAR, periodInDays);
    			calendarEndTime.add(Calendar.DAY_OF_YEAR, periodInDays);
    		}
    		calendarEndTime.add(Calendar.DAY_OF_YEAR, periodInDays);
    	}
    	
    	return new TimeCycle(calendarStartTime, calendarEndTime, updates, timePeriod, startTime, startDay, includeWeekends);
    }
    
    static TimeCycle createCurrentFromCheckpoint(final int updates, final String timePeriod, final int startTime, 
    		final int startDay, final boolean includeWeekends) {
    	return createCycleFromTime(updates, timePeriod, startTime, startDay, includeWeekends, new Date());
    }

    private TimeCycle(final Calendar startDate, final Calendar endDate, final int updates, final String timePeriod, final int startTime, 
    		final int startDay, final boolean includeWeekends) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.updates = updates;
        this.timePeriod = timePeriod;
        this.startTime = startTime;
        this.startDay = startDay;
        this.includeWeekends = includeWeekends;
    }

    Date getEndDate() {
        return endDate.getTime();
    }
    
    long getTotalTimeBetween() {
        return endDate.getTimeInMillis() - startDate.getTimeInMillis();
    }

    int getProgressBetween(final Date time) {
        if (isBefore(time)) {
            return 0;
        }
        if (isAfter(time)) {
            return 100;
        }
        final double transformedTime = time.getTime() - startDate.getTimeInMillis();
        final double transformedEndTime = endDate.getTimeInMillis() - startDate.getTimeInMillis();

        return Double.valueOf(transformedTime * 100 / transformedEndTime).intValue();
    }

    Date getStartDate() {
        return startDate.getTime();
    }

    private boolean isAfter(final Date time) {
    	return time.after(endDate.getTime());
    }

    boolean isBefore(final Date time) {
    	return time.before(startDate.getTime());
    }

    boolean isWithin(final Date time) {
        if(isBefore(time)) {
        	return false;            
        }
        if(isAfter(time)) {
        	return false;
        }
        return true;
    }
    
    TimeCycle getNext() {
    	return transferToTime(getAfterEnd().getTime());
    }
    
    TimeCycle getPrevious() {
    	return transferToTime(getBeforeStart().getTime());
    }
    
    private Calendar getBeforeStart() {
    	Calendar beforeStart = new GregorianCalendar();
        beforeStart.setTime(getStartDate());
        beforeStart.add(Calendar.MINUTE, -2);
        return beforeStart;
    }
    
    private Calendar getAfterEnd() {
    	Calendar afterEnd = new GregorianCalendar();
    	afterEnd.setTime(getEndDate());
    	afterEnd.add(Calendar.MINUTE, 2);
    	return afterEnd;
    }
    
    TimeCycle transferToTime(final Date anotherDate) {
    	return createCycleFromTime(updates, timePeriod, startTime, startDay, includeWeekends, anotherDate);
    }
}