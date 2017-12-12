package pl.agh.depressiondetector.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final String CLIENT_DATE_FORMAT = "dd-MM-yyyy";
    private static final String CLIENT_FRIENDLY_DATE_FORMAT = "dd MMMM yyyy";
    private static final String SERVER_DATE_FORMAT = "yyyy-MM-dd";
    private static final String SERVER_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String convertToServerDateTimeFormat(long timestamp) {
        return convertToServerDateTimeFormat(new Date(timestamp));
    }

    public static String convertToServerDateTimeFormat(Date date) {
        return new SimpleDateFormat(SERVER_DATE_TIME_FORMAT, Locale.getDefault()).format(date);
    }

    public static String convertToServerDateFormat(Date date) {
        return new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.getDefault()).format(date);
    }

    public static String convertToClientDateFormat(long timestamp) {
        return convertToClientDateFormat(new Date(timestamp));
    }

    public static String convertToClientDateFormat(Date date) {
        return new SimpleDateFormat(CLIENT_DATE_FORMAT, Locale.getDefault()).format(date);
    }

    public static String convertToHumanFriendlyFormat(Date date) {
        return new SimpleDateFormat(CLIENT_FRIENDLY_DATE_FORMAT, Locale.getDefault()).format(date);
    }

    public static Date getDateFromClientDateFormat(String date) {
        try {
            return new SimpleDateFormat(CLIENT_DATE_FORMAT, Locale.getDefault()).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
