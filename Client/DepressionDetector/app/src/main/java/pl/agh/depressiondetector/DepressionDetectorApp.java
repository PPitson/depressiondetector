package pl.agh.depressiondetector;


import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import pl.agh.depressiondetector.recording.PhoneCallService;

public class DepressionDetectorApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        startServices();
    }

    private void startServices() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String firstRun = getString(R.string.pref_first_run);

        if (preferences.getBoolean(firstRun, true)) {

            // Start recording phone calls
            Intent phoneCall = new Intent(this, PhoneCallService.class);
            startService(phoneCall);

            preferences.edit().putBoolean(firstRun, false).apply();
        }
    }
}
