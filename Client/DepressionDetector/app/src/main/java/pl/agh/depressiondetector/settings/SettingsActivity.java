package pl.agh.depressiondetector.settings;

import android.content.SharedPreferences;

import android.support.v7.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import pl.agh.depressiondetector.R;

import static pl.agh.depressiondetector.analytics.AnalysedDataType.MOOD;
import static pl.agh.depressiondetector.analytics.AnalysedDataType.PHONE_CALL;
import static pl.agh.depressiondetector.analytics.AnalysedDataType.SMS;
import static pl.agh.depressiondetector.analytics.AnalyticsAdapter.startAnalytics;
import static pl.agh.depressiondetector.analytics.AnalyticsAdapter.stopAnalytics;

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
                startAnalytics(this, MOOD);
            else
                stopAnalytics(this, MOOD);
        }

        if (key.equals(getString(R.string.pref_analyse_phone_calls))) {
            if (newValue)
                startAnalytics(this, PHONE_CALL);
            else
                stopAnalytics(this, PHONE_CALL);
        }

        if (key.equals(getString(R.string.pref_analyse_text_messages))) {
            if (newValue)
                startAnalytics(this, SMS);
            else
                stopAnalytics(this, SMS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
