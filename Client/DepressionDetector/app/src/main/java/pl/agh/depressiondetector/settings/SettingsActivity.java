package pl.agh.depressiondetector.settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import pl.agh.depressiondetector.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.framelayout_settings, new SettingsFragment())
                .commit();
    }
}
