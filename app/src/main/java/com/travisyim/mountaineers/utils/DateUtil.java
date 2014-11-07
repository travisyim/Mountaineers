package com.travisyim.mountaineers.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DateUtil {
    // List of all date formats that we need to parse for both activity and registration dates
    private static final List<SimpleDateFormat> dateFormatsActivity =
            new ArrayList<SimpleDateFormat>() {{
        add(new SimpleDateFormat("EEE, MMM  d, yyyy"));
        add(new SimpleDateFormat("EEE, MMM dd, yyyy"));
        add(new SimpleDateFormat("EEE, MMMM  d, yyyy"));
        add(new SimpleDateFormat("EEE, MMMM dd, yyyy"));
        add(new SimpleDateFormat("EEE, MMMMM  d, yyyy"));
        add(new SimpleDateFormat("EEE, MMMMM dd, yyyy"));
        add(new SimpleDateFormat("EEE, MMMMMM  d, yyyy"));
        add(new SimpleDateFormat("EEE, MMMMMMM  d, yyyy"));
        add(new SimpleDateFormat("EEE, MMMMMMM dd, yyyy"));
        add(new SimpleDateFormat("EEE, MMMMMMMM  d, yyyy"));
        add(new SimpleDateFormat("EEE, MMMMMMMM dd, yyyy"));
        add(new SimpleDateFormat("EEE, MMMMMMMMM  d, yyyy"));
        add(new SimpleDateFormat("EEE, MMMMMMMMM dd, yyyy"));
    }};

    private static final List<SimpleDateFormat> dateFormatsRegistration =
            new ArrayList<SimpleDateFormat>() {{
        add(new SimpleDateFormat("MMM d, yyyy"));
        add(new SimpleDateFormat("MMM dd, yyyy"));
        add(new SimpleDateFormat("MMM  d, yyyy"));
    }};

    private static final List<SimpleDateFormat> dateFormatsButton =
            new ArrayList<SimpleDateFormat>() {{
                add(new SimpleDateFormat("EEE MM/dd/yyyy"));
                add(new SimpleDateFormat("MM/dd/yyyy"));
            }};

    public static final int TYPE_ACTIVITY_DATE = 0;
    public static final int TYPE_REGISTRATION_DATE = 1;
    public static final int TYPE_BUTTON_DATE = 2;
    public static final int TYPE_ACTIVITY_DATE_WITH_YEAR = 3;
    public static final int TYPE_ACTIVITY_DATE_NO_YEAR = 4;

    public static final Date convertToDate(final String input, final int dateType) {
        Date date = null;

        // Check for date type
        switch(dateType) {
            case TYPE_ACTIVITY_DATE:  // Activity date
                for (SimpleDateFormat format : dateFormatsActivity) {
                    try {
                        format.setLenient(false);
                        date = format.parse(input);
                        break;  // Exit on successful conversion
                    }
                    catch (ParseException e) { /* Intentionally left blank */ }
                }

                break;
            case TYPE_REGISTRATION_DATE:  // Registration date - no year is specified in String so it must be added
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                int year = calendar.get(Calendar.YEAR);

                for (SimpleDateFormat format : dateFormatsRegistration) {
                    try {
                        format.setLenient(false);
                        date = format.parse(input + ", " + year);
                        break;  // Exit on successful conversion
                    }
                    catch (ParseException e) { /* Intentionally left blank */ }
                }

                break;
            case TYPE_BUTTON_DATE:
                for (SimpleDateFormat format : dateFormatsButton) {
                    try {
                        format.setLenient(false);
                        date = format.parse(input);
                        break;  // Exit on successful conversion
                    }
                    catch (ParseException e) { /* Intentionally left blank */ }
                }

                break;
        }

        return date;
    }

    public static final String convertToString(final Date date, final int dateType) {
        SimpleDateFormat sdf = null;

        // Define the output date format
        switch (dateType){
            case TYPE_ACTIVITY_DATE_WITH_YEAR:
                sdf = new SimpleDateFormat("EEE, MMM dd, yyyy");
                break;
            case TYPE_ACTIVITY_DATE_NO_YEAR:
                sdf = new SimpleDateFormat("EEE, MMM dd");
                break;
            case TYPE_BUTTON_DATE:
                sdf = new SimpleDateFormat("MM/dd/yyyy");
                break;
        }

        // Return the date object in the requested string format
        return sdf.format(date);
    }

    /* Convert a local date object to UNC timezone.  This is used to modify dates before sending
     * them to parse (e.g. for use in a query) */
    public static final Date convertToUNC(final Date localDate) {
        Date date = null;
        TimeZone timeZone;
        int offsetInMillis;

        if (localDate != null) {  // Only work with valid dates
            timeZone = TimeZone.getDefault();  // Get local timezone
            // Gets offset between local time and UNC (a.k.a. GMT)
            offsetInMillis = timeZone.getOffset(localDate.getTime());
            // Add offset to the original time to get a UNC date object
            date = new Date(localDate.getTime() + offsetInMillis);
        }

        return date;
    }

    /* Convert a UNC date object to local timezone.  This is used to modify dates before using
     * them locally in the app (e.g. activity start and end dates) */
    public static final Date convertFromUNC(final Date parseDate) {
        Date date = null;
        TimeZone timeZone;
        int offsetInMillis;

        if (parseDate != null) {  // Only work with valid dates
            timeZone = TimeZone.getDefault();  // Get local timezone
            // Gets offset between local time and UNC (a.k.a. GMT)
            offsetInMillis = timeZone.getOffset(parseDate.getTime());
            // Subtract offset from the original time to get a local date object
            date = new Date(parseDate.getTime() - offsetInMillis);
        }

        return date;
    }
}