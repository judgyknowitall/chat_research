package ivy.learn.chat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * For Formatting see: https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
 */
public class Util {

    public static final String DAY_FORMAT = "hh:mm a";              // eg. 3:00 AM
    public static final String WEEK_FORMAT = "EEE " + DAY_FORMAT;   // eg. Monday 3:00 AM
    public static final String MONTH_FORMAT = "MMMM d";             // eg. July 1
    public static final String YEAR_FORMAT = "MMMM, yyyy";          // eg. July 2020

    // Translate time in millis to a readable date format
    public static String millisToDateTime(long millis){
        // Format string in locale timezone base on device settings
        DateFormat formatter = new SimpleDateFormat(getPattern(millis), Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);

        return formatter.format(cal.getTime());
    }

    // Get a specific pattern depending on current time
    private static String getPattern(long millis){
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);

        if (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)){ // Same year?
            if (cal.get(Calendar.WEEK_OF_MONTH) == now.get(Calendar.WEEK_OF_MONTH)){ // Same week?
                if (cal.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)){ // Same day?
                    return DAY_FORMAT;
                } else return WEEK_FORMAT;
            } else return MONTH_FORMAT;
        } else return YEAR_FORMAT;
    }
}
