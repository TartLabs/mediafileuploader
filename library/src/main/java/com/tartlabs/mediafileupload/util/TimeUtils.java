package com.tartlabs.mediafileupload.util;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
    static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final int HOUR = 60 * MINUTE;
    public static final int DAY = 24 * HOUR;

    private static SimpleDateFormat[] ACCEPTED_TIMESTAMP_FORMATS = {
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.getDefault()),
            new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    };
    private static final SimpleDateFormat VALID_IFMODIFIEDSINCE_FORMAT =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.getDefault());
    private static Date d;

    public static Date parseTimestamp(String timestamp) {
        //it may be an integer - milliseconds
        try {
            if (timestamp.matches("[0-9]+")) {
                long longDate = Long.parseLong(timestamp);
                return new Date(longDate);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        for (SimpleDateFormat format : ACCEPTED_TIMESTAMP_FORMATS) {
            format.setTimeZone(TimeZone.getDefault());
            try {
                return format.parse(timestamp);
            } catch (ParseException ex) {
                continue;
            }
        }
        // didnt match any format
        return null;
    }

    public static boolean isSameDay(long time1, long time2, Context context) {
        TimeZone displayTimeZone = TimeUtils.getTimeZone();
        Calendar cal1 = Calendar.getInstance(displayTimeZone);
        Calendar cal2 = Calendar.getInstance(displayTimeZone);
        cal1.setTimeInMillis(time1);
        cal2.setTimeInMillis(time2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null)
            throw new IllegalArgumentException("Calender object cannot be null");
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isValidFormatForIfModifiedSinceHeader(String timestamp) {
        try {
            return VALID_IFMODIFIEDSINCE_FORMAT.parse(timestamp) != null;
        } catch (Exception ex) {
            return false;
        }
    }

    public static long timestampToMillis(String timestamp, long defaultValue) {
        if (TextUtils.isEmpty(timestamp)) {
            return defaultValue;
        }
        Date d = parseTimestamp(timestamp);
        return d == null ? defaultValue : d.getTime();
    }

    /**
     * Format a {@code date} honoring the app preference for using device timezone.
     * {@code Context} is used to lookup the shared preference settings.
     */
    public static String formatShortDate(Context context, Date date) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        return DateUtils.formatDateRange(context, formatter, date.getTime(), date.getTime(),
                DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_NO_YEAR,
                getDisplayTimeZone(context).getID()).toString();
    }

    public static String formatShortTime(Context context, Date time) {
        // Android DateFormatter will honor the user's current settings.
        DateFormat format = android.text.format.DateFormat.getTimeFormat(context);
        // Override with Timezone based on settings since users can override their phone's timezone
        // with Pacific time zones.
        TimeZone tz = getDisplayTimeZone(context);
        if (tz != null) {
            format.setTimeZone(tz);
        }
        return format.format(time);
    }

    public static TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    public static long getCurrentTimeInMs() {
        return System.currentTimeMillis();
    }

    /**
     * Returns "Today", "Tomorrow", "Yesterday", or a short date format.
     */
    public static String formatHumanFriendlyShortDate(final Context context, long timestamp) {
        long localTimestamp, localTime;
        long now = TimeUtils.getCurrentTimeInMs();

        TimeZone tz = getDisplayTimeZone(context);
        localTimestamp = timestamp + tz.getOffset(timestamp);
        localTime = now + tz.getOffset(now);

        long dayOrd = localTimestamp / 86400000L;
        long nowOrd = localTime / 86400000L;

        if (dayOrd == nowOrd) {
            //return context.getString(R.string.day_title_today);
            return new SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(new Date(timestamp))
                    .replace("am", "AM")
                    .replace("pm", "PM");
        } else if (dayOrd == nowOrd - 1) {
            return "Yesterday";
        } else if (dayOrd == nowOrd + 1) {
            return "Tomorrow";
        } else {
            return formatShortDate(context, new Date(timestamp));
        }
    }

    public static TimeZone getDisplayTimeZone(Context context) {
        return TimeZone.getDefault();
    }

    public static long diff(Date date1, Date date2) {
        return date1.getTime() - date2.getTime();
    }

    public static long diffInSeconds(Date date1, Date date2) {
        return TimeUnit.MILLISECONDS.toSeconds(date1.getTime() - date2.getTime());
    }

    public static long diffInMinutes(Date date1, Date date2) {
        return TimeUnit.MILLISECONDS.toMinutes(date1.getTime() - date2.getTime());
    }

    public static long diffInHours(Date date1, Date date2) {
        return TimeUnit.MILLISECONDS.toHours(date1.getTime() - date2.getTime());
    }

    public static long diffInDays(Date date1, Date date2) {
        return TimeUnit.MILLISECONDS.toDays(date1.getTime() - date2.getTime());
    }

    public static Date getCurrentTime() {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss", Locale.getDefault());

        //Local time zone
        SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss", Locale.getDefault());

        //Time in GMT
        try {
            return dateFormatLocal.parse(dateFormatGmt.format(new Date()));
        } catch (ParseException e) {
            return null;
        }
    }

    public static boolean isToday(Date date) {
        if (date == null)
            throw new IllegalArgumentException("Date object cannot be null");
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);

        Calendar cal2 = Calendar.getInstance();

        return isSameDay(cal1, cal2);
    }

    public static boolean isYesterday(Date date) {
        if (date == null)
            throw new IllegalArgumentException("Date object cannot be null");
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);

        Calendar cal2 = Calendar.getInstance();
        if ((cal2.get(Calendar.DATE) - cal1.get(Calendar.DATE)) == 1) {
            return true;
        } else {
            return false;
        }
    }

    // Convert from yyyy-MM-dd to dd MMM,yyyy
    public static String convertDateString(String string) {
        DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DateFormat toFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        Date date = null;
        try {
            date = fromFormat.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return toFormat.format(date);
    }

    public static String convertDateStringWithTime(String string) {
        DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        DateFormat toFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault());
        String convertedTime = null;
        try {
            convertedTime = toFormat.format(fromFormat.parse(string));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedTime != null ? convertedTime
                .replace("a.m.", "AM")
                .replace("p.m.", "PM") : null;
    }


    public static String convertDateStringWithDateOnly(String string) {
        DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        DateFormat toFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String convertedTime = null;
        try {
            convertedTime = toFormat.format(fromFormat.parse(string));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedTime != null ? convertedTime
                .replace("a.m.", "AM")
                .replace("p.m.", "PM") : null;
    }

    public static String convertDateStringWithTimeOnly(String string) {
        DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        DateFormat toFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String convertedTime = null;
        try {
            convertedTime = toFormat.format(fromFormat.parse(string));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedTime != null ? convertedTime
                .replace("a.m.", "AM")
                .replace("p.m.", "PM") : null;
    }


    public static String convertDateStringWithLocalTime(String string) {
        DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        DateFormat toFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault());
        String convertedTime = null;
        try {
            convertedTime = toFormat.format(fromFormat.parse(string));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedTime != null ? convertedTime
                .replace("a.m.", "AM")
                .replace("p.m.", "PM") : null;
    }

    public static String convertDateStringDate(String string) {
        DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        DateFormat toFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        Date date = null;
        try {
            date = fromFormat.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return toFormat.format(date);
    }

    public static String convertDateStringTime(String string) {
        DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        DateFormat toFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        Date date = null;
        try {
            date = fromFormat.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return toFormat.format(date);
    }


    public static long convertDateFormatStringDate(String string) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = sdf.parse(string);
        return date.getTime();
    }


    public static String getCurrentTimeZone() {
        return Calendar.getInstance().getTimeZone().getID();
    }

    public static String getTimeZoneString(String dateTimeString) {
        DateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat("hh mm a", Locale.getDefault());

        String convertedDate = null;
        try {
            convertedDate = outputFormat.format(inputFormat.parse(dateTimeString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedDate;
    }

    public static String getHourOnlyTimeString(String timeString) {
        DateFormat inputFormat = new SimpleDateFormat("HH a", Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat("KK:mm a", Locale.getDefault());

        String convertedTime = null;
        try {
            convertedTime = outputFormat.format(inputFormat.parse(timeString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedTime != null ? convertedTime
                .replace("a.m.", "am")
                .replace("p.m.", "pm") : null;
    }

    public static String getTimeString(String timeString) {
        DateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat("KK:mm a", Locale.getDefault());

        String convertedTime = null;
        try {
            convertedTime = outputFormat.format(inputFormat.parse(timeString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedTime != null ? convertedTime
                .replace("a.m.", "am")
                .replace("p.m.", "pm") : null;
    }

    public static String getTimeStringInGMTZone(String dateTimeString) {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        inputFormat.setTimeZone(TimeZone.getTimeZone(("GMT")));
        DateFormat outputFormat = new SimpleDateFormat("KK:mm a", Locale.getDefault());

        String convertedTime = null;
        try {
            convertedTime = outputFormat.format(inputFormat.parse(dateTimeString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedTime != null ? convertedTime
                .replace("am", "AM")
                .replace("pm", "PM") : null;
    }

    public static String getDateTimeString(String dateTimeString) {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy KK:mm a", Locale.getDefault());

        String convertedTime = null;
        try {
            convertedTime = outputFormat.format(inputFormat.parse(dateTimeString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedTime != null ? convertedTime
                .replace("am", "AM")
                .replace("pm", "PM") : null;
    }

    public static String getDateTimeStringTimeOnly(String dateTimeString) {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        String convertedTime = null;
        try {
            convertedTime = outputFormat.format(inputFormat.parse(dateTimeString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedTime != null ? convertedTime
                .replace("am", "AM")
                .replace("pm", "PM") : null;
    }

    public static String geTimeStringTimeOnly(String dateTimeString) {
        DateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        String convertedTime = null;
        try {
            convertedTime = outputFormat.format(inputFormat.parse(dateTimeString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedTime != null ? convertedTime
                .replace("am", "AM")
                .replace("pm", "PM") : null;
    }

    public static String getDateTimeStringDateOnly(String dateTimeString) {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        String convertedTime = null;
        try {
            convertedTime = outputFormat.format(inputFormat.parse(dateTimeString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedTime != null ? convertedTime
                .replace("am", "AM")
                .replace("pm", "PM") : null;
    }


    public static String getStringDateWithDividerOnly(String dateTimeString) {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        String convertedTime = null;
        try {
            convertedTime = outputFormat.format(inputFormat.parse(dateTimeString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedTime != null ? convertedTime
                .replace("am", "AM")
                .replace("pm", "PM") : null;
    }

    public static String getStringDateOnly(String dateTimeString) {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        String convertedTime = null;
        try {
            convertedTime = outputFormat.format(inputFormat.parse(dateTimeString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedTime != null ? convertedTime
                .replace("am", "AM")
                .replace("pm", "PM") : null;
    }

    public static String getStringTimeOnly(String dateTimeString) {
        DateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat("KK:mm a", Locale.getDefault());

        String convertedTime = null;
        try {
            convertedTime = outputFormat.format(inputFormat.parse(dateTimeString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedTime != null ? convertedTime
                .replace("am", "AM")
                .replace("pm", "PM") : null;
    }

    public static String getDateTimeString1(String dateTimeString) {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy KK:mm a", Locale.getDefault());

        String convertedTime = null;
        try {
            convertedTime = outputFormat.format(inputFormat.parse(dateTimeString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedTime != null ? convertedTime
                .replace("am", "AM")
                .replace("pm", "PM") : null;
    }

    public static String getCurrentDateString(Date date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(date);
    }

    public static String getCurrentDateString() {
        Calendar c = Calendar.getInstance();
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return format.format(c.getTime());
    }

    public static String getStringFromDate(Date date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return format.format(date);
    }

    public static String getStringFromDateString(String date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return format.format(date);
    }

    public static String getMonthAndDateFromDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d = null;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf.applyPattern("MMM dd");
        return sdf.format(d);
    }

    public static String hhmmaTimeZone(String timeValue) {
        String pattern = "hh:mm a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String convertedTime = simpleDateFormat.format(timeValue);
        return convertedTime;
    }

    public static Date getDateFromString(String date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Date d = null;

        try {
            d = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static Date getDateFromStringWithTimeZone(String date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        format.setTimeZone(utcZone);
        Date d = null;

        try {
            d = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static Date getDateFromDateStringOnly(String date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Date d = null;

        try {
            d = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    // Get time with local time zone
    public static String getLocalTimeZoneTime(String dateString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        simpleDateFormat.setTimeZone(utcZone);
        Date myDate = null;
        try {
            myDate = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(myDate);
        DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy KK:mm a", Locale.getDefault());
        outputFormat.setTimeZone(TimeZone.getDefault());
        String convertedTime = outputFormat.format(myDate);
        return convertedTime != null ? convertedTime
                .replace("am", "AM")
                .replace("pm", "PM") : null;
    }

    // Get time with local time zone
    public static String getDateWithTimeZone(String dateString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date myDate = null;
        try {
            myDate = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(myDate);
        DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy KK:mm a", Locale.getDefault());
        outputFormat.setTimeZone(TimeZone.getDefault());
        String convertedTime = outputFormat.format(myDate);
        return convertedTime != null ? convertedTime
                .replace("am", "AM")
                .replace("pm", "PM") : null;
    }

    // Calculate dates between two dates
    public static List<Date> getDates(String dateString1, String dateString2) {
        ArrayList<Date> dates = new ArrayList<Date>();
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date1 = null;
        Date date2 = null;

        try {
            date1 = df1.parse(dateString1);
            date2 = df1.parse(dateString2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        while (!cal1.after(cal2)) {
            dates.add(cal1.getTime());
            cal1.add(Calendar.DATE, 1);
        }
        return dates;
    }

    public static String getStringFromDate2(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault());
        return sdf.format(date);
    }

    public static String getDayStringFromDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
        return sdf.format(date);
    }

    public static String getDateStringFromDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
        return sdf.format(date);
    }

    public static String getTimeStringFromDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("KK:mm aa", Locale.getDefault());
        String convertedTime = sdf.format(date);
        return convertedTime != null ? convertedTime
                .replace("a.m.", "am")
                .replace("p.m.", "pm") : null;
    }

    // Date format like 21 oct 2018
    public static String getDateFormatFromDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    // Date format like 21 oct 2018
    public static String getDateFormatFromString(String dateString) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return sdf.format(date);
    }

    public static String getDateFormatFromStringWithMonthName(String dateString) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return simpleDateFormat.format(date);
    }

    // Date format like 12 pm
    public static String getTimeFormatFromString(String dateString) {

        SimpleDateFormat sdf = new SimpleDateFormat("hh aa", Locale.getDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String convertedTime = sdf.format(date);
        return convertedTime != null ? convertedTime
                .replace("a.m.", "am")
                .replace("p.m.", "pm") : null;
    }

    public static String getDay(String date_in_string_format) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        Date date = null;
        try {
            date = sdf.parse(date_in_string_format);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return simpleDateFormat.format(date);
    }

    public static String getDayHumanReadable(Date date) {
        String day;
        if (TimeUtils.isToday(date)) {
            day = "Today";
        } else if (TimeUtils.isYesterday(date)) {
            day = "Yesterday";
        } else {
            day = getStringFromDate2(date);
        }
        return day;
    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(String dateString, String timestampFormat) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timestampFormat, Locale.getDefault());
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        simpleDateFormat.setTimeZone(utcZone);

        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);


        long time = calendar.getTimeInMillis();

        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = getCurrentTime().getTime();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    public static CharSequence getAgo(String dateString, String timestampFormat) {
        String result = null;
        SimpleDateFormat sdf = new SimpleDateFormat(timestampFormat, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        long time = 0;
        try {
            time = sdf.parse(dateString).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long now = System.currentTimeMillis();
        if (DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS).equals("0 minutes ago")) {
            result = "just now";
        } else {
            result = (String) DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
        }
        return result;

    }
}
