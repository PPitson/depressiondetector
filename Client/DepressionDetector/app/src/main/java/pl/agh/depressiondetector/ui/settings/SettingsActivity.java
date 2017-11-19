package pl.agh.depressiondetector.ui.settings;

import android.Manifest;
import android.content.SharedPreferences;

import android.support.v7.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;

import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.utils.ToastUtils;

import static pl.agh.depressiondetector.analytics.AnalysedDataType.MOOD;
import static pl.agh.depressiondetector.analytics.AnalysedDataType.PHONE_CALL;
import static pl.agh.depressiondetector.analytics.AnalysedDataType.SMS;
import static pl.agh.depressiondetector.analytics.AnalyticsAdapter.startAnalytics;
import static pl.agh.depressiondetector.analytics.AnalyticsAdapter.stopAnalytics;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        settingsFragment = new SettingsFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.framelayout_settings, settingsFragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        boolean newValue = preferences.getBoolean(key, false);

        if (key.equals(getString(R.string.pref_analyse_mood))) {
            if (newValue)
                askForMoodPermissionsAndStartAnalytics();
            else
                stopAnalytics(this, MOOD);
        }

        if (key.equals(getString(R.string.pref_analyse_phone_calls))) {
            if (newValue)
                askForPhoneCallsPermissionsAndStartAnalytics();
            else
                stopAnalytics(this, PHONE_CALL);
        }

        if (key.equals(getString(R.string.pref_analyse_text_messages))) {
            if (newValue)
                askForTextMessagesPermissionsAndStartAnalytics();
            else
                stopAnalytics(this, SMS);
        }
    }


    private void askForMoodPermissionsAndStartAnalytics() {
        BasePermissionListener listener = new BasePermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                startAnalytics(SettingsActivity.this, MOOD);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                settingsFragment.setMoodPreferenceChecked(false);
                ToastUtils.show(SettingsActivity.this, "We can't start analytics without this permission");
            }
        };

        askForPermission(listener, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void askForPhoneCallsPermissionsAndStartAnalytics() {
        BaseMultiplePermissionsListener listener = new BaseMultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    startAnalytics(SettingsActivity.this, PHONE_CALL);
                } else {
                    settingsFragment.setPhoneCallsPreferenceChecked(false);
                    ToastUtils.show(SettingsActivity.this, "We can't start analytics without this permission");
                }
            }
        };

        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_PHONE_STATE);
        permissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        permissions.add(Manifest.permission.RECORD_AUDIO);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        askForPermissions(listener, permissions);
    }

    private void askForTextMessagesPermissionsAndStartAnalytics() {
        BaseMultiplePermissionsListener listener = new BaseMultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    startAnalytics(SettingsActivity.this, SMS);
                } else {
                    settingsFragment.setTextMessagesPreferenceChecked(false);
                    ToastUtils.show(SettingsActivity.this, "We can't start analytics without this permission");
                }
            }
        };

        List<String> permissions = new ArrayList<>(3);
        permissions.add(Manifest.permission.READ_SMS);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        askForPermissions(listener, permissions);
    }

    private void askForPermission(PermissionListener listener, String permission) {
        Dexter.withActivity(this)
                .withPermission(permission)
                .withListener(listener)
                .onSameThread()
                .check();
    }

    private void askForPermissions(MultiplePermissionsListener listener, List<String> permissions) {
        Dexter.withActivity(this)
                .withPermissions(permissions)
                .withListener(listener)
                .onSameThread()
                .check();
    }
}
