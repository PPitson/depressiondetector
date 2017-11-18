package pl.agh.depressiondetector.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

import pl.agh.depressiondetector.R;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;
import static pl.agh.depressiondetector.analytics.AnalysedDataType.MOOD;
import static pl.agh.depressiondetector.analytics.AnalysedDataType.PHONE_CALL;
import static pl.agh.depressiondetector.analytics.AnalysedDataType.SMS;


public class SettingsFragment extends PreferenceFragmentCompat {

    private SwitchPreferenceCompat moodPreference;
    private SwitchPreferenceCompat phoneCallsPreference;
    private SwitchPreferenceCompat textMessagesPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String s) {
        addPreferencesFromResource(R.xml.preferences);

        moodPreference = (SwitchPreferenceCompat) findPreference(MOOD.preferenceName);
        phoneCallsPreference = (SwitchPreferenceCompat) findPreference(PHONE_CALL.preferenceName);
        textMessagesPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.pref_analyse_text_messages));

        SharedPreferences preferences = getDefaultSharedPreferences(getContext());
        setMoodPreferenceChecked(preferences.getBoolean(MOOD.preferenceName, false));
        setPhoneCallsPreferenceChecked(preferences.getBoolean(PHONE_CALL.preferenceName, false));
        setTextMessagesPreferenceChecked(preferences.getBoolean(SMS.preferenceName, false));
    }

    public void setMoodPreferenceChecked(boolean value) {
        moodPreference.setChecked(value);
    }

    public void setPhoneCallsPreferenceChecked(boolean value) {
        phoneCallsPreference.setChecked(value);
    }

    public void setTextMessagesPreferenceChecked(boolean value) {
        textMessagesPreference.setChecked(value);
    }
}
