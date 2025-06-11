package org.openmrs.module.drools.utils;

import org.apache.commons.lang3.time.DateUtils;
import org.openmrs.api.context.Context;

import java.text.ParseException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class DroolsDateUtils {

    /**
     * Returns the current date and time as a {@link java.util.Date}.
     *
     * @return the current date and time
     */
    public static Date now() {
        return new Date();
    }

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
}
