package pl.agh.depressiondetector.analytics;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;

import pl.agh.depressiondetector.analytics.mood.MoodBroadcastReceiver;
import pl.agh.depressiondetector.analytics.phonecalls.PhoneCallService;
import pl.agh.depressiondetector.analytics.smses.TextMessageService;
import pl.agh.depressiondetector.utils.ToastUtils;

import static android.content.Context.ALARM_SERVICE;
import static pl.agh.depressiondetector.analytics.mood.MoodBroadcastReceiver.MOOD_REQUEST_CODE;

public final class AnalyticsAdapter {

    public static void startAnalytics(Context context) {
        context = context.getApplicationContext();
        startAnalytics(context, AnalysedDataType.values());
    }

    public static void startAnalytics(Context context, AnalysedDataType... types) {
        context = context.getApplicationContext();
        for (AnalysedDataType type : types) {
            switch (type) {
                case MOOD:
                    startMoodAlarm(context);
                    break;
                case PHONE_CALL:
                    startService(context, PhoneCallService.class);
                    break;
                case SMS:
                    startService(context, TextMessageService.class);
                    break;
            }
        }
    }

    public static void stopAnalytics(Context context, AnalysedDataType... types) {
        context = context.getApplicationContext();
        for (AnalysedDataType type : types) {
            switch (type) {
                case MOOD:
                    stopMoodAlarm(context);
                    break;
                case PHONE_CALL:
                    stopService(context, PhoneCallService.class);
                    break;
                case SMS:
                    stopService(context, TextMessageService.class);
                    break;
            }
        }
    }

    private static void startMoodAlarm(Context context) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Intent intent = new Intent(context, MoodBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MOOD_REQUEST_CODE, intent, 0);

        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (manager != null)
            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), DateUtils.DAY_IN_MILLIS, pendingIntent);
        else
            ToastUtils.show(context, "Could not start mood alarm");
    }

    private static void stopMoodAlarm(Context context) {
        Intent intent = new Intent(context, MoodBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MOOD_REQUEST_CODE, intent, 0);

        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (manager != null)
            manager.cancel(pendingIntent);
        else
            ToastUtils.show(context, "Could not stop mood alarm");
    }

    private static void startService(Context context, Class<? extends Service> service) {
        Intent intent = new Intent(context, service);
        context.startService(intent);
    }

    private static void stopService(Context context, Class<? extends Service> service) {
        Intent intent = new Intent(context, service);
        context.stopService(intent);
    }
}