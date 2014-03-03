package com.ma.dc;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeCycle {
	
	private final static int MINUTES_IN_HOUR = 60;
	private final static int HOURS_IN_DAY = 24;
	private final static int DAYS_IN_FULL_WEEK = 7;
	
	public final static String HOUR = "hour";
	public final static String DAY = "day";
	public final static String WEEK = "week";
	public final static String MONTH = "month";
	
	static TimeCycle createCurrentFromCheckpoint(final int updates,
			final String timePeriod, final int startTime, final int startDay,
			final boolean includeWeekends) {
		return createCycleFromTime(updates, timePeriod, startTime, startDay,
				includeWeekends, System.currentTimeMillis());
	}

	static TimeCycle createCycleFromTime(final int updates,
			final String timePeriod, final int startTime, final int startDay,
			final boolean includeWeekends, final long time) {

		Calendar calendarNow = new GregorianCalendar();
		calendarNow.setTimeInMillis(time);

		int currentMinute = calendarNow.get(Calendar.MINUTE);
		int currentHour = calendarNow.get(Calendar.HOUR_OF_DAY);

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

		if (timePeriod.compareToIgnoreCase(TimeCycle.HOUR) == 0) {
			int periodInMinutes = MINUTES_IN_HOUR / updates;

			while (!calendarStartTime.before(calendarNow)
					|| (!includeWeekends && inWeekend(calendarStartTime))) {
				calendarStartTime.add(Calendar.MINUTE, -periodInMinutes);
				calendarEndTime.add(Calendar.MINUTE, -periodInMinutes);
			}

			while (currentMinute > (calendarStartTime.get(Calendar.MINUTE) + periodInMinutes)) {
				calendarStartTime.add(Calendar.MINUTE, periodInMinutes);
				calendarEndTime.add(Calendar.MINUTE, periodInMinutes);
			}

			while (!includeWeekends && inWeekend(calendarStartTime)) {
				calendarStartTime.add(Calendar.MINUTE, periodInMinutes);
				calendarEndTime.add(Calendar.MINUTE, periodInMinutes);
			}

			calendarEndTime.add(Calendar.MINUTE, periodInMinutes);

			while (!includeWeekends && inWeekend(calendarEndTime)) {
				calendarEndTime.add(Calendar.MINUTE, periodInMinutes);
			}
		} else if (timePeriod.compareToIgnoreCase(TimeCycle.DAY) == 0) {
			calendarStartTime.set(Calendar.HOUR_OF_DAY, startTime);
			calendarEndTime.set(Calendar.HOUR_OF_DAY, startTime);

			int periodInMinutes = (MINUTES_IN_HOUR * HOURS_IN_DAY) / updates;

			while (!calendarStartTime.before(calendarNow)
					|| (!includeWeekends && inWeekend(calendarStartTime))) {
				calendarStartTime.add(Calendar.MINUTE, -periodInMinutes);
				calendarEndTime.add(Calendar.MINUTE, -periodInMinutes);
			}

			while ((currentHour * MINUTES_IN_HOUR + currentMinute) > (calendarStartTime
					.get(Calendar.HOUR_OF_DAY)
					* MINUTES_IN_HOUR
					+ calendarStartTime.get(Calendar.MINUTE) + periodInMinutes)) {
				calendarStartTime.add(Calendar.MINUTE, periodInMinutes);
				calendarEndTime.add(Calendar.MINUTE, periodInMinutes);
			}

			while (!includeWeekends && inWeekend(calendarStartTime)) {
				calendarStartTime.add(Calendar.MINUTE, periodInMinutes);
				calendarEndTime.add(Calendar.MINUTE, periodInMinutes);
			}

			calendarEndTime.add(Calendar.MINUTE, periodInMinutes);

			while (!includeWeekends && inWeekend(calendarEndTime)) {
				calendarEndTime.add(Calendar.MINUTE, periodInMinutes);
			}

		} else if (timePeriod.compareToIgnoreCase(TimeCycle.WEEK) == 0) {
			calendarStartTime.set(Calendar.HOUR_OF_DAY, startTime);
			calendarEndTime.set(Calendar.HOUR_OF_DAY, startTime);

			calendarStartTime.set(Calendar.DAY_OF_WEEK, startDay);
			calendarEndTime.set(Calendar.DAY_OF_WEEK, startDay);
			if (!calendarStartTime.before(calendarNow)) {
				calendarStartTime.add(Calendar.WEEK_OF_YEAR, -1);
				calendarEndTime.add(Calendar.WEEK_OF_YEAR, -1);
			}

			int periodInHours = (DAYS_IN_FULL_WEEK * HOURS_IN_DAY) / updates;

			while (calendarNow.getTimeInMillis() > (calendarStartTime
					.getTimeInMillis() + ((long) periodInHours * 3600000))) {
				calendarStartTime.add(Calendar.HOUR_OF_DAY, periodInHours);
				calendarEndTime.add(Calendar.HOUR_OF_DAY, periodInHours);
			}
			calendarEndTime.add(Calendar.HOUR_OF_DAY, periodInHours);

			while (!includeWeekends && inWeekend(calendarStartTime)) {
				calendarStartTime.add(Calendar.DAY_OF_YEAR, -1);
			}

			while (!includeWeekends && inWeekend(calendarEndTime)) {
				calendarEndTime.add(Calendar.DAY_OF_YEAR, 1);
			}
		} else { // MONTH
			calendarStartTime.set(Calendar.HOUR_OF_DAY, startTime);
			calendarEndTime.set(Calendar.HOUR_OF_DAY, startTime);

			calendarStartTime.set(Calendar.DAY_OF_WEEK, startDay);
			calendarEndTime.set(Calendar.DAY_OF_WEEK, startDay);

			if (!calendarStartTime.before(calendarNow)) {
				calendarStartTime.add(Calendar.DAY_OF_YEAR, -7);
				calendarEndTime.add(Calendar.DAY_OF_YEAR, -7);
			}

			int daysInMonth = calendarNow
					.getActualMaximum(Calendar.DAY_OF_MONTH);

			int periodInHours = (daysInMonth * HOURS_IN_DAY) / updates;

			while (calendarNow.getTimeInMillis() > (calendarStartTime
					.getTimeInMillis() + ((long) periodInHours * 3600000))) {
				calendarStartTime.add(Calendar.HOUR_OF_DAY, periodInHours);
				calendarEndTime.add(Calendar.HOUR_OF_DAY, periodInHours);
			}

			calendarEndTime.add(Calendar.HOUR_OF_DAY, periodInHours);

			while (!includeWeekends && inWeekend(calendarStartTime)) {
				calendarStartTime.add(Calendar.DAY_OF_YEAR, -1);
			}

			while (!includeWeekends && inWeekend(calendarEndTime)) {
				calendarEndTime.add(Calendar.DAY_OF_YEAR, 1);
			}
		}

		return new TimeCycle(calendarStartTime, calendarEndTime, updates,
				timePeriod, startTime, startDay, includeWeekends);
	}

	private static boolean inWeekend(final Calendar date) {
		final int dayInWeek = date.get(Calendar.DAY_OF_WEEK);
		if (dayInWeek == Calendar.SATURDAY || dayInWeek == Calendar.SUNDAY) {
			return true;
		}
		return false;
	}

	private final Calendar startDate;
	private final Calendar endDate;

	private final int updates;
	private final String timePeriod;
	private final int startDay;
	private final int startTime;
	private final boolean includeWeekends;

	private TimeCycle(final Calendar startDate, final Calendar endDate,
			final int updates, final String timePeriod, final int startTime,
			final int startDay, final boolean includeWeekends) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.updates = updates;
		this.timePeriod = timePeriod;
		this.startTime = startTime;
		this.startDay = startDay;
		this.includeWeekends = includeWeekends;
	}

	long getEndDate() {
		return endDate.getTimeInMillis();
	}

	TimeCycle getNext() {
		return transferToTime(endDate.getTimeInMillis() + 1000);
	}

	TimeCycle getPrevious() {
		return transferToTime(startDate.getTimeInMillis() - 1000);
	}

	int getProgressTowardsEndTimeInPercent(final long time) {
		if (isBefore(time)) {
			return 0;
		}
		if (isAfter(time)) {
			return 100;
		}
		final double transformedTime = time - startDate.getTimeInMillis();
		final double transformedEndTime = endDate.getTimeInMillis()
				- startDate.getTimeInMillis();

		return Double.valueOf(transformedTime * 100 / transformedEndTime)
				.intValue();
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
		return TimeCycle.createCycleFromTime(updates, timePeriod, startTime,
				startDay, includeWeekends, anotherTime);
	}
}