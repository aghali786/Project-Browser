package browser.projektb.com.rikoshae.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by User on 9/29/2016.
 */
public class Util {
    public static String getCurrentDate()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String date = sdf.format(new Date());
        return date;
    }
    public static long getDateCurrentMillis() {
        // 2013-10-24T01:00:00Z

        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
                Locale.getDefault());
        sfd.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = sfd.format(new Date());

        try {
            Date d = sfd.parse(date);
            return d.getTime();


        } catch (Exception e) {
            System.out.println("");
        }

        return 0;

    }
    public static long getTimeStampBefore7Days()
    {
        long date = 0;
        try {
            date = getDateCurrentMillis() - 604800000L;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return date;
    }

   /* public static long getCurrentTimeStamp()
    {
        long date = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date myDate = sdf.parse(getCurrentDate());
            date = myDate.getTime();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return date;
    }
*/
   /* public static String getCurrentTime()
    {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm a");
// you can get seconds by adding  "...:ss" to it
        //date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));

        String localTime = date.format(currentLocalTime);
        return localTime;
    }*/

    public static String oneDayDifferenceDate(String dateString2) {
        String days = "";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat fullFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");//"EEEE, MMMM dd, 'yyyy");
            Date date1 = formatter.parse(dateString2);
            Date date2 = formatter.parse(getCurrentDate());
            long different = date2.getTime() - date1.getTime();

            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;
            long elapsedDays = different / daysInMilli;
            //System.out.println(daysInMilli);

            String exactDate = fullFormat.format(date1);
            if(elapsedDays == 0)
            {
                days = "Today - "+ exactDate;
            }else if(elapsedDays ==1)
            {
                days = "Yesterday - "+ exactDate;
            }else
            {
                days = exactDate;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return days;

    }
    public static String getDate(long timeStamp){

        try{
            DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "";
        }
    }

    public static String getTime(long timeStamp){

        try{
            DateFormat sdf = new SimpleDateFormat("HH:mm a");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate.getTime());
        }
        catch(Exception ex){
            return "";
        }
    }
}
