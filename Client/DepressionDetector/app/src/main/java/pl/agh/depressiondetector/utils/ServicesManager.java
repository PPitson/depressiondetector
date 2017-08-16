package pl.agh.depressiondetector.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.recording.PhoneCallService;

public class ServicesManager {

    public static void startServices(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String firstRun = context.getString(R.string.pref_first_run);

        if (preferences.getBoolean(firstRun, true)) {

            // Start recording phone calls
            Intent phoneCall = new Intent(context, PhoneCallService.class);
            context.startService(phoneCall);
            
            preferences.edit().putBoolean(firstRun, false).apply();
        }
    }

    public static void stopServices(Context context) {
        context.stopService(new Intent(context, PhoneCallService.class));
    }
}
