package com.ma.dc;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

class TimeCycle {
    private final Calendar startDate;
    private final Calendar endDate;
    private final int minutes;
    private final boolean includeWeekends;
    
    private static int getMinutes(final int currentField, final int updates, final int multiplier) {
    	if(currentField == Calendar.MINUTE) {
    		return multiplier / updates;
    	}
    	if(currentField == Calendar.HOUR_OF_DAY) {
    		return getMinutes(Calendar.MINUTE, updates, multiplier * 60);
    	} else if(currentField == Calendar.DAY_OF_WEEK) {
    		return getMinutes(Calendar.HOUR_OF_DAY, updates, multiplier * 24);
    	} else if(currentField == Calendar.WEEK_OF_YEAR) {
    		return getMinutes(Calendar.DAY_OF_WEEK, updates, multiplier * 7);
    	} else { //MONTH
    		return getMinutes(Calendar.DAY_OF_WEEK, updates, multiplier * 30);
    	}
    }
    
    static TimeCycle getCurrentCycle(final int updates, final String timePeriod, final int startTime, 
    		final int startDay, final boolean includeWeekends, final Date now) {
    	
    	Calendar calendarNow = new GregorianCalendar();
    	calendarNow.setTime(now);
    	
    	Calendar calendarStartPeriod = new GregorianCalendar();
    	calendarStartPeriod.setTime(now);
    	calendarStartPeriod.set(Calendar.DAY_OF_WEEK, startDay);
    	calendarStartPeriod.set(Calendar.HOUR_OF_DAY, startTime);
    	
    	if(calendarStartPeriod.after(calendarNow)) {
    		calendarStartPeriod.add(Calendar.WEEK_OF_YEAR, -1);
    	}
    	
    	int periodField;
    	if(timePeriod.compareToIgnoreCase("HOUR") == 0) {
    		periodField = Calendar.HOUR_OF_DAY;
    	} else if(timePeriod.compareToIgnoreCase("DAY") == 0) {
    		periodField = Calendar.DAY_OF_WEEK;
    	} else if(timePeriod.compareToIgnoreCase("WEEK") == 0) {
    		periodField = Calendar.WEEK_OF_YEAR;
    	} else { //MONTH
    		periodField = Calendar.MONTH;
    	}
    	
    	int minutes = getMinutes(periodField, updates, 1);
    	
    	Calendar calendarNextPeriod = new GregorianCalendar();
    	calendarNextPeriod.setTime(calendarStartPeriod.getTime());
    	
    	TimeCycle cycle = new TimeCycle(calendarStartPeriod, calendarNextPeriod, minutes, includeWeekends);
    	
    	while(!cycle.isWithin(calendarNow.getTime())) {
    		cycle = cycle.getNext();
    	}

        return cycle;
    }

    private TimeCycle(final Calendar startDate, final Calendar endDate, final int minutes, final boolean includeWeekends) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.minutes = minutes;
        this.includeWeekends = includeWeekends;
    }

    Date getEndDate() {
        return endDate.getTime();
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

    boolean isAfter(final Date time) {
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
    
    private static boolean inWeekend(final Calendar date) {
    	final int dayInWeek = date.get(Calendar.DAY_OF_WEEK);
    	if(dayInWeek == Calendar.SATURDAY || dayInWeek == Calendar.SUNDAY) {
    		return true;
    	}
    	return false;
    }

    public TimeCycle getNext() {
    	Calendar newStartDate = new GregorianCalendar();
    	newStartDate.setTime(endDate.getTime());
    	
    	Calendar newEndDate = new GregorianCalendar();
    	newEndDate.setTime(endDate.getTime());
    	
    	do {
    		newEndDate.add(Calendar.MINUTE, minutes);
    	} while(inWeekend(newEndDate));
        return new TimeCycle(newStartDate, newEndDate, minutes, includeWeekends);
    }

	public TimeCycle getPrevious() {
    	Calendar newStartDate = new GregorianCalendar();
    	newStartDate.setTime(startDate.getTime());
    	
    	Calendar newEndDate = new GregorianCalendar();
    	newEndDate.setTime(startDate.getTime());
    	do {
    		newStartDate.add(Calendar.MINUTE, -minutes);
    	} while(inWeekend(newStartDate));
        return new TimeCycle(newStartDate, newEndDate, minutes, includeWeekends);
	}
}