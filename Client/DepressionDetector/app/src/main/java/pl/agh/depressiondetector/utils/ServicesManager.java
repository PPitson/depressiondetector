package pl.agh.depressiondetector.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.recording.PhoneCallService;

public class ServicesManager {

    public static void startServices(Context context) {
        // Start recording phone calls
        Intent phoneCall = new Intent(context, PhoneCallService.class);
        context.startService(phoneCall);
    }

    public static void stopServices(Context context) {
        context.stopService(new Intent(context, PhoneCallService.class));
    }
}
