package pl.agh.depressiondetector.settings;

import android.content.Intent;
import android.content.SharedPreferences;

import android.support.v7.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.analytics.messages_upload.TextMessageService;
import pl.agh.depressiondetector.analytics.recording.PhoneCallService;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.framelayout_settings, new SettingsFragment())
                .commit();
    }


    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        boolean newValue = preferences.getBoolean(key, false);

        if (key.equals(getString(R.string.pref_analyse_phone_calls))) {
            Intent phoneCall = new Intent(this, PhoneCallService.class);
            if (newValue)
                startService(phoneCall);
            else
                stopService(phoneCall);
        }

        if (key.equals(getString(R.string.pref_analyse_text_messages))) {
            Intent textMessage = new Intent(this, TextMessageService.class);
            if (newValue)
                startService(textMessage);
            else
                stopService(textMessage);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
