package pl.agh.depressiondetector.analytics.mood;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import pl.agh.depressiondetector.R;
import static pl.agh.depressiondetector.analytics.mood.MoodActivity.EXTRA_TIME;


public class MoodBroadcastReceiver extends BroadcastReceiver {

    public static final int MOOD_REQUEST_CODE = 8009;

    @Override
    public void onReceive(Context context, Intent intent) {
        showNotification(context);
    }

    public static void showNotification(Context context) {
        long time = System.currentTimeMillis();
        Intent intent = new Intent(context, MoodActivity.class);
        intent.putExtra(EXTRA_TIME, time);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, MOOD_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_leaves);
        builder.setContentTitle(context.getString(R.string.mood_notification_title));
        builder.setContentText(context.getString(R.string.mood_notification_text));
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) time, builder.build());
    }
}
