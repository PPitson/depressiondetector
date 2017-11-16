package pl.agh.depressiondetector.analytics;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;

import pl.agh.depressiondetector.analytics.mood.MoodBroadcastReceiver;
import pl.agh.depressiondetector.analytics.phonecalls.PhoneCallService;
import pl.agh.depressiondetector.analytics.smses.TextMessageService;

import static android.content.Context.ALARM_SERVICE;
import static pl.agh.depressiondetector.analytics.mood.MoodBroadcastReceiver.MOOD_REQUEST_CODE;

public final class AnalyticsManager {

    public static void startAnalytics(Context context) {
        context = context.getApplicationContext();

        startMoodAlarm(context);
        startRecordingPhoneCalls(context);
        //startListeningForSmses(context);
    }

    public static void startMoodAlarm(Context context) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Intent intent = new Intent(context, MoodBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MOOD_REQUEST_CODE, intent, 0);

        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), DateUtils.DAY_IN_MILLIS, pendingIntent);
    }

    public static void stopMoodAlarm(Context context){
        Intent intent = new Intent(context, MoodBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MOOD_REQUEST_CODE, intent, 0);

        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

    public static void startRecordingPhoneCalls(Context context) {
        Intent phoneCall = new Intent(context, PhoneCallService.class);
        context.startService(phoneCall);
    }

    public static void stopRecordingPhoneCalls(Context context) {
        Intent phoneCall = new Intent(context, PhoneCallService.class);
        context.stopService(phoneCall);
    }

    // TODO Should not explode when there is no permission granted
    public static void startListeningForSmses(Context context) {
        Intent textMessage = new Intent(context, TextMessageService.class);
        context.startService(textMessage);
    }

    public static void stopListeningForSmses(Context context) {
        Intent textMessage = new Intent(context, TextMessageService.class);
        context.stopService(textMessage);
    }
}
