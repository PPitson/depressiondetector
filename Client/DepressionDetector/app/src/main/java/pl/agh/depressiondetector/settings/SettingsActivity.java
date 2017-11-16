package pl.agh.depressiondetector.settings;

import android.content.SharedPreferences;

import android.support.v7.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import pl.agh.depressiondetector.R;

import static pl.agh.depressiondetector.analytics.AnalyticsManager.startListeningForSmses;
import static pl.agh.depressiondetector.analytics.AnalyticsManager.startMoodAlarm;
import static pl.agh.depressiondetector.analytics.AnalyticsManager.startRecordingPhoneCalls;
import static pl.agh.depressiondetector.analytics.AnalyticsManager.stopListeningForSmses;
import static pl.agh.depressiondetector.analytics.AnalyticsManager.stopMoodAlarm;
import static pl.agh.depressiondetector.analytics.AnalyticsManager.stopRecordingPhoneCalls;

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

        if (key.equals(getString(R.string.pref_analyse_mood))) {
            if (newValue)
                startMoodAlarm(this);
            else
                stopMoodAlarm(this);
        }

        if (key.equals(getString(R.string.pref_analyse_phone_calls))) {
            if (newValue)
                startRecordingPhoneCalls(this);
            else
                stopRecordingPhoneCalls(this);
        }

        if (key.equals(getString(R.string.pref_analyse_text_messages))) {
            if (newValue)
                startListeningForSmses(this);
            else
                stopListeningForSmses(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
