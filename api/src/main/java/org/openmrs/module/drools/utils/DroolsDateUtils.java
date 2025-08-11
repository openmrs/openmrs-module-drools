package org.openmrs.module.drools.utils;

import org.apache.commons.lang3.time.DateUtils;
import org.openmrs.api.context.Context;

import java.text.ParseException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class DroolsDateUtils {

    /**
     * Parses a date string using standard 'yyyy-MM-dd hh:mm:ss' format
     */
    public static Date parseDate(String dateString) throws ParseException {
        return DateUtils.parseDate(dateString,"yyyy-MM-dd hh:mm:ss", "yyyy-MM-dd");
    }

    /**
     * Parses a date string using the application's locale-sensitive date format
     */
    public static Date parseLocaleDate(String dateString) throws ParseException {
        return Context.getDateFormat().parse(dateString);
    }

    /**
     * Converts a java.util.Date to java.time.LocalTime,
     * ignoring the date part.
     *
     * @param date the Date to convert
     * @return the LocalTime extracted from the Date
     * @throws IllegalArgumentException if date is null
     */
    public static LocalTime toLocalTime(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null");
        }

        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
    }

    public static LocalTime parseTime(String timeStr) {
        return LocalTime.parse(timeStr); // uses default ISO_LOCAL_TIME
    }

    /**
     * Returns the current date and time as a {@link java.util.Date}.
     *
     * @return the current date and time
     */
    public static Date now() {
        return new Date();
    }

    /**
     * Returns a date N days ago from today (00:00:00).
     *
     * @param days the number of days to subtract
     */
    public static Date daysAgo(int days) {
        return shift(Calendar.DAY_OF_YEAR, -days);
    }

    /**
     * Returns a date N weeks ago from today (00:00:00).
     *
     * @param weeks the number of days to subtract
     */
    public static Date weeksAgo(int weeks) {
        return shift(Calendar.WEEK_OF_YEAR, -weeks);
    }

    /**
     * Returns a date N months ago from today (00:00:00).
     *
     * @param months the number of days to subtract
     */
    public static Date monthsAgo(int months) {
        return shift(Calendar.MONTH, -months);
    }

    /**
     * Returns a date N years ago from today (00:00:00).
     *
     * @param years the number of days to subtract
     */
    public static Date yearsAgo(int years) {
        return shift(Calendar.YEAR, -years);
    }

    /**
     * Returns a date N days from today in the future (00:00:00).
     *
     * @param days the number of days to subtract
     */
    public static Date daysFromNow(int days) {
        return shift(Calendar.DAY_OF_YEAR, days);
    }

    /**
     * Shifts the current date by the specified amount of time
     *
     * @param calendarField the field to adjust
     * @param amount the amount to shift
     */
    private static Date shift(int calendarField, int amount) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(calendarField, amount);
        return cal.getTime();
    }
}
