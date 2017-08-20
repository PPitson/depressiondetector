package pl.agh.depressiondetector.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.authentication.AuthenticationActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_profile_delete:
                break;
            case R.id.item_profile_logout:
                removeCredentialPreferences();
                finishAffinity();
                startActivity(new Intent(this, AuthenticationActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeCredentialPreferences(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(getString(R.string.pref_user_username));
        editor.remove(getString(R.string.pref_user_password));
        editor.apply();
    }
}
