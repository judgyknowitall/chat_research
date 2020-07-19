package ivy.learn.chat.utility;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.recyclerview.widget.SortedList;

import com.google.android.gms.tasks.OnCompleteListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ivy.learn.chat.adapters.LobbyAdapter;

/**
 * For Formatting see: https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
 */
public class Util {

    public static final int CHATROOM_REQUEST = 1;
    public static final int NEWCHATROOM_REQUEST = 2;

    public static final String DAY_FORMAT = "hh:mm a";              // eg. 3:00 AM
    public static final String WEEK_FORMAT = "EEE " + DAY_FORMAT;   // eg. Monday 3:00 AM
    public static final String MONTH_FORMAT = "MMMM d";             // eg. July 1
    public static final String YEAR_FORMAT = "MMMM, yyyy";          // eg. July 2020


/* Calendar & Millis
***************************************************************************************************/

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


/* Lists
***************************************************************************************************/

    // Remove an item from position [init] in ArrayList of generic type <T> and insert it in [dest]
    public static <T> void reorderItem(List<T> list, int init, int dest){
        if (dest < init) {
            list.add(dest, list.get(init));
            list.remove(init+1);
        } else if (dest > init) {
            list.add(dest+1, list.get(init));
            list.remove(init);
        }
    }

    // Replace an item in a list
    public static <T> void replaceItem(List<T> list, T newItem, int index){
        list.remove(index);
        list.add(index, newItem);
    }


/* Spannable String
***************************************************************************************************/

    // Create a spannable string for a menuItem
    public static void colorMenuItem(MenuItem menuItem, int color_id){
        SpannableString str = new SpannableString(menuItem.getTitle());
        str.setSpan(new ForegroundColorSpan(color_id), 0, str.length(), 0);
        menuItem.setTitle(str);
    }


/* Firebase
***************************************************************************************************/

    // Get a simple onComplete Listener
    public static <T> OnCompleteListener<T> getSimpleOnCompleteListener(Context context, String success_msg, String failure_msg){
        return task -> {
            if (task.isSuccessful()) Toast.makeText(context, success_msg, Toast.LENGTH_SHORT).show();
            else {
                Toast.makeText(context, failure_msg, Toast.LENGTH_SHORT).show();
                Log.e(context.toString(), failure_msg, task.getException());
            }
        };
    }
}
