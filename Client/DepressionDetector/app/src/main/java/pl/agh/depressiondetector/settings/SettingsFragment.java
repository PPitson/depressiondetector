package pl.agh.depressiondetector.settings;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import pl.agh.depressiondetector.R;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }
}
