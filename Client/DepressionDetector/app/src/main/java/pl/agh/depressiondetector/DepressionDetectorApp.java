package pl.agh.depressiondetector;


import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import pl.agh.depressiondetector.recording.PhoneCallService;

public class DepressionDetectorApp extends Application {

    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        startPhoneCallService();
    }

    // TODO Add somewhere stopping the service
    private void startPhoneCallService() {
        boolean record = preferences.getBoolean(getString(R.string.pref_analyse_phone_calls), true);
        if(record){
            boolean working = preferences.getBoolean(getString(R.string.pref_analyse_phone_calls_running), false);
            if(!working){
                Intent intent = new Intent(this, PhoneCallService.class);
                startService(intent);
            }
        }
    }
}
